package com.tastyhouse.adminapi.admin;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.admin.domain.model.Admin;
import com.tastyhouse.domain.admin.domain.repository.AdminRepository;

/**
 * 관리자 계정 조회 서비스.
 *
 * <p>표현 목적 read model이 없는 도메인이라 infra query DAO를 두지 않고 domain write 포트
 * ({@link AdminRepository})를 그대로 주입한다. 여기서 제공하는 조회는 모두 인증·중복 검증에
 * 필요한 엔티티/원시값 반환이므로 README의 "write 포트 잔류 판정 기준"에 해당한다.
 */
@Service
@Transactional(readOnly = true)
public class AdminQueryService {

    private final AdminRepository adminRepository;

    public AdminQueryService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * 인증(UserDetails 로드·토큰 갱신 시 계정 상태 재검증)용 단건 조회.
     */
    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    /**
     * SUPER_ADMIN 시드 멱등성 확인용 존재 검증.
     */
    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }
}
