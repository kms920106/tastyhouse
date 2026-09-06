package com.tastyhouse.application.rank.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.rank.port.out.MemberRankResult;
import com.tastyhouse.application.rank.port.out.RankPeriodResult;
import com.tastyhouse.application.rank.port.out.RankPrizeManagementResult;
import com.tastyhouse.application.rank.port.out.RankManagementQueryPort;
import com.tastyhouse.application.rank.port.in.RankManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class RankManagementQueryService implements RankManagementQueryUseCase {

    private final RankManagementQueryPort rankManagementQueryPort;

    public RankManagementQueryService(RankManagementQueryPort rankManagementQueryPort) {
        this.rankManagementQueryPort = rankManagementQueryPort;
    }

    @Override
    public List<MemberRankResult> getMemberRankList(String type, int limit) {
        RankType rankType = RankType.from(type);
        LocalDate baseDate = LocalDate.now();

        return rankManagementQueryPort.findMemberRanks(rankType, baseDate, limit);
    }

    @Override
    public List<RankPeriodResult> getPeriods() {
        return rankManagementQueryPort.findAllPeriods();
    }

    @Override
    public RankPeriodResult getPeriod(Long id) {
        return rankManagementQueryPort.findPeriodById(RankPeriodId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RANK_PERIOD_NOT_FOUND));
    }

    @Override
    public List<RankPrizeManagementResult> getPrizesByPeriod(Long periodId) {
        return rankManagementQueryPort.findPrizesByPeriodId(RankPeriodId.of(periodId));
    }

    @Override
    public RankPrizeManagementResult getPrize(Long prizeId) {
        return rankManagementQueryPort.findPrizeById(RankPrizeId.of(prizeId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RANK_PRIZE_NOT_FOUND));
    }
}
