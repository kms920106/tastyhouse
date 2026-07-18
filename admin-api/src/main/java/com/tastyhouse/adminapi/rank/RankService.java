package com.tastyhouse.adminapi.rank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.core.domain.rank.domain.vo.RankPrizeId;
import com.tastyhouse.core.domain.rank.application.RankCommandService;
import com.tastyhouse.core.domain.rank.application.RankQueryService;
import com.tastyhouse.core.domain.rank.application.dto.command.RankPeriodCreateCommand;
import com.tastyhouse.core.domain.rank.application.dto.command.RankPeriodUpdateCommand;
import com.tastyhouse.core.domain.rank.application.dto.command.RankPrizeCreateCommand;
import com.tastyhouse.core.domain.rank.application.dto.command.RankPrizeUpdateCommand;
import com.tastyhouse.core.domain.rank.application.dto.result.MemberRankResult;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPeriodResult;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPrizeManagementResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.adminapi.common.FileResponse;
import com.tastyhouse.adminapi.rank.response.RankMemberListItemResponse;
import com.tastyhouse.adminapi.rank.response.RankPeriodDetailResponse;
import com.tastyhouse.adminapi.rank.response.RankPeriodListItemResponse;
import com.tastyhouse.adminapi.rank.response.RankPrizeDetailResponse;
import com.tastyhouse.adminapi.rank.response.RankPrizeListItemResponse;

@Service
@RequiredArgsConstructor
public class RankService {

    private static final int DEFAULT_AGGREGATE_LIMIT = 10;

    private final RankCommandService rankCommandService;
    private final RankQueryService rankQueryService;
    private final FileService fileService;

    public List<RankMemberListItemResponse> getMemberRankList(String type, int limit) {
        RankType rankType = RankType.from(type);
        LocalDate baseDate = LocalDate.now();

        List<MemberRankResult> ranks = rankQueryService.searchMemberRankList(rankType, baseDate, limit);

        return ranks.stream()
            .map(this::toMemberListItemResponse)
            .toList();
    }

    private RankMemberListItemResponse toMemberListItemResponse(MemberRankResult dto) {
        return RankMemberListItemResponse.of(
            dto.memberId().value(),
            dto.nickname(),
            fileService.getUrlByPath(dto.profileImageUrl()),
            dto.reviewCount(),
            dto.rankNo(),
            dto.grade().name()
        );
    }

    public void aggregate(String type, LocalDate baseDate, Integer limit) {
        if (type == null) {
            rankCommandService.aggregateAllRanks();
            return;
        }

        RankType rankType = RankType.from(type);
        LocalDate targetDate = baseDate != null ? baseDate : LocalDate.now();
        int targetLimit = limit != null ? limit : DEFAULT_AGGREGATE_LIMIT;
        rankCommandService.aggregateRankByType(rankType, targetDate, targetLimit);
    }

    public List<RankPeriodListItemResponse> getPeriods() {
        return rankQueryService.findAllPeriods().stream()
            .map(this::toPeriodListItemResponse)
            .toList();
    }

    private RankPeriodListItemResponse toPeriodListItemResponse(RankPeriodResult dto) {
        return RankPeriodListItemResponse.from(dto.id(), dto.startAt(), dto.endAt(), dto.visible());
    }

    public Long createPeriod(LocalDateTime startAt, LocalDateTime endAt, boolean visible) {
        RankPeriodCreateCommand command = RankPeriodCreateCommand.of(startAt, endAt, visible);
        RankPeriodId periodId = rankCommandService.createPeriod(command);
        return periodId.value();
    }

    public RankPeriodDetailResponse getPeriod(Long id) {
        RankPeriodId periodId = RankPeriodId.of(id);
        RankPeriodResult period = rankQueryService.findPeriod(periodId);
        return RankPeriodDetailResponse.from(
            period.id(),
            period.startAt(),
            period.endAt(),
            period.visible(),
            period.createdAt(),
            period.updatedAt()
        );
    }

    public void updatePeriod(Long id, LocalDateTime startAt, LocalDateTime endAt, boolean visible) {
        RankPeriodId periodId = RankPeriodId.of(id);
        RankPeriodUpdateCommand command = RankPeriodUpdateCommand.of(startAt, endAt, visible);
        rankCommandService.updatePeriod(periodId, command);
    }

    public void deletePeriod(Long id) {
        RankPeriodId periodId = RankPeriodId.of(id);
        rankCommandService.deletePeriod(periodId);
    }

    public List<RankPrizeListItemResponse> getPrizesByPeriod(Long periodId) {
        RankPeriodId id = RankPeriodId.of(periodId);
        return rankQueryService.findPrizesByPeriod(id).stream()
            .map(this::toPrizeListItemResponse)
            .toList();
    }

    private RankPrizeListItemResponse toPrizeListItemResponse(RankPrizeManagementResult dto) {
        return RankPrizeListItemResponse.from(dto.id(), dto.prizeRank(), dto.name(), dto.brand(), toFileResponse(dto));
    }

    public Long createPrize(Long periodId, Integer prizeRank, String name, String brand, Long imageFileId) {
        RankPeriodId id = RankPeriodId.of(periodId);
        RankPrizeCreateCommand command = RankPrizeCreateCommand.of(prizeRank, name, brand, imageFileId);
        RankPrizeId prizeId = rankCommandService.createPrize(id, command);
        return prizeId.value();
    }

    public RankPrizeDetailResponse getPrize(Long prizeId) {
        RankPrizeId id = RankPrizeId.of(prizeId);
        RankPrizeManagementResult prize = rankQueryService.findPrize(id);
        return RankPrizeDetailResponse.from(
            prize.id(),
            prize.periodId(),
            prize.prizeRank(),
            prize.name(),
            prize.brand(),
            toFileResponse(prize)
        );
    }

    public void updatePrize(Long prizeId, Integer prizeRank, String name, String brand, Long imageFileId) {
        RankPrizeId id = RankPrizeId.of(prizeId);
        RankPrizeUpdateCommand command = RankPrizeUpdateCommand.of(prizeRank, name, brand, imageFileId);
        rankCommandService.updatePrize(id, command);
    }

    public void deletePrize(Long prizeId) {
        RankPrizeId id = RankPrizeId.of(prizeId);
        rankCommandService.deletePrize(id);
    }

    private FileResponse toFileResponse(RankPrizeManagementResult dto) {
        if (dto.imageFileId() == null) {
            return null;
        }
        return FileResponse.of(dto.imageFileId(), dto.imageFileName(), fileService.getUrlByPath(dto.imageFilePath()));
    }
}
