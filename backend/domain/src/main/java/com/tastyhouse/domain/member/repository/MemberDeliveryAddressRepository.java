package com.tastyhouse.domain.member.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;

/**
 * 회원 배달 주소록 write 포트.
 *
 * <p>여기 남는 조회는 전부 도메인 서비스가 불변식(기본 배송지 유일성·10건 한도·소유권)을 검증하는 데
 * 필요한 것이므로 write 포트 잔류 기준을 만족한다. 표현용 목록 조회(행정동명 조인 포함)는
 * infrastructure-module의 {@code member/query/MemberDeliveryAddressQueryDao}가 별도로 담당한다
 * (CQRS 교차 주입 금지).
 */
public interface MemberDeliveryAddressRepository {

    Optional<MemberDeliveryAddress> findById(Long addressId);

    List<MemberDeliveryAddress> findByMemberId(MemberId memberId);

    /** 회원당 등록 한도 검증에 쓴다. */
    long countByMemberId(MemberId memberId);

    /** 기본 배송지 유일성 검증에 쓴다 — 새 기본을 지정하기 전에 기존 기본을 찾아 해제한다. */
    Optional<MemberDeliveryAddress> findDefaultByMemberId(MemberId memberId);

    MemberDeliveryAddress save(MemberDeliveryAddress memberDeliveryAddress);

    void deleteById(Long addressId);
}
