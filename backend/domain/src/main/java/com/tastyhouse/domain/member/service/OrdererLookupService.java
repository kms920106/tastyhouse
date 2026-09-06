package com.tastyhouse.domain.member.service;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 주문자 조회(도메인 서비스, member 컨텍스트 소유).
 *
 * <p>주문 접수가 주문 헤더에 박제할 주문자 정보(이름·연락처·계정명)를 회원 존재 확인과 함께 돌려준다.
 *
 * <p><b>왜 member가 소유하는가</b>: 과거 {@code OrderPlacementService}가 {@code MemberRepository}와
 * {@code Member} 애그리거트를 직접 주입·import해 필드를 꺼내 썼다. 그러면 order가 회원 애그리거트의
 * 내부 구조(연락처가 VO인지 문자열인지 등)에 결합되어, member가 표현을 바꿀 때 order가 함께 깨진다.
 * 스냅샷 record만 돌려주면 order는 회원 모델을 전혀 몰라도 된다.
 *
 * <p>회원이 없으면 {@code MEMBER_NOT_FOUND}(404)로 실패한다 — 이관 전과 동일하다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code MemberDomainConfig}가 담당한다.
 */
public class OrdererLookupService {

    private final MemberRepository memberRepository;

    public OrdererLookupService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 주문자 정보를 조회한다. 회원이 없으면 {@code MEMBER_NOT_FOUND}로 실패한다.
     */
    public OrdererSnapshot findOrderer(MemberId memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return new OrdererSnapshot(
            member.getFullName(),
            member.getPhoneNumber().value(),
            member.getUsername()
        );
    }
}
