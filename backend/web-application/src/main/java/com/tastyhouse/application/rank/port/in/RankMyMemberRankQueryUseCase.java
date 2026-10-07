package com.tastyhouse.application.rank.port.in;

import com.tastyhouse.application.rank.port.out.MemberRankResult;

public interface RankMyMemberRankQueryUseCase {

    MemberRankResult getMyMemberRank(Long memberId, String rankType);
}
