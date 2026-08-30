package com.tastyhouse.application.rank.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

/**
 * 랭킹 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>랭킹 기간과 경품의 관리 목록·상세를 조회한다. 회원 노출 조회는 {@link RankQueryPort}가 소유한다.
 *
 * <p>{@link #findMemberRanks}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다 —
 * 관리자가 기간별 순위 결과를 확인할 때 회원 화면과 같은 집계를 본다.
 */
public interface RankManagementQueryPort {

    /** 공유 메서드 — {@link RankQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<MemberRankResult> findMemberRanks(RankType rankType, LocalDate baseDate, int limit);

    List<RankPeriodResult> findAllPeriods();

    Optional<RankPeriodResult> findPeriodById(RankPeriodId id);

    List<RankPrizeManagementResult> findPrizesByPeriodId(RankPeriodId periodId);

    Optional<RankPrizeManagementResult> findPrizeById(RankPrizeId id);
}
