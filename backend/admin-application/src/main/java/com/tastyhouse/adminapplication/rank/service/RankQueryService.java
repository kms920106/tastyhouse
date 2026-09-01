package com.tastyhouse.adminapplication.rank.service;

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
import com.tastyhouse.adminapplication.rank.port.in.RankQueryUseCase;

/**
 * 랭킹 관리 조회 서비스(admin).
 *
 * <p>읽기 포트({@link RankManagementQueryPort})만 주입해 조회한다(패턴 2/3). 도메인 write 포트를
 * 주입하지 않으므로 조회 경로가 도메인 모델을 거치지 않는다.
 *
 * <p>파일 URL은 DAO가 join으로 함께 표시용 URL까지 완성해 주므로 여기서는 파일을 다시 조회하지 않는다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·{@code FileResponse}) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class RankQueryService implements RankQueryUseCase {

    private final RankManagementQueryPort rankManagementQueryPort;

    public RankQueryService(RankManagementQueryPort rankManagementQueryPort) {
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
