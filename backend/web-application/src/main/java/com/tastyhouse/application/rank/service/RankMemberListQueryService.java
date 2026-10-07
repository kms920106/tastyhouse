package com.tastyhouse.application.rank.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.application.rank.port.in.RankMemberListQueryUseCase;
import com.tastyhouse.application.rank.port.out.MemberRankResult;
import com.tastyhouse.application.rank.port.out.RankQueryPort;

@Service
@Transactional(readOnly = true)
class RankMemberListQueryService implements RankMemberListQueryUseCase {

    private final RankQueryPort rankQueryPort;

    public RankMemberListQueryService(RankQueryPort rankQueryPort) {
        this.rankQueryPort = rankQueryPort;
    }

    @Override
    public List<MemberRankResult> getMemberRankList(String rankType, int limit) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();

        return rankQueryPort.findMemberRanks(type.name(), baseDate, limit);
    }

    private RankType parseRankType(String rankType) {
        try {
            return RankType.valueOf(rankType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RankType.ALL;
        }
    }
}
