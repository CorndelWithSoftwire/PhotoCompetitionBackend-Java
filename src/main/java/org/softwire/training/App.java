package org.softwire.training;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.github.arteam.jdbi3.JdbiFactory;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.logging.LoggingFeature;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.softwire.training.auth.AdminConstraintSecurityHandler;
import org.softwire.training.auth.BasicPrincipal;
import org.softwire.training.auth.PrincipalRepository;
import org.softwire.training.checks.S3Check;
import org.softwire.training.components.ClientVisibleExceptionMapper;
import org.softwire.training.components.ImageResource;
import org.softwire.training.components.UserResource;
import org.softwire.training.core.ImageRepository;
import org.softwire.training.core.ThumbnailGenerator;
import org.softwire.training.core.UserRepository;
import org.softwire.training.metadataStore.ImageMetadataDao;
import org.softwire.training.metadataStore.UserDao;
import org.softwire.training.rawImageStore.ImageUrlBuilder;
import org.softwire.training.rawImageStore.RawImageDao;

import javax.ws.rs.container.ContainerResponseFilter;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class App extends Application<Config> {

    public static void main(final String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public String getName() {
        return "webdev";
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());
        bootstrap.addBundle(new MigrationsBundle<Config>() {
            @Override
            public DataSourceFactory getDataSourceFactory(Config configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(new AssetsBundle("/api-specification.html", "/api-specification"));
    }

    @Override
    public void run(final Config configuration,
                    final Environment environment) {
        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "mysql");
        jdbi.installPlugin(new SqlObjectPlugin());

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.EU_WEST_1)
                .build();

        final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();

        // Build resources
        final ImageUrlBuilder imageUrlBuilder = configuration.getRawImagesFactory().imageUrlBuilder(s3);
        final RawImageDao rawImageDao = configuration.getRawImagesFactory().rawImageDao(s3);
        final ImageMetadataDao imageMetadataDao = jdbi.onDemand(ImageMetadataDao.class);
        final UserDao userDao = jdbi.onDemand(UserDao.class);
        final UserRepository userRepository = jdbi.onDemand(UserRepository.class);
        final ImageRepository imageRepository = new ImageRepository(
                rawImageDao,
                imageMetadataDao,
                imageUrlBuilder,
                thumbnailGenerator);

        // Register components
        environment.jersey().register(new ImageResource(imageMetadataDao, imageUrlBuilder, imageRepository));
        environment.jersey().register(new UserResource(userDao, rawImageDao, userRepository, imageRepository));
        environment.jersey().register(new ClientVisibleExceptionMapper());

        // Authorisation
        PrincipalRepository principalRepository = new PrincipalRepository(
                configuration.getAdminPassword(), configuration.getModeratorPassword());
        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<BasicPrincipal>()
                        .setAuthenticator(credentials -> principalRepository
                                        .getByUsername(credentials.getUsername())
                                        .filter(principal -> principal.getPassword().equals(credentials.getPassword())))
                        .setAuthorizer((principal, role) -> principal.getRoles().contains(role))
                        .setRealm("Photo Competition")
                        .buildAuthFilter()));
        environment.admin().setSecurityHandler(new AdminConstraintSecurityHandler(principalRepository.getAdminPrincipal()));

        // We don't to cause problems for anyone trying to access this with an xhr from localhost.
        final ContainerResponseFilter accessControlAllowAnyOrigin = (requestContext, responseContext) -> {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "OPTIONS, GET, POST, DELETE");
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Accept, Origin, Authorization");
        };
        environment.jersey().register(accessControlAllowAnyOrigin);

        // Log request payloads all the time
        environment.jersey().register(new LoggingFeature(
                java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
                Level.INFO, LoggingFeature.Verbosity.PAYLOAD_TEXT,
                LoggingFeature.DEFAULT_MAX_ENTITY_SIZE));

        // Register Swagger OpenAPI generation
        environment.jersey().register(
                new OpenApiResource().openApiConfiguration(configuration.getSwaggerConfiguration()));

        // Register health checks
        environment.healthChecks().register("S3Check", new S3Check(rawImageDao));

        // Die on startup if any health checks fail
        Map<String, String> failedHealthChecks = environment.healthChecks()
                .runHealthChecks()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isHealthy())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getKey));
        if (!failedHealthChecks.isEmpty()) {
            throw new RuntimeException(String.format("Failed health checks on startup: %s", failedHealthChecks));
        }
    }
}
