package org.softwire.training.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class ClientVisibleError {

    private final int code;
    private final String clientVisibleDetail;

    public ClientVisibleError(int code, String clientVisibleError) {
        this.code = code;
        this.clientVisibleDetail = clientVisibleError;
    }

    @JsonProperty("code")
    public int getCode() {
        return code;
    }

    @JsonProperty("clientVisibleDetail")
    public String getClientVisibleDetail() {
        return clientVisibleDetail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientVisibleError that = (ClientVisibleError) o;
        return code == that.code &&
                Objects.equal(clientVisibleDetail, that.clientVisibleDetail);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code, clientVisibleDetail);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("clientVisibleDetail", clientVisibleDetail)
                .toString();
    }
}
