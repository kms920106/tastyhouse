package com.tastyhouse.application.rank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.rank.port.out.MemberReviewCountPort;
import com.tastyhouse.application.rank.port.out.write.MemberReviewRankPersistencePort;
import com.tastyhouse.application.rank.service.RankSettlementService;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class RankServiceConfig {
    @Bean
    public RankSettlementService rankSettlementService(
        MemberReviewRankPersistencePort memberReviewRankPersistencePort,
        MemberReviewCountPort memberReviewCountPort
    ) {
        return new RankSettlementService(memberReviewRankPersistencePort, memberReviewCountPort);
    }
}
