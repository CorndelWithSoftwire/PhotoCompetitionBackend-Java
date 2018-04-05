package org.softwire.training.rawImageStore;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class RawImagesFactory {
    @NotEmpty
    private String s3Bucket;

    @JsonProperty
    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public RawImageDao rawImageDao(AmazonS3 s3) {
        return new S3RawImageDao(s3, s3Bucket);
    }

    public ImageUrlBuilder imageUrlBuilder(AmazonS3 s3) {
        return new S3ImageUrlBuilder(s3, s3Bucket);
    }
}
