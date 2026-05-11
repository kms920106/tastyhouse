package com.tastyhouse.webapi.rank;

import com.tastyhouse.core.entity.rank.RankType;
import com.tastyhouse.core.entity.rank.dto.MemberRankDto;
import com.tastyhouse.core.entity.rank.dto.RankPrizeDto;
import com.tastyhouse.core.entity.user.dto.MemberWithProfileImageDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.MemberCoreService;
import com.tastyhouse.core.service.RankCoreService;
import com.tastyhouse.core.service.RankInfoCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.rank.response.MemberRankResponse;
import com.tastyhouse.webapi.rank.response.RankDurationResponse;
import com.tastyhouse.webapi.rank.response.RankPrizeItemResponse;
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
    private final MemberCoreService memberCoreService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public Optional<RankDurationResponse> getDuration() {
        return rankInfoCoreService.findActiveDuration()
            .map(dto -> RankDurationResponse.from(dto.startAt(), dto.endAt()));
    }

    @Transactional(readOnly = true)
    public List<RankPrizeItemResponse> getPrizes() {
        return rankInfoCoreService.findActivePrizes().stream()
            .map(this::convertToPrizeItemResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberRankResponse> getMemberRankList(String rankType, int limit) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();

        List<MemberRankDto> ranks = rankCoreService.searchMemberRankList(type, baseDate, limit);

        return ranks.stream()
            .map(dto -> MemberRankResponse.of(
                dto.memberId(),
                dto.nickname(),
                fileService.getUrlByPath(dto.profileImageUrl()),
                dto.reviewCount(),
                dto.rankNo(),
                dto.grade()))
            .toList();
    }

    @Transactional(readOnly = true)
    public MemberRankResponse getMyMemberRank(Long memberId, String rankType) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = LocalDate.now();

        MemberRankDto dto = rankCoreService.findMemberRank(memberId, type, baseDate);
        if (dto == null) {
            MemberWithProfileImageDto member = memberCoreService.findMemberWithProfileImageById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
            return MemberRankResponse.of(
                memberId,
                member.nickname(),
                fileService.getUrlByPath(member.profileImageFilePath()),
                0,
                null,
                member.memberGrade()
            );
        }
        return MemberRankResponse.of(
            dto.memberId(),
            dto.nickname(),
            fileService.getUrlByPath(dto.profileImageUrl()),
            dto.reviewCount(),
            dto.rankNo(),
            dto.grade()
        );
    }

    private RankPrizeItemResponse convertToPrizeItemResponse(RankPrizeDto dto) {
        return RankPrizeItemResponse.from(
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
