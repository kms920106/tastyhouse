package com.tastyhouse.infrastructure.ceo.query;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.ceo.persistence.QCeoJpaEntity.ceoJpaEntity;

/**
 * 점주 계정 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code CeoRepository})와 역할이 겹치지 않는다. 소비 모듈(admin-api)의
 * {@code CeoQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>인증·시드 멱등성에 쓰이는 단건 조회({@code findByUsername}/{@code existsByUsername})는 불변식
 * 검증 경로이므로 이 DAO가 아니라 write 포트에 잔류한다(README "write 포트 잔류 판정 기준").
 */
@Repository
public class CeoQueryDao {

    private final JPAQueryFactory queryFactory;

    public CeoQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 전체 점주 목록 조회 — 가게 배정용 Select 드롭다운을 채운다. 점주 조회는 관리자만 소비하므로
     * 메서드명에 admin 마커를 붙이지 않고 순수 동작명을 쓴다.
     */
    public List<CeoListItemResult> findAllCeos() {
        return queryFactory
            .select(new QCeoListItemResult(
                ceoJpaEntity.id,
                ceoJpaEntity.name,
                ceoJpaEntity.businessRegistrationNumber,
                ceoJpaEntity.status
            ))
            .from(ceoJpaEntity)
            .orderBy(ceoJpaEntity.name.asc())
            .fetch();
    }
}
