package com.tastyhouse.application.rank.port.in;

import java.util.List;

import com.tastyhouse.application.rank.port.out.MemberRankResult;

public interface RankMemberManagementListQueryUseCase {

    List<MemberRankResult> getMemberRankList(String type, int limit);
}
