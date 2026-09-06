package com.tastyhouse.adminapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.admin.port.in.AdminCommandUseCase;
import com.tastyhouse.application.admin.port.in.AdminCreateCommand;
import com.tastyhouse.application.admin.port.in.AdminQueryUseCase;

@Configuration
public class AdminSeeder {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    @Bean
    public ApplicationRunner seedSuperAdmin(
        AdminQueryUseCase adminQueryUseCase,
        AdminCommandUseCase adminCommandUseCase,
        AdminSeedProperties seedProperties
    ) {
        return (ApplicationArguments args) -> {
            String username = seedProperties.username();
            if (adminQueryUseCase.existsByUsername(username)) {
                log.info("[AdminSeeder] SUPER_ADMIN '{}' 이미 존재 - 시드 생략", username);
                return;
            }

            if (seedProperties.isDefaultPassword()) {
                throw new IllegalStateException(
                    "최초 SUPER_ADMIN을 생성하려면 ADMIN_SEED_PASSWORD 환경변수로 안전한 비밀번호를 지정해야 합니다.");
            }
            AdminCreateCommand command = AdminCreateCommand.of(
                username,
                seedProperties.password(),
                seedProperties.name(),
                "SUPER_ADMIN"
            );
            adminCommandUseCase.createAdmin(command);
            log.info("[AdminSeeder] 최초 SUPER_ADMIN '{}' 생성 완료", username);
        };
    }
}
