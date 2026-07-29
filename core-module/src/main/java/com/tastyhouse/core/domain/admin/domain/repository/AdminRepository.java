package com.tastyhouse.core.domain.admin.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.admin.domain.model.Admin;

/**
 * 관리자 계정 write 포트.
 *
 * <p>표현 목적 read model이 없는 도메인이라 별도 query DAO를 두지 않는다. 여기 남은 조회는 모두
 * 인증(username 단건 로드)·중복 검증(existsByUsername)에 필요한 엔티티/원시값 반환이므로
 * write 포트 잔류 판정 기준에 해당한다.
 */
public interface AdminRepository {

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);

    Admin save(Admin admin);
}
