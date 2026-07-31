package com.tastyhouse.infrastructure.partnership.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.partnership.domain.model.PartnershipStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.partnership.persistence.QPartnershipRequestJpaEntity.partnershipRequestJpaEntity;

/**
 * 제휴 신청 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code PartnershipRepository})와 역할이 겹치지 않는다. 소비 모듈(admin-api)의
 * {@code PartnershipQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 제휴 신청 조회는 관리자만
 * 소비하므로(web-api는 신청 생성만 한다) 메서드명에 admin 마커를 붙이지 않고 순수 동작명을 쓴다.
 */
@Repository
@RequiredArgsConstructor
public class PartnershipQueryDao {

    private final JPAQueryFactory queryFactory;

    /**
     * 관리 목록 조회 — 상호명/담당자명/연락처 부분일치·처리상태·접수기간 필터를 적용한다.
     */
    public PageResult<PartnershipRequestListItemResult> findPartnershipRequests(PartnershipSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(partnershipRequestJpaEntity.id.count())
            .from(partnershipRequestJpaEntity)
            .where(
                businessNameContains(condition.businessName()),
                contactNameContains(condition.contactName()),
                contactPhoneContains(condition.contactPhone()),
                statusEq(condition.status()),
                createdAtGoe(condition.startDate()),
                createdAtLoe(condition.endDate()),
                partnershipRequestJpaEntity.deleted.isFalse()
            )
            .fetchOne();

        List<PartnershipRequestListItemResult> items = queryFactory
            .select(new QPartnershipRequestListItemResult(
                partnershipRequestJpaEntity.id,
                partnershipRequestJpaEntity.businessName,
                partnershipRequestJpaEntity.contactName,
                partnershipRequestJpaEntity.contactPhone,
                partnershipRequestJpaEntity.status,
                partnershipRequestJpaEntity.consultationRequestedAt,
                partnershipRequestJpaEntity.createdAt
            ))
            .from(partnershipRequestJpaEntity)
            .where(
                businessNameContains(condition.businessName()),
                contactNameContains(condition.contactName()),
                contactPhoneContains(condition.contactPhone()),
                statusEq(condition.status()),
                createdAtGoe(condition.startDate()),
                createdAtLoe(condition.endDate()),
                partnershipRequestJpaEntity.deleted.isFalse()
            )
            .orderBy(partnershipRequestJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(items, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 관리 상세 조회 — 삭제되지 않은 신청 단건을 투영한다.
     */
    public Optional<PartnershipRequestDetailResult> findDetailById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        PartnershipRequestDetailResult detail = queryFactory
            .select(new QPartnershipRequestDetailResult(
                partnershipRequestJpaEntity.id,
                partnershipRequestJpaEntity.businessName,
                partnershipRequestJpaEntity.address,
                partnershipRequestJpaEntity.addressDetail,
                partnershipRequestJpaEntity.contactName,
                partnershipRequestJpaEntity.contactPhone,
                partnershipRequestJpaEntity.status,
                partnershipRequestJpaEntity.consultationRequestedAt,
                partnershipRequestJpaEntity.createdAt,
                partnershipRequestJpaEntity.updatedAt
            ))
            .from(partnershipRequestJpaEntity)
            .where(
                partnershipRequestJpaEntity.id.eq(id),
                partnershipRequestJpaEntity.deleted.isFalse()
            )
            .fetchOne();

        return Optional.ofNullable(detail);
    }

    private BooleanExpression businessNameContains(String businessName) {
        return StringUtils.hasText(businessName) ? partnershipRequestJpaEntity.businessName.containsIgnoreCase(businessName) : null;
    }

    private BooleanExpression contactNameContains(String contactName) {
        return StringUtils.hasText(contactName) ? partnershipRequestJpaEntity.contactName.containsIgnoreCase(contactName) : null;
    }

    private BooleanExpression contactPhoneContains(String contactPhone) {
        return StringUtils.hasText(contactPhone) ? partnershipRequestJpaEntity.contactPhone.containsIgnoreCase(contactPhone) : null;
    }

    private BooleanExpression statusEq(PartnershipStatus status) {
        return status != null ? partnershipRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression createdAtGoe(LocalDateTime startDate) {
        return startDate != null ? partnershipRequestJpaEntity.createdAt.goe(startDate) : null;
    }

    private BooleanExpression createdAtLoe(LocalDateTime endDate) {
        return endDate != null ? partnershipRequestJpaEntity.createdAt.loe(endDate) : null;
    }
}
