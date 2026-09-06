package com.tastyhouse.infrastructure.member.query;

import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressItemResult;
import com.querydsl.core.types.Projections;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;

import static com.tastyhouse.infrastructure.member.persistence.QMemberDeliveryAddressJpaEntity.memberDeliveryAddressJpaEntity;
import static com.tastyhouse.infrastructure.region.persistence.QAdminDongJpaEntity.adminDongJpaEntity;

@Repository
public class MemberDeliveryAddressQueryDao implements MemberDeliveryAddressQueryPort {
    private final JPAQueryFactory queryFactory;

    public MemberDeliveryAddressQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<MemberDeliveryAddressItemResult> findByMemberId(MemberId memberId) {
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
            .where(memberDeliveryAddressJpaEntity.memberId.eq(memberId.value()))
            .orderBy(memberDeliveryAddressJpaEntity.defaultAddress.desc(), memberDeliveryAddressJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public Optional<Long> findDefaultAdminDongId(MemberId memberId) {
        return Optional.ofNullable(queryFactory
            .select(memberDeliveryAddressJpaEntity.adminDongId)
            .from(memberDeliveryAddressJpaEntity)
            .where(
                memberDeliveryAddressJpaEntity.memberId.eq(memberId.value()),
                memberDeliveryAddressJpaEntity.defaultAddress.isTrue(),
                memberDeliveryAddressJpaEntity.adminDongId.isNotNull()
            )
            .fetchFirst());
    }

    private StringExpression regionNameExpression() {
        return adminDongJpaEntity.sidoName
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.sigunguName)
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.dongName);
    }
}
