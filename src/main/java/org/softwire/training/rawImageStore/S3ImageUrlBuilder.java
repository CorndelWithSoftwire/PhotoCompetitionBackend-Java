package org.softwire.training.rawImageStore;

import com.amazonaws.services.s3.AmazonS3;

public class S3ImageUrlBuilder implements ImageUrlBuilder {
    private final AmazonS3 s3;
    private final String s3Bucket;

    public S3ImageUrlBuilder(AmazonS3 s3, String s3Bucket) {
        this.s3 = s3;
        this.s3Bucket = s3Bucket;
    }

    public String build(String filename) {
        return s3.getUrl(s3Bucket, filename).toString();
    }
}
