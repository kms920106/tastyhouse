package com.tastyhouse.core.domain.partnership.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.core.domain.partnership.domain.model.PartnershipStatus;
import com.tastyhouse.core.domain.partnership.domain.repository.PartnershipRepository;
import com.tastyhouse.core.domain.partnership.domain.vo.PartnershipRequestId;
import com.tastyhouse.core.domain.partnership.application.dto.PartnershipSearchCondition;
import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestListItemResult;
import com.tastyhouse.core.domain.partnership.application.dto.result.QPartnershipRequestListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.partnership.domain.model.QPartnershipRequest.partnershipRequest;

@Repository
@RequiredArgsConstructor
public class PartnershipRepositoryImpl implements PartnershipRepository {

    private final JPAQueryFactory queryFactory;
    private final PartnershipRequestJpaRepository partnershipRequestJpaRepository;

    @Override
    public Optional<PartnershipRequest> findById(PartnershipRequestId partnershipRequestId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(partnershipRequest)
                .where(partnershipRequest.id.eq(partnershipRequestId.value()), partnershipRequest.deleted.isFalse())
                .fetchOne());
    }

    @Override
    public PageResult<PartnershipRequestListItemResult> findPartnershipRequests(PartnershipSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
                .select(partnershipRequest.id.count())
                .from(partnershipRequest)
                .where(
                        businessNameContains(condition.businessName()),
                        contactNameContains(condition.contactName()),
                        contactPhoneContains(condition.contactPhone()),
                        statusEq(condition.status()),
                        createdAtGoe(condition.startDate()),
                        createdAtLoe(condition.endDate()),
                        partnershipRequest.deleted.isFalse()
                )
                .fetchOne();

        List<PartnershipRequestListItemResult> items = queryFactory
                .select(new QPartnershipRequestListItemResult(
                        partnershipRequest.id,
                        partnershipRequest.businessName,
                        partnershipRequest.contactName,
                        partnershipRequest.contactPhone,
                        partnershipRequest.status,
                        partnershipRequest.consultationRequestedAt,
                        partnershipRequest.createdAt
                ))
                .from(partnershipRequest)
                .where(
                        businessNameContains(condition.businessName()),
                        contactNameContains(condition.contactName()),
                        contactPhoneContains(condition.contactPhone()),
                        statusEq(condition.status()),
                        createdAtGoe(condition.startDate()),
                        createdAtLoe(condition.endDate()),
                        partnershipRequest.deleted.isFalse()
                )
                .orderBy(partnershipRequest.createdAt.desc())
                .offset((long) pageQuery.page() * pageQuery.size())
                .limit(pageQuery.size())
                .fetch();

        return PageResult.of(items, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PartnershipRequest save(PartnershipRequest request) {
        return partnershipRequestJpaRepository.save(request);
    }

    private BooleanExpression businessNameContains(String businessName) {
        return StringUtils.hasText(businessName) ? partnershipRequest.businessName.containsIgnoreCase(businessName) : null;
    }

    private BooleanExpression contactNameContains(String contactName) {
        return StringUtils.hasText(contactName) ? partnershipRequest.contactName.containsIgnoreCase(contactName) : null;
    }

    private BooleanExpression contactPhoneContains(String contactPhone) {
        return StringUtils.hasText(contactPhone) ? partnershipRequest.contactPhone.containsIgnoreCase(contactPhone) : null;
    }

    private BooleanExpression statusEq(PartnershipStatus status) {
        return status != null ? partnershipRequest.status.eq(status) : null;
    }

    private BooleanExpression createdAtGoe(LocalDateTime startDate) {
        return startDate != null ? partnershipRequest.createdAt.goe(startDate) : null;
    }

    private BooleanExpression createdAtLoe(LocalDateTime endDate) {
        return endDate != null ? partnershipRequest.createdAt.loe(endDate) : null;
    }
}
