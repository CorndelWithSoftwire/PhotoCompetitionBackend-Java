package org.softwire.training.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Null;

@SuppressWarnings("unused")
public class ApiUser {
    private final String uuid;
    private final String name;

    @JsonCreator
    public ApiUser(@JsonProperty("uuid") String uuid,
                   @JsonProperty("name") String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @JsonProperty
    @Null
    public String getUuid() {
        return uuid;
    }

    public ApiUser withUuid(String uuid) {
        return new ApiUser(uuid, name);
    }

    @JsonProperty
    @NotEmpty
    @Length(max = 255)
    @Schema(required=true)
    public String getName() {
        return name;
    }

    public ApiUser withName(String name) {
        return new ApiUser(uuid, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiUser apiUser = (ApiUser) o;
        return Objects.equal(uuid, apiUser.uuid) &&
                Objects.equal(name, apiUser.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid, name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", uuid)
                .add("name", name)
                .toString();
    }
}
