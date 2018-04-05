package org.softwire.training.rawImageStore;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.List;

public class S3RawImageDao implements RawImageDao {

    private final AmazonS3 s3;
    private final String s3Bucket;

    public S3RawImageDao(AmazonS3 s3, String s3Bucket) {
        this.s3 = s3;
        this.s3Bucket = s3Bucket;
    }

    @Override
    public void put(String key, InputStream inputStream, String contentType, long contentLength) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        objectMetadata.setContentLength(contentLength);
        s3.putObject(new PutObjectRequest(s3Bucket, key, inputStream, objectMetadata));
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(s3Bucket, key);
    }

    @Override
    public void delete(List<String> keys) {
        s3.deleteObjects(new DeleteObjectsRequest(s3Bucket).withKeys((String[]) keys.toArray()));
    }

    @Override
    public void ping() {
        s3.headBucket(new HeadBucketRequest(s3Bucket));
    }
}
