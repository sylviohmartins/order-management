package com.example.order.management.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsConfig {

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder() //
                .region(Region.SA_EAST_1) //
                .credentialsProvider(ProfileCredentialsProvider.create()) //
                .build();
    }

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder() //
                .region(Region.SA_EAST_1) //
                .credentialsProvider(ProfileCredentialsProvider.create()) //
                .build();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder() //
                .region(Region.SA_EAST_1) //
                .credentialsProvider(ProfileCredentialsProvider.create()) //
                .build();
    }
}
