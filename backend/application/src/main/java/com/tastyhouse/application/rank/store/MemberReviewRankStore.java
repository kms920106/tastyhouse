package com.tastyhouse.application.rank.store;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.application.rank.port.out.write.MemberReviewRankState;
import com.tastyhouse.application.rank.port.out.write.MemberReviewRankStatePort;

public class MemberReviewRankStore implements MemberReviewRankRepository {
    private final MemberReviewRankStatePort memberReviewRankStatePort;

    public MemberReviewRankStore(MemberReviewRankStatePort memberReviewRankStatePort) {
        this.memberReviewRankStatePort = memberReviewRankStatePort;
    }

    @Override
    public Optional<MemberReviewRank> findLatestByMemberIdAndRankType(MemberId memberId, RankType rankType) {
        return memberReviewRankStatePort.findLatestByMemberIdAndRankType(memberId.value(), rankType.name())
            .map(MemberReviewRankStateMapper::toDomain);
    }

    @Override
    public void saveAll(List<MemberReviewRank> ranks) {
        List<MemberReviewRankState> states = ranks.stream()
            .map(MemberReviewRankStateMapper::toState)
            .toList();
        memberReviewRankStatePort.saveAll(states);
    }

    @Override
    public void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate) {
        memberReviewRankStatePort.deleteByRankTypeAndBaseDate(rankType.name(), baseDate);
    }
}
