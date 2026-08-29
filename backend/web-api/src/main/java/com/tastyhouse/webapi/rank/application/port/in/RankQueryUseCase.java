package com.tastyhouse.webapi.rank.application.port.in;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.webapi.rank.response.RankDurationResponse;
import com.tastyhouse.webapi.rank.response.RankMemberListItemResponse;
import com.tastyhouse.webapi.rank.response.RankPrizeListItemResponse;

/**
 * 랭킹 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code RankQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface RankQueryUseCase {

    Optional<RankDurationResponse> getDuration();

    List<RankPrizeListItemResponse> getPrizes();

    List<RankMemberListItemResponse> getMemberRankList(String rankType, int limit);

    RankMemberListItemResponse getMyMemberRank(Long memberId, String rankType);
}
