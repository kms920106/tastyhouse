package com.tastyhouse.application.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.member.follow.port.out.write.MemberFollowRepository;
import com.tastyhouse.application.member.follow.service.MemberFollowService;
import com.tastyhouse.application.member.port.out.MemberReviewCountPort;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressRepository;
import com.tastyhouse.application.member.port.out.write.MemberRepository;
import com.tastyhouse.application.member.port.out.write.MemberWithdrawalRepository;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralRepository;
import com.tastyhouse.application.member.referral.service.ReferralRegistrationService;
import com.tastyhouse.application.member.referral.service.ReferralRewardCompletionService;
import com.tastyhouse.application.member.service.GradeSettlementService;
import com.tastyhouse.application.member.service.MemberDeliveryAddressService;
import com.tastyhouse.application.member.service.MemberRegistrationService;
import com.tastyhouse.application.member.service.MemberWithdrawalService;
import com.tastyhouse.application.member.service.OrdererLookupService;
import com.tastyhouse.application.region.port.out.write.AdminDongRepository;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class MemberServiceConfig {
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
