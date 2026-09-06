package com.tastyhouse.infrastructure.rank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.rank.port.MemberReviewCountPort;
import com.tastyhouse.domain.rank.repository.MemberReviewRankRepository;
import com.tastyhouse.domain.rank.service.RankSettlementService;

@Configuration(proxyBeanMethods = false)
public class RankDomainConfig {
    @Bean
    public RankSettlementService rankSettlementService(
        MemberReviewRankRepository memberReviewRankRepository,
        MemberReviewCountPort memberReviewCountPort
    ) {
        return new RankSettlementService(memberReviewRankRepository, memberReviewCountPort);
    }
}
