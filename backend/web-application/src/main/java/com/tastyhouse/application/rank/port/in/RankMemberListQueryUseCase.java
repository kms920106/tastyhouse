package com.tastyhouse.application.rank.port.in;

import java.util.List;

import com.tastyhouse.application.rank.port.out.MemberRankResult;

public interface RankMemberListQueryUseCase {

    List<MemberRankResult> getMemberRankList(String rankType, int limit);
}
