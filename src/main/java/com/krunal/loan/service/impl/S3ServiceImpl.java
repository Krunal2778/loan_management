package com.krunal.loan.service.impl;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import com.krunal.loan.common.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.krunal.loan.service.S3Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3ServiceImpl implements S3Service {

    /**
     * s3client
     */
    @Autowired
    private AmazonS3 s3client;

    /**
     * @param array
     * @param fileName
     * @param bucketName
     */
    @Override
    public void uploadS3Object(byte[] array, String fileName, String bucketName) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(array.length);
        s3client.putObject(bucketName, fileName, new ByteArrayInputStream(array), metadata);

    }

    /**
     * @param bucket
     * @param objectId
     * @param preSignedUrlExpiration
     * @param preSignedUrlExpirationTimeUnit
     * @return
     */
    @Override
    public String generatePreSignedUrl(String bucket, String objectId, int preSignedUrlExpiration,
                                       String preSignedUrlExpirationTimeUnit) {

        log.info("request pre-signed url for {} bucket {}, to be expired in {} {}", objectId, bucket,
                preSignedUrlExpiration, preSignedUrlExpirationTimeUnit);
        // Set the presigned URL to expire after one hour.
        var expiration = new java.util.Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        long expireInMillis = TimeUtil.getTimeInMillis(preSignedUrlExpiration, preSignedUrlExpirationTimeUnit);
        expTimeMillis += expireInMillis;
        log.info("set expiration time as {} millis, link will be expired at {} millis", expireInMillis, expTimeMillis);
        expiration.setTime(expTimeMillis);
        var generatedPresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, objectId).withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        return s3client.generatePresignedUrl(generatedPresignedUrlRequest).toString();

    }

    /**
     * @param s3client
     */
    public void setS3client(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    /**
     *
     * @param fileName
     * @param bucketName
     */

    @Override
    public S3Object getFileFromS3(String bucketName, String fileName) {
        return s3client.getObject(bucketName, fileName);
    }
}


