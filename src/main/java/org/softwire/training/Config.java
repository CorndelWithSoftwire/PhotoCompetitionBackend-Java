package org.softwire.training;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import org.hibernate.validator.constraints.NotEmpty;
import org.softwire.training.rawImageStore.RawImagesFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@SuppressWarnings("unused")
public class Config extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @Valid
    @NotNull
    private RawImagesFactory rawImageDaoFactory = new RawImagesFactory();

    private SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration()
            .resourcePackages(Sets.newHashSet("org.softwire.training.resources"));

    @NotEmpty
    private String adminPassword;

    @NotEmpty
    private String moderatorPassword;

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("rawImages")
    public RawImagesFactory getRawImagesFactory() {
        return rawImageDaoFactory;
    }

    @JsonProperty("rawImages")
    public void setRawImagesFactory(RawImagesFactory rawImageDaoFactory) {
        this.rawImageDaoFactory = rawImageDaoFactory;
    }

    @JsonProperty("swaggerConfiguration")
    public SwaggerConfiguration getSwaggerConfiguration() {
        return swaggerConfiguration;
    }

    @JsonProperty("swaggerConfiguration")
    public void setSwaggerConfiguration(SwaggerConfiguration swaggerConfiguration) {
        this.swaggerConfiguration = swaggerConfiguration;
    }

    @JsonProperty("adminPassword")
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    @JsonProperty("adminPassword")
    public String getAdminPassword() {
        return adminPassword;
    }

    @JsonProperty("moderatorPassword")
    public void setModeratorPassword(String moderatorPassword) {
        this.moderatorPassword = moderatorPassword;
    }

    @JsonProperty("moderatorPassword")
    public String getModeratorPassword() {
        return moderatorPassword;
    }
}
