package com.tastyhouse.application.rank.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.application.rank.port.in.RankMemberManagementListQueryUseCase;
import com.tastyhouse.application.rank.port.out.MemberRankResult;
import com.tastyhouse.application.rank.port.out.RankManagementQueryPort;

@Service
@Transactional(readOnly = true)
class RankMemberManagementListQueryService implements RankMemberManagementListQueryUseCase {

    private final RankManagementQueryPort rankManagementQueryPort;

    public RankMemberManagementListQueryService(RankManagementQueryPort rankManagementQueryPort) {
        this.rankManagementQueryPort = rankManagementQueryPort;
    }

    @Override
    public List<MemberRankResult> getMemberRankList(String type, int limit) {
        RankType rankType = RankType.from(type);
        LocalDate baseDate = LocalDate.now();

        return rankManagementQueryPort.findMemberRanks(rankType.name(), baseDate, limit);
    }
}
