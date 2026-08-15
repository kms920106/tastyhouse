package com.tastyhouse.infrastructure.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.member.follow.repository.MemberFollowRepository;
import com.tastyhouse.domain.member.follow.service.MemberFollowService;
import com.tastyhouse.domain.member.referral.repository.MemberReferralRepository;
import com.tastyhouse.domain.member.referral.service.ReferralRegistrationService;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.repository.MemberWithdrawalRepository;
import com.tastyhouse.domain.member.service.MemberDeliveryAddressService;
import com.tastyhouse.domain.member.service.MemberRegistrationService;
import com.tastyhouse.domain.member.service.MemberWithdrawalService;
import com.tastyhouse.domain.point.repository.PointHistoryRepository;
import com.tastyhouse.domain.point.repository.PointRepository;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * member 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>member의 하위 컨텍스트(follow·referral) 빈도 별도 파일로 쪼개지 않고 여기에 함께 둔다.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class MemberDomainConfig {

    /**
     * 회원 등록 — 회원 애그리거트를 저장하고, 추천인이 지정되면 추천 관계까지 함께 만드는 오케스트레이션.
     */
    @Bean
    public MemberRegistrationService memberRegistrationService(
        MemberRepository memberRepository,
        ReferralRegistrationService referralRegistrationService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MemberRegistrationService(memberRepository, referralRegistrationService, domainEventPublisher);
    }

    /**
     * 회원 탈퇴 — 회원 상태 전이와 탈퇴 사유 기록을 한 트랜잭션에서 함께 처리하는 오케스트레이션.
     */
    @Bean
    public MemberWithdrawalService memberWithdrawalService(
        MemberRepository memberRepository,
        MemberWithdrawalRepository memberWithdrawalRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MemberWithdrawalService(memberRepository, memberWithdrawalRepository, domainEventPublisher);
    }

    /**
     * 팔로우 등록·해제 — 팔로우 대상 회원의 존재를 확인해야 하는 크로스 애그리거트 규칙.
     */
    @Bean
    public MemberFollowService memberFollowService(
        MemberFollowRepository memberFollowRepository,
        MemberRepository memberRepository
    ) {
        return new MemberFollowService(memberFollowRepository, memberRepository);
    }

    /**
     * 추천인 등록 — 추천 관계 생성과 추천인·피추천인 양쪽 포인트 보상 적립을 함께 처리하는 오케스트레이션.
     */
    @Bean
    public ReferralRegistrationService referralRegistrationService(
        MemberReferralRepository memberReferralRepository,
        PointRepository pointRepository,
        PointHistoryRepository pointHistoryRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReferralRegistrationService(
            memberReferralRepository,
            pointRepository,
            pointHistoryRepository,
            domainEventPublisher
        );
    }

    /**
     * 회원 배달 주소록 불변식 — 기본 배송지 유일성, 회원당 10건 한도, 소유권 검증,
     * 주소 문자열의 행정동 매칭을 담당한다.
     */
    @Bean
    public MemberDeliveryAddressService memberDeliveryAddressService(
        MemberDeliveryAddressRepository memberDeliveryAddressRepository,
        AdminDongRepository adminDongRepository
    ) {
        return new MemberDeliveryAddressService(memberDeliveryAddressRepository, adminDongRepository);
    }
}
