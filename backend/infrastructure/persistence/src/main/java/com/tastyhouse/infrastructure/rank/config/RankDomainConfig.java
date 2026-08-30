package com.tastyhouse.infrastructure.rank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.rank.port.MemberReviewCountPort;
import com.tastyhouse.domain.rank.repository.MemberReviewRankRepository;
import com.tastyhouse.domain.rank.service.RankSettlementService;

/**
 * rank 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class RankDomainConfig {

    /**
     * 랭킹 확정 — 기준일의 기존 랭킹 일괄 삭제와 신규 순위 일괄 적재를 한 트랜잭션에서 함께 처리하는 오케스트레이션.
     */
    @Bean
    public RankSettlementService rankSettlementService(
        MemberReviewRankRepository memberReviewRankRepository,
        MemberReviewCountPort memberReviewCountPort
    ) {
        return new RankSettlementService(memberReviewRankRepository, memberReviewCountPort);
    }
}
