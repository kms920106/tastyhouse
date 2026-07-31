package com.tastyhouse.external.sms.sns;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import com.tastyhouse.domain.verification.domain.port.SmsSender;

@Configuration
@ConditionalOnProperty(name = "sms.provider", havingValue = "sns")
public class SnsConfig {

    @Bean
    public SnsClient snsClient(
            @Value("${sms.aws.sns.access-key}") String accessKey,
            @Value("${sms.aws.sns.secret-key}") String secretKey,
            @Value("${sms.aws.sns.region}") String region
    ) {
        return SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean
    public SmsSender awsSnsSmsSender(SnsClient snsClient) {
        return new AwsSnsSmsSender(snsClient);
    }
}
