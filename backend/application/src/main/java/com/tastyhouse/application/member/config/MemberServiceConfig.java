package com.tastyhouse.application.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.member.follow.port.out.write.MemberFollowStatePort;
import com.tastyhouse.application.member.follow.service.MemberFollowService;
import com.tastyhouse.application.member.follow.store.MemberFollowRepository;
import com.tastyhouse.application.member.follow.store.MemberFollowStore;
import com.tastyhouse.application.member.port.out.MemberReviewCountPort;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressStatePort;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountStatePort;
import com.tastyhouse.application.member.port.out.write.MemberStatePort;
import com.tastyhouse.application.member.port.out.write.MemberWithdrawalStatePort;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralStatePort;
import com.tastyhouse.application.member.referral.store.MemberReferralRepository;
import com.tastyhouse.application.member.referral.store.MemberReferralStore;
import com.tastyhouse.application.member.referral.service.ReferralRegistrationService;
import com.tastyhouse.application.member.referral.service.ReferralRewardCompletionService;
import com.tastyhouse.application.member.service.GradeSettlementService;
import com.tastyhouse.application.member.service.MemberDeliveryAddressService;
import com.tastyhouse.application.member.service.MemberRegistrationService;
import com.tastyhouse.application.member.service.MemberWithdrawalService;
import com.tastyhouse.application.member.service.OrdererLookupService;
import com.tastyhouse.application.member.store.MemberDeliveryAddressRepository;
import com.tastyhouse.application.member.store.MemberDeliveryAddressStore;
import com.tastyhouse.application.member.store.MemberRepository;
import com.tastyhouse.application.member.store.MemberSocialAccountRepository;
import com.tastyhouse.application.member.store.MemberSocialAccountStore;
import com.tastyhouse.application.member.store.MemberStore;
import com.tastyhouse.application.member.store.MemberWithdrawalRepository;
import com.tastyhouse.application.member.store.MemberWithdrawalStore;
import com.tastyhouse.application.region.store.AdminDongRepository;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class MemberServiceConfig {
    @Bean
    public MemberRepository memberRepository(MemberStatePort memberStatePort) {
        return new MemberStore(memberStatePort);
    }

    @Bean
    public MemberDeliveryAddressRepository memberDeliveryAddressRepository(
        MemberDeliveryAddressStatePort memberDeliveryAddressStatePort
    ) {
        return new MemberDeliveryAddressStore(memberDeliveryAddressStatePort);
    }

    @Bean
    public MemberSocialAccountRepository memberSocialAccountRepository(
        MemberSocialAccountStatePort memberSocialAccountStatePort
    ) {
        return new MemberSocialAccountStore(memberSocialAccountStatePort);
    }

    @Bean
    public MemberWithdrawalRepository memberWithdrawalRepository(MemberWithdrawalStatePort memberWithdrawalStatePort) {
        return new MemberWithdrawalStore(memberWithdrawalStatePort);
    }

    @Bean
    public MemberFollowRepository memberFollowRepository(MemberFollowStatePort memberFollowStatePort) {
        return new MemberFollowStore(memberFollowStatePort);
    }

    @Bean
    public MemberReferralRepository memberReferralRepository(MemberReferralStatePort memberReferralStatePort) {
        return new MemberReferralStore(memberReferralStatePort);
    }

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
