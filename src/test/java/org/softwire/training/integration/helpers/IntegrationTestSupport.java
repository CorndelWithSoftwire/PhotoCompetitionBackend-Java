package org.softwire.training.integration.helpers;

import com.amazonaws.services.s3.AmazonS3;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.softwire.training.App;
import org.softwire.training.Config;
import org.softwire.training.rawImageStore.ImageUrlBuilder;
import org.softwire.training.rawImageStore.RawImageDao;
import org.softwire.training.rawImageStore.RawImagesFactory;

public class IntegrationTestSupport {

    private static final String TEST_CONFIG = ResourceHelpers.resourceFilePath("test-config.yml");

    private static final DropwizardTestSupport<Config> DROPWIZARD_TEST_SUPPORT =
            new DropwizardTestSupport<>(ApplicationWithMockAws.class, TEST_CONFIG);
    private static final MockUrlBuilder URL_BUILDER = new MockUrlBuilder();
    private static final MockRawImageDao RAW_IMAGE_DAO = new MockRawImageDao();

    private Client client;

    public Client getClient() {
        return client;
    }

    public byte[] getDataFromRawImageDao(String filename) {
        return RAW_IMAGE_DAO.get(URL_BUILDER.deconstruct(filename));
    }

    public void before() throws Exception {
        DROPWIZARD_TEST_SUPPORT.before();
        DROPWIZARD_TEST_SUPPORT.getApplication().run("db", "migrate", TEST_CONFIG);
        client = new Client(
                DROPWIZARD_TEST_SUPPORT.getLocalPort(),
                DROPWIZARD_TEST_SUPPORT.getConfiguration().getAdminPassword()
        );
    }

    public void after() throws Exception {
        DROPWIZARD_TEST_SUPPORT.after();
        client.close();
    }

    public static class ApplicationWithMockAws extends App {
        @Override
        public void run(Config configuration, Environment environment) {
            configuration.setRawImagesFactory(new RawImagesFactory(){
                @Override
                public RawImageDao rawImageDao(AmazonS3 s3) {
                    return RAW_IMAGE_DAO;
                }

                @Override
                public ImageUrlBuilder imageUrlBuilder(AmazonS3 s3) {
                    return URL_BUILDER;
                }
            });
            super.run(configuration, environment);
        }
    }

    public static class MockUrlBuilder implements ImageUrlBuilder {
        @Override
        public String build(String key) {
            return "URL:" + key;
        }

        String deconstruct(String filename) {
            return filename.substring(4);
        }
    }
}
