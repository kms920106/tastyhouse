package com.tastyhouse.adminapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.adminapplication.admin.port.in.AdminCommandUseCase;
import com.tastyhouse.adminapplication.admin.port.in.AdminCreateCommand;
import com.tastyhouse.adminapplication.admin.port.in.AdminQueryUseCase;

/**
 * 최초 SUPER_ADMIN 시드.
 * 공개 회원가입이 없으므로 첫 관리자는 부팅 시 멱등하게 주입한다.
 * 초기 자격증명은 application.yml(admin.seed.*)에서 주입한다.
 *
 * <p><b>role은 도메인 enum이 아니라 문자열로 넘긴다.</b> HTTP 경계가 도메인 enum을 {@code String}으로
 * 받고 승격은 서비스가 {@code Enum.from(String)}으로 수행한다는 도메인 enum 경계 규칙
 * (backend/CLAUDE.md)을 부트스트랩에도 동일하게 적용한 것이다 — {@code AdminCreateRequest}가
 * {@code allowableValues}로 같은 문자열을 쓰는 것과 대칭이며, 덕분에 이 모듈은
 * {@code com.tastyhouse.domain..}를 알지 않는다({@code apiModuleShouldBeDomainModelFree}).
 * 승격·검증은 {@code AdminCommandService}의 {@code AdminRole.from(command.role())}이 담당하므로,
 * 상수명이 어긋나면 {@code BusinessException(ErrorCode.ADMIN_ROLE_UNKNOWN)}으로 <b>신규 시드 경로에서
 * 부팅이 실패</b>한다. 다만 이 검증은 아래 멱등성 체크 <b>이후</b>에 있으므로, 이미 시드된 DB에서는
 * 시드가 생략되어 드러나지 않는다 — enum 참조를 뗀 대가로 잃는 것은 컴파일 게이트가 아니라
 * <b>검출 시점</b>이다(빈 DB 최초 기동까지 지연). 부팅이 실패한다는 점은 위
 * {@code isDefaultPassword()} 가드와 같지만 예외 타입은 다르다(그쪽은 {@code IllegalStateException}).
 */
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
            // 기본(취약) 비밀번호로 운영에 시드되는 것을 방지: 신규 시드 시에는 외부 주입 비밀번호를 강제한다.
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
