package com.tastyhouse.infrastructure.partnership.persistence;

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

import static com.tastyhouse.infrastructure.partnership.persistence.QPartnershipRequestJpaEntity.partnershipRequestJpaEntity;

@Repository
@RequiredArgsConstructor
public class PartnershipRepositoryImpl implements PartnershipRepository {

    private final JPAQueryFactory queryFactory;
    private final PartnershipRequestJpaRepository partnershipRequestJpaRepository;

    @Override
    public Optional<PartnershipRequest> findById(PartnershipRequestId partnershipRequestId) {
        PartnershipRequestJpaEntity entity = queryFactory
            .selectFrom(partnershipRequestJpaEntity)
            .where(partnershipRequestJpaEntity.id.eq(partnershipRequestId.value()), partnershipRequestJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(PartnershipRequestMapper::toDomain);
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

    @Override
    public PartnershipRequest save(PartnershipRequest partnershipRequest) {
        if (partnershipRequest.getId() == null) {
            PartnershipRequestJpaEntity saved = partnershipRequestJpaRepository.save(PartnershipRequestMapper.toEntity(partnershipRequest));
            return PartnershipRequestMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        // soft delete 갱신이 동작하도록 deleted 필터가 없는 순수 PK 조회를 사용한다.
        PartnershipRequestJpaEntity entity = partnershipRequestJpaRepository.findById(partnershipRequest.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 제휴 문의입니다: " + partnershipRequest.getId()));
        PartnershipRequestMapper.applyChanges(entity, partnershipRequest);
        return PartnershipRequestMapper.toDomain(entity);
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
