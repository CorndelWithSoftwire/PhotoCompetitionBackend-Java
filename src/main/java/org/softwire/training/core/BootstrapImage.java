package org.softwire.training.core;

import com.google.common.base.MoreObjects;
import org.softwire.training.api.ApiImage;

public class BootstrapImage {

    private final ApiImage apiImage;
    private final String mimeType;
    private final String resourceName;

    BootstrapImage(ApiImage apiImage, String mimeType, String resourceName) {
        this.apiImage = apiImage;
        this.mimeType = mimeType;
        this.resourceName = resourceName;
    }

    public ApiImage getApiImage() {
        return apiImage;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getResourceName() {
        return resourceName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("apiImage", apiImage)
                .add("mimeType", mimeType)
                .add("resourceName", resourceName)
                .toString();
    }
}