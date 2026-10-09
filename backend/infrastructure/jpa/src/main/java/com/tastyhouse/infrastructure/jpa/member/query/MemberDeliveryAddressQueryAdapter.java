package com.tastyhouse.infrastructure.jpa.member.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.member.port.out.MemberDeliveryAddressItemResult;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;

import static com.tastyhouse.infrastructure.jpa.member.persistence.QMemberDeliveryAddressJpaEntity.memberDeliveryAddressJpaEntity;
import static com.tastyhouse.infrastructure.jpa.region.persistence.QAdminDongJpaEntity.adminDongJpaEntity;

@Repository
class MemberDeliveryAddressQueryAdapter implements MemberDeliveryAddressQueryPort {

    private final JPAQueryFactory queryFactory;

    public MemberDeliveryAddressQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<MemberDeliveryAddressItemResult> findByMemberId(Long memberId) {
        return queryFactory
            .select(Projections.constructor(MemberDeliveryAddressItemResult.class,
                memberDeliveryAddressJpaEntity.id,
                memberDeliveryAddressJpaEntity.alias,
                memberDeliveryAddressJpaEntity.roadAddress,
                memberDeliveryAddressJpaEntity.lotAddress,
                memberDeliveryAddressJpaEntity.detailAddress,
                memberDeliveryAddressJpaEntity.adminDongId,
                regionNameExpression(),
                memberDeliveryAddressJpaEntity.latitude,
                memberDeliveryAddressJpaEntity.longitude,
                memberDeliveryAddressJpaEntity.defaultAddress
            ))
            .from(memberDeliveryAddressJpaEntity)
            .leftJoin(adminDongJpaEntity)
            .on(memberDeliveryAddressJpaEntity.adminDongId.eq(adminDongJpaEntity.id))
            .where(memberDeliveryAddressJpaEntity.memberId.eq(memberId))
            .orderBy(memberDeliveryAddressJpaEntity.defaultAddress.desc(), memberDeliveryAddressJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public Optional<Long> findDefaultAdminDongId(Long memberId) {
        Long result = queryFactory
            .select(memberDeliveryAddressJpaEntity.adminDongId)
            .from(memberDeliveryAddressJpaEntity)
            .where(
                memberDeliveryAddressJpaEntity.memberId.eq(memberId),
                memberDeliveryAddressJpaEntity.defaultAddress.isTrue(),
                memberDeliveryAddressJpaEntity.adminDongId.isNotNull()
            )
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    private StringExpression regionNameExpression() {
        return adminDongJpaEntity.sidoName
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.sigunguName)
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.dongName);
    }
}
