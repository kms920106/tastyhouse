package com.tastyhouse.webapi.rank;

import com.tastyhouse.core.entity.rank.RankType;
import com.tastyhouse.core.entity.rank.dto.MemberRankDto;
import com.tastyhouse.core.service.RankCoreService;
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

    @Transactional(readOnly = true)
    public List<MemberRankItem> getMemberRankList(String rankType, int limit) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = calculateBaseDate(type);

        List<MemberRankDto> ranks = rankCoreService.searchMemberRankList(type, baseDate, limit);

        return ranks.stream()
            .map(this::convertToMemberRankItem)
            .toList();
    }

    @Transactional(readOnly = true)
    public MyRankResponse getMyMemberRank(Long memberId, String rankType) {
        RankType type = parseRankType(rankType);
        LocalDate baseDate = calculateBaseDate(type);

        return rankCoreService.findMemberRank(memberId, type, baseDate)
            .map(MyRankResponse::from)
            .orElse(null);
    }

    private MemberRankItem convertToMemberRankItem(MemberRankDto dto) {
        return new MemberRankItem(
            dto.memberId(), dto.nickname(), dto.profileImageUrl(),
            dto.reviewCount(), dto.rankNo(), dto.grade()
        );
    }

    private RankType parseRankType(String rankType) {
        try {
            return RankType.valueOf(rankType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RankType.ALL;
        }
    }

    private LocalDate calculateBaseDate(RankType rankType) {
        return LocalDate.now();
    }
}
