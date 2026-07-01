package com.tastyhouse.external.email.ses;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

import com.tastyhouse.core.domain.verification.application.port.out.MailSender;
import com.tastyhouse.external.email.EmailProperties;

@Configuration
@ConditionalOnProperty(name = "email.provider", havingValue = "ses")
public class SesConfig {

    @Bean
    public SesClient sesClient(
            @Value("${email.aws.ses.access-key}") String accessKey,
            @Value("${email.aws.ses.secret-key}") String secretKey,
            @Value("${email.aws.ses.region}") String region
    ) {
        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean
    public MailSender awsSesEmailSender(SesClient sesClient, EmailProperties emailProperties) {
        return new AwsSesEmailSender(sesClient, emailProperties.getSenderAddress());
    }
}
