package com.tastyhouse.infrastructure.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.member.follow.repository.MemberFollowRepository;
import com.tastyhouse.domain.member.follow.service.MemberFollowService;
import com.tastyhouse.domain.member.port.MemberReviewCountPort;
import com.tastyhouse.domain.member.referral.repository.MemberReferralRepository;
import com.tastyhouse.domain.member.referral.service.ReferralRegistrationService;
import com.tastyhouse.domain.member.referral.service.ReferralRewardCompletionService;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.repository.MemberWithdrawalRepository;
import com.tastyhouse.domain.member.service.GradeSettlementService;
import com.tastyhouse.domain.member.service.MemberDeliveryAddressService;
import com.tastyhouse.domain.member.service.MemberRegistrationService;
import com.tastyhouse.domain.member.service.MemberWithdrawalService;
import com.tastyhouse.domain.member.service.OrdererLookupService;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

@Configuration(proxyBeanMethods = false)
public class MemberDomainConfig {
    @Bean
    public MemberRegistrationService memberRegistrationService(
        MemberRepository memberRepository,
        ReferralRegistrationService referralRegistrationService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MemberRegistrationService(memberRepository, referralRegistrationService, domainEventPublisher);
    }

    @Bean
    public MemberWithdrawalService memberWithdrawalService(
        MemberRepository memberRepository,
        MemberWithdrawalRepository memberWithdrawalRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MemberWithdrawalService(memberRepository, memberWithdrawalRepository, domainEventPublisher);
    }

    @Bean
    public MemberFollowService memberFollowService(
        MemberFollowRepository memberFollowRepository,
        MemberRepository memberRepository
    ) {
        return new MemberFollowService(memberFollowRepository, memberRepository);
    }

    @Bean
    public ReferralRegistrationService referralRegistrationService(
        MemberReferralRepository memberReferralRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReferralRegistrationService(memberReferralRepository, domainEventPublisher);
    }

    @Bean
    public ReferralRewardCompletionService referralRewardCompletionService(
        MemberReferralRepository memberReferralRepository
    ) {
        return new ReferralRewardCompletionService(memberReferralRepository);
    }

    @Bean
    public GradeSettlementService gradeSettlementService(
        MemberReviewCountPort memberReviewCountPort,
        MemberRepository memberRepository
    ) {
        return new GradeSettlementService(memberReviewCountPort, memberRepository);
    }

    @Bean
    public MemberDeliveryAddressService memberDeliveryAddressService(
        MemberDeliveryAddressRepository memberDeliveryAddressRepository,
        AdminDongRepository adminDongRepository
    ) {
        return new MemberDeliveryAddressService(memberDeliveryAddressRepository, adminDongRepository);
    }

    @Bean
    public OrdererLookupService ordererLookupService(MemberRepository memberRepository) {
        return new OrdererLookupService(memberRepository);
    }
}
