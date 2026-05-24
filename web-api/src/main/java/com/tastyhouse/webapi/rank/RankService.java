package com.tastyhouse.webapi.rank;

import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.entity.rank.RankType;
import com.tastyhouse.core.entity.rank.dto.MemberRankDto;
import com.tastyhouse.core.entity.rank.dto.RankPrizeDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.RankCoreService;
import com.tastyhouse.core.service.RankInfoCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.rank.response.MemberRankListItemResponse;
import com.tastyhouse.webapi.rank.response.RankDurationResponse;
import com.tastyhouse.webapi.rank.response.RankPrizeListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RankService {

    private final RankCoreService rankCoreService;
    private final RankInfoCoreService rankInfoCoreService;
    private final MemberQueryService memberQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public Optional<RankDurationResponse> getDuration() {
        return rankInfoCoreService.findActiveDuration()
            .map(dto -> RankDurationResponse.from(dto.startAt(), dto.endAt()));
    }

    @Transactional(readOnly = true)
    public List<RankPrizeListItemResponse> getPrizes() {
        return rankInfoCoreService.findActivePrizes().stream()
            .map(this::convertToPrizeItemResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberRankListItemResponse> getMemberRankList(String rankType, int limit) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();

        List<MemberRankDto> ranks = rankCoreService.searchMemberRankList(type, baseDate, limit);

        return ranks.stream()
            .map(dto -> MemberRankListItemResponse.of(
                dto.memberId(),
                dto.nickname(),
                fileService.getUrlByPath(dto.profileImageUrl()),
                dto.reviewCount(),
                dto.rankNo(),
                dto.grade()))
            .toList();
    }

    @Transactional(readOnly = true)
    public MemberRankListItemResponse getMyMemberRank(Long memberId, String rankType) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();

        MemberRankDto dto = rankCoreService.findMemberRank(memberId, type, baseDate);
        if (dto == null) {
            MemberWithProfileImageResult member = memberQueryService.findMemberWithProfileImage(new MemberId(memberId))
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
            return MemberRankListItemResponse.of(
                memberId,
                member.nickname(),
                fileService.getUrlByPath(member.profileImageFilePath()),
                0,
                null,
                member.memberGrade()
            );
        }
        return MemberRankListItemResponse.of(
            dto.memberId(),
            dto.nickname(),
            fileService.getUrlByPath(dto.profileImageUrl()),
            dto.reviewCount(),
            dto.rankNo(),
            dto.grade()
        );
    }

    private RankPrizeListItemResponse convertToPrizeItemResponse(RankPrizeDto dto) {
        return RankPrizeListItemResponse.from(
            dto.id(),
            dto.prizeRank(),
            dto.name(),
            dto.brand(),
            fileService.getUrlByPath(dto.imageFilePath())
        );
    }

    private RankType parseRankType(String rankType) {
        try {
            return RankType.valueOf(rankType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RankType.ALL;
        }
    }
}
