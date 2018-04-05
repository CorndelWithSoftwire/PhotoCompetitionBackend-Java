package org.softwire.training.api;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.softwire.training.models.License;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@SuppressWarnings("unused")
public class ApiImage {
    private final Integer id;
    private final String url;
    private final Integer score;
    private final String author;
    private final String name;
    private final License license;
    private final String thumbnail;

    @JsonCreator
    public ApiImage(@JsonProperty("id") Integer id,
                    @JsonProperty("url") String url,
                    @JsonProperty("score") Integer score,
                    @JsonProperty("author") String author,
                    @JsonProperty("name") String name,
                    @JsonProperty("license") License license,
                    @JsonProperty("thumbnail") String thumbnail) {
        this.id = id;
        this.url = url;
        this.score = score;
        this.author = author;
        this.name = name;
        this.license = license;
        this.thumbnail = thumbnail;
    }

    @JsonProperty
    @Null
    public Integer getId() {
        return id;
    }

    public ApiImage withId(int id) {
        return new ApiImage(id, url, score, author, name, license, thumbnail);
    }

    @JsonProperty
    @Null
    public String getUrl() {
        return url;
    }

    public ApiImage withUrl(String url) {
        return new ApiImage(id, url, score, author, name, license, thumbnail);
    }

    @JsonProperty
    @Null
    public Integer getScore() {
        return score;
    }

    public ApiImage withScore(Integer score) {
        return new ApiImage(id, url, score, author, name, license, thumbnail);
    }

    @JsonProperty
    @Null
    public String getThumbnail() {
        return thumbnail;
    }

    public ApiImage withThumbnail(String thumbnail) {
        return new ApiImage(id, url, score, author, name, license, thumbnail);
    }

    @JsonProperty
    @NotEmpty
    @Length(max = 255)
    @Schema(required=true)
    public String getAuthor() {
        return author;
    }

    public ApiImage withAuthor(String author) {
        return new ApiImage(id, url, score, author, name, license, thumbnail);
    }

    @JsonProperty
    @NotEmpty
    @Length(max = 255)
    @Schema(required=true)
    public String getName() {
        return name;
    }

    public ApiImage withName(String name) {
        return new ApiImage(id, url, score, author, name, license, thumbnail);
    }

    @JsonProperty
    @NotNull
    @Schema(required=true)
    public License getLicense() {
        return license;
    }

    public ApiImage withLicense(License license) {
        return new ApiImage(id, url, score, author, name, license, thumbnail);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiImage apiImage = (ApiImage) o;
        return Objects.equal(id, apiImage.id) &&
                Objects.equal(url, apiImage.url) &&
                Objects.equal(score, apiImage.score) &&
                Objects.equal(author, apiImage.author) &&
                Objects.equal(name, apiImage.name) &&
                license == apiImage.license;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, url, score, author, name, license);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("url", url)
                .add("score", score)
                .add("author", author)
                .add("name", name)
                .add("license", license)
                .toString();
    }
}
