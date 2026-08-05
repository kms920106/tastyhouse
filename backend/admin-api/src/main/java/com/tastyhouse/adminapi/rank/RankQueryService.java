package com.tastyhouse.adminapi.rank;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.infrastructure.rank.query.MemberRankResult;
import com.tastyhouse.infrastructure.rank.query.RankPeriodResult;
import com.tastyhouse.infrastructure.rank.query.RankPrizeManagementResult;
import com.tastyhouse.infrastructure.rank.query.RankQueryDao;
import com.tastyhouse.adminapi.file.response.FileResponse;
import com.tastyhouse.adminapi.rank.response.RankMemberListItemResponse;
import com.tastyhouse.adminapi.rank.response.RankPeriodDetailResponse;
import com.tastyhouse.adminapi.rank.response.RankPeriodListItemResponse;
import com.tastyhouse.adminapi.rank.response.RankPrizeDetailResponse;
import com.tastyhouse.adminapi.rank.response.RankPrizeListItemResponse;

/**
 * 랭킹 관리 조회 서비스(admin).
 *
 * <p>infra read 어댑터({@link RankQueryDao})만 주입해 조회하고 Response를 조립한다(패턴 2/3). 도메인
 * write 포트를 주입하지 않으므로 조회 경로가 도메인 모델을 거치지 않는다.
 *
 * <p>파일 URL은 DAO가 join으로 함께 표시용 URL까지 완성해 주므로 여기서는 값을 그대로 응답에 전달한다
 * (응답에 파일 ID 대신 URL을 노출하는 규칙).
 */
@Service
@Transactional(readOnly = true)
public class RankQueryService {

    private final RankQueryDao rankQueryDao;

    public RankQueryService(RankQueryDao rankQueryDao) {
        this.rankQueryDao = rankQueryDao;
    }

    public List<RankMemberListItemResponse> getMemberRankList(String type, int limit) {
        RankType rankType = RankType.from(type);
        LocalDate baseDate = LocalDate.now();

        return rankQueryDao.findMemberRanks(rankType, baseDate, limit).stream()
            .map(this::toMemberListItemResponse)
            .toList();
    }

    public List<RankPeriodListItemResponse> getPeriods() {
        return rankQueryDao.findAllPeriods().stream()
            .map(this::toPeriodListItemResponse)
            .toList();
    }

    public RankPeriodDetailResponse getPeriod(Long id) {
        RankPeriodResult period = rankQueryDao.findPeriodById(RankPeriodId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RANK_PERIOD_NOT_FOUND));
        return toPeriodDetailResponse(period);
    }

    public List<RankPrizeListItemResponse> getPrizesByPeriod(Long periodId) {
        return rankQueryDao.findPrizesByPeriodId(RankPeriodId.of(periodId)).stream()
            .map(this::toPrizeListItemResponse)
            .toList();
    }

    public RankPrizeDetailResponse getPrize(Long prizeId) {
        RankPrizeManagementResult prize = rankQueryDao.findPrizeById(RankPrizeId.of(prizeId))
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
