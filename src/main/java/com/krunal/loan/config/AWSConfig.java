package com.krunal.loan.config;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import javax.sql.DataSource;

@Configuration
public class AWSConfig {

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.secretName}")
    private String secretName;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private final Environment env;

    public AWSConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public S3Client amazonS3() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public JSONObject secretsClient() throws JSONException {

        try (SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            GetSecretValueRequest secretRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            GetSecretValueResponse secretResponse = secretsClient.getSecretValue(secretRequest);

            String secretJson = secretResponse.secretString();
            return new JSONObject(secretJson);
        }
    }

    @Bean
    public DataSource dataSource() throws JSONException {
        JSONObject secret = secretsClient();
        String username = secret.getString("username");
        String password = secret.getString("password");
        String host = secret.getString("host");
        String database = secret.getString("database");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:mysql://" + host + ":3306/" + database + "?allowPublicKeyRetrieval=true&useSSL=false");
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

}