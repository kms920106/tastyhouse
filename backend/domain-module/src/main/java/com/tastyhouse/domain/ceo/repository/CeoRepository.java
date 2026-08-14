package com.tastyhouse.domain.ceo.repository;

import java.util.Optional;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.vo.CeoId;

/**
 * 점주 계정 write 포트.
 *
 * <p>인증(UserDetails 로드·토큰 갱신 시 계정 상태 재검증)과 username 중복 검증에 필요한 단건 조회만
 * 남긴다. 표현 목적 목록 조회는 infrastructure-module의 {@code CeoQueryDao}가 담당한다.
 */
public interface CeoRepository {

    /**
     * 식별자로 점주를 조회한다.
     *
     * <p>가게 담당 점주 배정 시 대상 점주가 실재하는지 확인하는 불변식 검증용이다 — 이 조회가 없으면
     * 존재하지 않는 계정에 접근권한을 부여하는 이력이 남을 수 있으므로, write 포트 잔류 기준("이 조회가
     * 없으면 불변식 검증이 불가능한가?")을 만족한다.
     */
    Optional<Ceo> findById(CeoId id);

    Optional<Ceo> findByUsername(String username);

    boolean existsByUsername(String username);

    Ceo save(Ceo ceo);
}
