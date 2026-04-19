package com.tastyhouse.webapi.rank;

import com.tastyhouse.core.entity.rank.RankType;
import com.tastyhouse.core.entity.rank.dto.MemberRankDto;
import com.tastyhouse.core.service.RankCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.rank.response.MemberRankItem;
import com.tastyhouse.webapi.rank.response.MyRankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {

    private final RankCoreService rankCoreService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<MemberRankItem> getMemberRankList(String rankType, int limit) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = calculateBaseDate();

        List<MemberRankDto> ranks = rankCoreService.searchMemberRankList(type, baseDate, limit);

        return ranks.stream()
            .map(dto -> MemberRankItem.from(
                dto.memberId(),
                dto.nickname(),
                fileService.getUrlByPath(dto.profileImageUrl()),
                dto.reviewCount(),
                dto.rankNo(),
                dto.grade()))
            .toList();
    }

    @Transactional(readOnly = true)
    public MyRankResponse getMyMemberRank(Long memberId, String rankType) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = calculateBaseDate();

        MemberRankDto dto = rankCoreService.findMemberRank(memberId, type, baseDate);
        if (dto == null) {
            return null;
        }
        return MyRankResponse.from(
            dto.memberId(),
            dto.nickname(),
            fileService.getUrlByPath(dto.profileImageUrl()),
            dto.reviewCount(),
            dto.rankNo(),
            dto.grade()
        );
    }

    private RankType parseRankType(String rankType) {
        try {
            return RankType.valueOf(rankType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RankType.ALL;
        }
    }

    private LocalDate calculateBaseDate() {
        return LocalDate.now();
    }
}
