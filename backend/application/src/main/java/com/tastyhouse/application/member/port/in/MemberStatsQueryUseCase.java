package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.member.port.out.MemberStatsResult;

@WebApp
public interface MemberStatsQueryUseCase {

    MemberStatsResult getMemberStats(Long memberId);
}
