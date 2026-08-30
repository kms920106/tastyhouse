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
import com.tastyhouse.application.rank.port.out.RankQueryPort;
import com.tastyhouse.adminapplication.file.response.FileResponse;
import com.tastyhouse.adminapplication.rank.response.RankMemberListItemResponse;
import com.tastyhouse.adminapplication.rank.response.RankPeriodDetailResponse;
import com.tastyhouse.adminapplication.rank.response.RankPeriodListItemResponse;
import com.tastyhouse.adminapplication.rank.response.RankPrizeDetailResponse;
import com.tastyhouse.adminapplication.rank.response.RankPrizeListItemResponse;
import com.tastyhouse.adminapplication.rank.port.in.RankQueryUseCase;

/**
 * 랭킹 관리 조회 서비스(admin).
 *
 * <p>읽기 포트({@link RankQueryPort})만 주입해 조회하고 Response를 조립한다(패턴 2/3). 도메인
 * write 포트를 주입하지 않으므로 조회 경로가 도메인 모델을 거치지 않는다.
 *
 * <p>파일 URL은 DAO가 join으로 함께 표시용 URL까지 완성해 주므로 여기서는 값을 그대로 응답에 전달한다
 * (응답에 파일 ID 대신 URL을 노출하는 규칙).
 */
@Service
@Transactional(readOnly = true)
public class RankQueryService implements RankQueryUseCase {

    private final RankQueryPort rankQueryPort;

    public RankQueryService(RankQueryPort rankQueryPort) {
        this.rankQueryPort = rankQueryPort;
    }

    @Override
    public List<RankMemberListItemResponse> getMemberRankList(String type, int limit) {
        RankType rankType = RankType.from(type);
        LocalDate baseDate = LocalDate.now();

        return rankQueryPort.findMemberRanks(rankType, baseDate, limit).stream()
            .map(this::toMemberListItemResponse)
            .toList();
    }

    @Override
    public List<RankPeriodListItemResponse> getPeriods() {
        return rankQueryPort.findAllPeriods().stream()
            .map(this::toPeriodListItemResponse)
            .toList();
    }

    @Override
    public RankPeriodDetailResponse getPeriod(Long id) {
        RankPeriodResult period = rankQueryPort.findPeriodById(RankPeriodId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RANK_PERIOD_NOT_FOUND));
        return toPeriodDetailResponse(period);
    }

    @Override
    public List<RankPrizeListItemResponse> getPrizesByPeriod(Long periodId) {
        return rankQueryPort.findPrizesByPeriodId(RankPeriodId.of(periodId)).stream()
            .map(this::toPrizeListItemResponse)
            .toList();
    }

    @Override
    public RankPrizeDetailResponse getPrize(Long prizeId) {
        RankPrizeManagementResult prize = rankQueryPort.findPrizeById(RankPrizeId.of(prizeId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RANK_PRIZE_NOT_FOUND));
        return toPrizeDetailResponse(prize);
    }

    private RankMemberListItemResponse toMemberListItemResponse(MemberRankResult dto) {
        return RankMemberListItemResponse.of(
            dto.memberId(),
            dto.nickname(),
            dto.profileImageUrl(),
            dto.reviewCount(),
            dto.rankNo(),
            dto.grade().name()
        );
    }

    private RankPeriodListItemResponse toPeriodListItemResponse(RankPeriodResult dto) {
        return RankPeriodListItemResponse.from(
            dto.id(),
            dto.startAt(),
            dto.endAt(),
            dto.visible()
        );
    }

    private RankPeriodDetailResponse toPeriodDetailResponse(RankPeriodResult dto) {
        return RankPeriodDetailResponse.from(
            dto.id(),
            dto.startAt(),
            dto.endAt(),
            dto.visible(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }

    private RankPrizeListItemResponse toPrizeListItemResponse(RankPrizeManagementResult dto) {
        return RankPrizeListItemResponse.from(
            dto.id(),
            dto.prizeRank(),
            dto.name(),
            dto.brand(),
            toFileResponse(dto)
        );
    }

    private RankPrizeDetailResponse toPrizeDetailResponse(RankPrizeManagementResult dto) {
        return RankPrizeDetailResponse.from(
            dto.id(),
            dto.periodId(),
            dto.prizeRank(),
            dto.name(),
            dto.brand(),
            toFileResponse(dto)
        );
    }

    private FileResponse toFileResponse(RankPrizeManagementResult dto) {
        if (dto.imageFileId() == null) {
            return null;
        }
        return FileResponse.of(dto.imageFileId(), dto.imageFileName(), dto.imageUrl());
    }
}
