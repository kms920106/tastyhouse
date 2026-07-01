package com.tastyhouse.core.domain.rank.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.rank.application.dto.result.MemberRankResult;
import com.tastyhouse.core.domain.rank.application.dto.result.RankDurationResult;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPrizeResult;
import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.domain.repository.MemberReviewRankRepository;
import com.tastyhouse.core.domain.rank.domain.repository.RankInfoRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RankQueryService {

    private final RankInfoRepository rankInfoRepository;
    private final MemberReviewRankRepository memberReviewRankRepository;

    public Optional<RankDurationResult> findActiveDuration() {
        return rankInfoRepository.findActiveDuration();
    }

    public List<RankPrizeResult> findActivePrizes() {
        return rankInfoRepository.findActivePrizes();
    }

    public List<MemberRankResult> searchMemberRankList(RankType rankType, LocalDate baseDate, int limit) {
        return memberReviewRankRepository.findMemberRankList(rankType, baseDate, limit);
    }

    public MemberRankResult findMemberRank(Long memberId, RankType rankType, LocalDate baseDate) {
        return memberReviewRankRepository.findMemberRank(memberId, rankType, baseDate);
    }

    public Optional<MemberReviewRank> findLatestByMemberIdAndRankType(Long memberId, RankType rankType) {
        return memberReviewRankRepository.findLatestByMemberIdAndRankType(memberId, rankType);
    }
}
