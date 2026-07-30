package com.tastyhouse.core.domain.ceo.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.ceo.domain.model.Ceo;

/**
 * 점주 계정 write 포트.
 *
 * <p>인증(UserDetails 로드·토큰 갱신 시 계정 상태 재검증)과 username 중복 검증에 필요한 단건 조회만
 * 남긴다. 표현 목적 목록 조회는 infrastructure-module의 {@code CeoQueryDao}가 담당한다.
 */
public interface CeoRepository {

    Optional<Ceo> findByUsername(String username);

    boolean existsByUsername(String username);

    Ceo save(Ceo ceo);
}
