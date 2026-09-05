package com.tastyhouse.external.aws.ses;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

import com.tastyhouse.domain.mail.port.MailSender;
import com.tastyhouse.external.mail.MailProperties;

@Configuration
@ConditionalOnProperty(name = "mail.provider", havingValue = "ses")
public class SesConfig {

    @Bean
    public SesClient sesClient(
            @Value("${mail.aws.ses.access-key}") String accessKey,
            @Value("${mail.aws.ses.secret-key}") String secretKey,
            @Value("${mail.aws.ses.region}") String region
    ) {
        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean
    public MailSender awsSesMailSender(SesClient sesClient, MailProperties mailProperties) {
        return new SesMailSender(sesClient, mailProperties.senderAddress());
    }
}
