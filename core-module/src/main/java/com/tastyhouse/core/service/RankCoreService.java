package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.rank.MemberReviewRank;
import com.tastyhouse.core.entity.rank.RankType;
import com.tastyhouse.core.entity.rank.dto.MemberRankDto;
import com.tastyhouse.core.repository.rank.MemberReviewRankRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankCoreService {

    private final MemberReviewRankRepository memberReviewRankRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<MemberRankDto> searchMemberRankList(RankType rankType, LocalDate baseDate, int limit) {
        return memberReviewRankRepository.findMemberRankList(rankType, baseDate, limit);
    }

    @Transactional(readOnly = true)
    public Optional<MemberRankDto> findMemberRank(Long memberId, RankType rankType, LocalDate baseDate) {
        return memberReviewRankRepository.findMemberRank(memberId, rankType, baseDate);
    }

    @Transactional
    public void saveAllRanks(List<MemberReviewRank> ranks) {
        memberReviewRankRepository.saveAll(ranks);
    }

    @Transactional
    public void deleteOldRanks(RankType rankType, LocalDate baseDate) {
        memberReviewRankRepository.deleteByRankTypeAndBaseDate(rankType, baseDate);
        entityManager.flush();
        entityManager.clear();
    }
}
