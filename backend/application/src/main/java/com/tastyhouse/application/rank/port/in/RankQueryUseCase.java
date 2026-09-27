package com.tastyhouse.application.rank.port.in;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.rank.port.out.MemberRankResult;
import com.tastyhouse.application.rank.port.out.RankDurationResult;
import com.tastyhouse.application.rank.port.out.RankPrizeResult;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface RankQueryUseCase {

    Optional<RankDurationResult> getDuration();

    List<RankPrizeResult> getPrizes();

    List<MemberRankResult> getMemberRankList(String rankType, int limit);

    MemberRankResult getMyMemberRank(Long memberId, String rankType);
}
