package com.tastyhouse.ceoapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tastyhouse.application.ceo.port.in.CeoCommandUseCase;
import com.tastyhouse.application.ceo.port.in.CeoCreateCommand;
import com.tastyhouse.application.ceo.port.in.CeoOwnerQueryUseCase;

@Configuration
public class CeoSeeder {
    private static final Logger log = LoggerFactory.getLogger(CeoSeeder.class);

    @Bean
    public ApplicationRunner seedCeo(
        CeoOwnerQueryUseCase ceoQueryUseCase,
        CeoCommandUseCase ceoCommandUseCase,
        PasswordEncoder passwordEncoder,
        CeoSeedProperties seedProperties
    ) {
        return (ApplicationArguments args) -> {
            String username = seedProperties.username();
            if (ceoQueryUseCase.existsByUsername(username)) {
                log.info("[CeoSeeder] 점주 '{}' 이미 존재 - 시드 생략", username);
                return;
            }

            if (seedProperties.isDefaultPassword()) {
                throw new IllegalStateException(
                    "최초 점주 계정을 생성하려면 CEO_SEED_PASSWORD 환경변수로 안전한 비밀번호를 지정해야 합니다.");
            }
            CeoCreateCommand command = CeoCreateCommand.of(
                username,
                passwordEncoder.encode(seedProperties.password()),
                seedProperties.name()
            );
            ceoCommandUseCase.createCeo(command);
            log.info("[CeoSeeder] 최초 점주 '{}' 생성 완료", username);
        };
    }
}
