package com.tastyhouse.ceoapi.ceo;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.ceo.domain.model.Ceo;
import com.tastyhouse.core.domain.ceo.domain.repository.CeoRepository;

/**
 * 점주 계정 조회 서비스.
 *
 * <p>ceo-api가 필요한 조회는 인증(UserDetails 로드·토큰 갱신 시 계정 상태 재검증)과 시드
 * 멱등성 확인뿐이라 표현 목적 read model이 없다. 따라서 infra query DAO를 두지 않고 domain
 * write 포트({@link CeoRepository})를 그대로 주입한다 — README의 "write 포트 잔류 판정 기준"에
 * 해당한다(엔티티/원시값 반환 + 불변식 검증 경로). 표현용 목록 조회는 admin-api의
 * {@code CeoQueryService}가 infra {@code CeoQueryDao}로 수행한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CeoQueryService {

    private final CeoRepository ceoRepository;

    /**
     * 인증(UserDetails 로드·토큰 갱신 시 계정 상태 재검증)용 단건 조회.
     */
    public Optional<Ceo> findByUsername(String username) {
        return ceoRepository.findByUsername(username);
    }

    /**
     * 최초 점주 시드 멱등성 확인용 존재 검증.
     */
    public boolean existsByUsername(String username) {
        return ceoRepository.existsByUsername(username);
    }
}
