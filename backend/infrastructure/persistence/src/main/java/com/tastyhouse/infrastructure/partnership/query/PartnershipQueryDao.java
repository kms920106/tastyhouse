package com.tastyhouse.infrastructure.partnership.query;

import com.tastyhouse.application.partnership.port.out.PartnershipQueryPort;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestDetailResult;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestListItemResult;
import com.tastyhouse.application.partnership.port.out.PartnershipSearchCondition;
import com.querydsl.core.types.Projections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.partnership.model.PartnershipStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.partnership.persistence.QPartnershipRequestJpaEntity.partnershipRequestJpaEntity;

@Repository
public class PartnershipQueryDao implements PartnershipQueryPort {
    private final JPAQueryFactory queryFactory;

    public PartnershipQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
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
            .select(Projections.constructor(PartnershipRequestListItemResult.class,
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

    @Override
    public Optional<PartnershipRequestDetailResult> findDetailById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        PartnershipRequestDetailResult detail = queryFactory
            .select(Projections.constructor(PartnershipRequestDetailResult.class,
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
