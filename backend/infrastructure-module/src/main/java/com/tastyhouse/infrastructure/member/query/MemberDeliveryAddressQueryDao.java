package com.tastyhouse.infrastructure.member.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;

import static com.tastyhouse.infrastructure.member.persistence.QMemberDeliveryAddressJpaEntity.memberDeliveryAddressJpaEntity;
import static com.tastyhouse.infrastructure.region.persistence.QAdminDongJpaEntity.adminDongJpaEntity;

/**
 * 회원 배달 주소록 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code MemberDeliveryAddressRepository})와 역할이 겹치지 않는다. 소비 모듈(web-api)의
 * {@code MemberDeliveryAddressQueryService}가 이 DAO를 주입하므로 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>행정동은 <b>left join</b>이다 — 주소 문자열 매칭에 실패해 {@code admin_dong_id}가 null인 주소도
 * 목록에서 빠지면 안 되기 때문이다. 그 경우 {@code regionName}은 null로 내려간다.
 */
@Repository
public class MemberDeliveryAddressQueryDao {

    private final JPAQueryFactory queryFactory;

    public MemberDeliveryAddressQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 회원의 배달 주소 목록. 기본 배송지를 먼저 노출하고 그다음은 등록 순으로 정렬한다.
     */
    public List<MemberDeliveryAddressItemResult> findByMemberId(MemberId memberId) {
        return queryFactory
            .select(new QMemberDeliveryAddressItemResult(
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

    /**
     * 회원 기본 배송지의 행정동 식별자.
     *
     * <p>가게 목록·검색이 "이 회원에게 배달되는 가게만" 남기려고 쓰는 값이라 주소 전체가 필요 없다.
     * 목록 조회 경로마다 도는 질의이므로 컬럼 하나만 투영한다.
     *
     * <p>기본 배송지가 없거나, 있어도 주소 문자열 매칭에 실패해 {@code admin_dong_id}가 null이면
     * 비어 있다 — 호출부는 그 경우 필터를 걸지 않는다.
     */
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

    /**
     * 표시용 행정동 전체 이름({@code "서울특별시 강남구 역삼1동"}) 투영식. 도메인 모델
     * {@code AdminDong#fullName()}과 같은 규칙(공백 하나 join)이며, 프론트가 조립하지 않도록 서버가
     * 완성해 내려준다. left join이 비면 concat 결과 전체가 null이 되어 Result의 {@code regionName}도
     * null이 된다.
     */
    private StringExpression regionNameExpression() {
        return adminDongJpaEntity.sidoName
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.sigunguName)
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.dongName);
    }
}
