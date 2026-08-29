package com.tastyhouse.ceoapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tastyhouse.ceoapi.ceo.application.port.in.CeoCommandUseCase;
import com.tastyhouse.ceoapi.ceo.application.port.in.CeoCreateCommand;
import com.tastyhouse.ceoapi.ceo.application.service.CeoQueryService;

/**
 * 최초 점주 계정 시드.
 * 공개 회원가입이 없으므로 첫 점주 계정은 부팅 시 멱등하게 주입한다.
 * 초기 자격증명은 application.yml(ceo.seed.*)에서 주입한다.
 */
@Configuration
public class CeoSeeder {

    private static final Logger log = LoggerFactory.getLogger(CeoSeeder.class);

    @Bean
    public ApplicationRunner seedCeo(
        CeoQueryService ceoQueryService,
        CeoCommandUseCase ceoCommandUseCase,
        PasswordEncoder passwordEncoder,
        CeoSeedProperties seedProperties
    ) {
        return (ApplicationArguments args) -> {
            String username = seedProperties.username();
            if (ceoQueryService.existsByUsername(username)) {
                log.info("[CeoSeeder] 점주 '{}' 이미 존재 - 시드 생략", username);
                return;
            }
            // 기본(취약) 비밀번호로 운영에 시드되는 것을 방지: 신규 시드 시에는 외부 주입 비밀번호를 강제한다.
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
