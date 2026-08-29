package com.tastyhouse.adminapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.admin.model.AdminRole;
import com.tastyhouse.adminapi.admin.application.port.in.AdminCommandUseCase;
import com.tastyhouse.adminapi.admin.application.port.in.AdminCreateCommand;
import com.tastyhouse.adminapi.admin.application.service.AdminQueryService;

/**
 * 최초 SUPER_ADMIN 시드.
 * 공개 회원가입이 없으므로 첫 관리자는 부팅 시 멱등하게 주입한다.
 * 초기 자격증명은 application.yml(admin.seed.*)에서 주입한다.
 */
@Configuration
public class AdminSeeder {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    @Bean
    public ApplicationRunner seedSuperAdmin(
        AdminQueryService adminQueryService,
        AdminCommandUseCase adminCommandUseCase,
        AdminSeedProperties seedProperties
    ) {
        return (ApplicationArguments args) -> {
            String username = seedProperties.username();
            if (adminQueryService.existsByUsername(username)) {
                log.info("[AdminSeeder] SUPER_ADMIN '{}' 이미 존재 - 시드 생략", username);
                return;
            }
            // 기본(취약) 비밀번호로 운영에 시드되는 것을 방지: 신규 시드 시에는 외부 주입 비밀번호를 강제한다.
            if (seedProperties.isDefaultPassword()) {
                throw new IllegalStateException(
                    "최초 SUPER_ADMIN을 생성하려면 ADMIN_SEED_PASSWORD 환경변수로 안전한 비밀번호를 지정해야 합니다.");
            }
            AdminCreateCommand command = AdminCreateCommand.of(
                username,
                seedProperties.password(),
                seedProperties.name(),
                AdminRole.SUPER_ADMIN.name()
            );
            adminCommandUseCase.createAdmin(command);
            log.info("[AdminSeeder] 최초 SUPER_ADMIN '{}' 생성 완료", username);
        };
    }
}
