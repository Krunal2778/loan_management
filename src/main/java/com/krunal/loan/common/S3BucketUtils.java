package com.krunal.loan.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

@Component
public class S3BucketUtils {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private static final Logger logger = LoggerFactory.getLogger(S3BucketUtils.class);
    private static final String ERROR_MESSAGE = "Error";

    private final S3Client s3Client;
    private static final Random random = new Random();

    @Autowired
    public S3BucketUtils(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadImageToS3Bucket(String base64Image) {
        logger.info("Getting Started with Amazon S3");
        Date curDate = DateUtils.getCurrentDateObject(DateUtils.IST);
        String key;
        byte[] imageBytes;
        if (base64Image != null && !base64Image.isEmpty()) {
            // Decode the base64 image
            imageBytes = Base64.getDecoder().decode(base64Image);
            String imageName = "_image.jpg";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            int randomNumber = random.nextInt(90000000) + 10000000;
            key = String.format("%s%s", sdf.format(curDate)+ "/"+randomNumber, imageName);
        } else {
            logger.error("Error no image provided");
            return ERROR_MESSAGE;
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("image/jpeg")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
            logger.info("Object uploaded with key: {}", key);
            return key;
        } catch (S3Exception e) {
            logger.error("Error uploading file to S3: {}", e.getMessage());
            return ERROR_MESSAGE;
        }
    }

    public String getFileFromS3(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            logger.info("Successfully downloaded image with key: {}", key);
            byte[] byteArray = objectBytes.asByteArray();
            return Base64.getEncoder().encodeToString(byteArray);
        } catch (S3Exception e) {
            logger.error("Failed to download image from S3: {}", e.awsErrorDetails().errorMessage());
            return ERROR_MESSAGE;
        }
    }
}