package com.tastyhouse.external.email.ses;

import com.tastyhouse.external.email.EmailProperties;
import com.tastyhouse.external.email.EmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
@ConditionalOnProperty(name = "email.provider", havingValue = "ses")
public class SesConfig {

    @Bean
    public SesClient sesClient(
            @Value("${aws.ses.access-key}") String accessKey,
            @Value("${aws.ses.secret-key}") String secretKey,
            @Value("${aws.ses.region}") String region
    ) {
        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean
    public EmailSender awsSesEmailSender(SesClient sesClient, EmailProperties emailProperties) {
        return new AwsSesEmailSender(sesClient, emailProperties.getSenderAddress());
    }
}
