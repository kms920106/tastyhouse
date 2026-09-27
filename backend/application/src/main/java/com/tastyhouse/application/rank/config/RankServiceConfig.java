package com.tastyhouse.application.rank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.rank.port.out.MemberReviewCountPort;
import com.tastyhouse.application.rank.port.out.write.MemberReviewRankStatePort;
import com.tastyhouse.application.rank.port.out.write.RankPeriodStatePort;
import com.tastyhouse.application.rank.port.out.write.RankPrizeStatePort;
import com.tastyhouse.application.rank.service.RankSettlementService;
import com.tastyhouse.application.rank.store.MemberReviewRankRepository;
import com.tastyhouse.application.rank.store.MemberReviewRankStore;
import com.tastyhouse.application.rank.store.RankPeriodRepository;
import com.tastyhouse.application.rank.store.RankPeriodStore;
import com.tastyhouse.application.rank.store.RankPrizeRepository;
import com.tastyhouse.application.rank.store.RankPrizeStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class RankServiceConfig {
    @Bean
    public MemberReviewRankRepository memberReviewRankRepository(MemberReviewRankStatePort memberReviewRankStatePort) {
        return new MemberReviewRankStore(memberReviewRankStatePort);
    }

    @Bean
    public RankPeriodRepository rankPeriodRepository(RankPeriodStatePort rankPeriodStatePort) {
        return new RankPeriodStore(rankPeriodStatePort);
    }

    @Bean
    public RankPrizeRepository rankPrizeRepository(RankPrizeStatePort rankPrizeStatePort) {
        return new RankPrizeStore(rankPrizeStatePort);
    }

    @Bean
    public RankSettlementService rankSettlementService(
        MemberReviewRankRepository memberReviewRankRepository,
        MemberReviewCountPort memberReviewCountPort
    ) {
        return new RankSettlementService(memberReviewRankRepository, memberReviewCountPort);
    }
}
