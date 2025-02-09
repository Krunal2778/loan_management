package com.krunal.loan.service;

import com.amazonaws.services.s3.model.S3Object;

public interface S3Service {

    /**
     * @param array
     * @param fileName
     * @param bucketName
     */
    public void uploadS3Object(byte[] array, String fileName, String bucketName);

    /**
     * @param bucket
     * @param objectId
     * @param preSignedUrlExpiration
     * @param preSignedUrlExpirationTimeUnit
     * @return
     */
    public String generatePreSignedUrl(String bucket, String objectId, int preSignedUrlExpiration,
                                       String preSignedUrlExpirationTimeUnit);

    /**
     *
     * @param fileName
     * @param bucketName
     * @return S3Object
     */
    public S3Object getFileFromS3(String bucketName, String fileName) ;

}
