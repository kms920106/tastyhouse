package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.member.port.out.MemberStatsResult;

public interface MemberStatsQueryUseCase {

    MemberStatsResult getMemberStats(Long memberId);
}
