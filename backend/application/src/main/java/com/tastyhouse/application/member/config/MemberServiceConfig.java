package com.tastyhouse.application.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.member.follow.port.out.write.MemberFollowPersistencePort;
import com.tastyhouse.application.member.follow.service.MemberFollowService;
import com.tastyhouse.application.member.port.out.MemberReviewCountPort;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressPersistencePort;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.member.port.out.write.MemberWithdrawalPersistencePort;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralPersistencePort;
import com.tastyhouse.application.member.referral.service.ReferralRegistrationService;
import com.tastyhouse.application.member.referral.service.ReferralRewardCompletionService;
import com.tastyhouse.application.member.service.GradeSettlementService;
import com.tastyhouse.application.member.service.MemberDeliveryAddressService;
import com.tastyhouse.application.member.service.MemberRegistrationService;
import com.tastyhouse.application.member.service.MemberWithdrawalService;
import com.tastyhouse.application.member.service.OrdererLookupService;
import com.tastyhouse.application.region.port.out.write.AdminDongPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class MemberServiceConfig {
    @Bean
    public MemberRegistrationService memberRegistrationService(
        MemberPersistencePort memberPersistencePort,
        ReferralRegistrationService referralRegistrationService,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MemberRegistrationService(memberPersistencePort, referralRegistrationService, domainEventPublisher);
    }

    @Bean
    public MemberWithdrawalService memberWithdrawalService(
        MemberPersistencePort memberPersistencePort,
        MemberWithdrawalPersistencePort memberWithdrawalPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MemberWithdrawalService(memberPersistencePort, memberWithdrawalPersistencePort, domainEventPublisher);
    }

    @Bean
    public MemberFollowService memberFollowService(
        MemberFollowPersistencePort memberFollowPersistencePort,
        MemberPersistencePort memberPersistencePort
    ) {
        return new MemberFollowService(memberFollowPersistencePort, memberPersistencePort);
    }

    @Bean
    public ReferralRegistrationService referralRegistrationService(
        MemberReferralPersistencePort memberReferralPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReferralRegistrationService(memberReferralPersistencePort, domainEventPublisher);
    }

    @Bean
    public ReferralRewardCompletionService referralRewardCompletionService(
        MemberReferralPersistencePort memberReferralPersistencePort
    ) {
        return new ReferralRewardCompletionService(memberReferralPersistencePort);
    }

    @Bean
    public GradeSettlementService gradeSettlementService(
        MemberReviewCountPort memberReviewCountPort,
        MemberPersistencePort memberPersistencePort
    ) {
        return new GradeSettlementService(memberReviewCountPort, memberPersistencePort);
    }

    @Bean
    public MemberDeliveryAddressService memberDeliveryAddressService(
        MemberDeliveryAddressPersistencePort memberDeliveryAddressPersistencePort,
        AdminDongPersistencePort adminDongPersistencePort
    ) {
        return new MemberDeliveryAddressService(memberDeliveryAddressPersistencePort, adminDongPersistencePort);
    }

    @Bean
    public OrdererLookupService ordererLookupService(MemberPersistencePort memberPersistencePort) {
        return new OrdererLookupService(memberPersistencePort);
    }
}
