package com.tastyhouse.application.rank.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankType;

/**
 * 랭킹 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>진행 중인 랭킹 기간·경품과 순위표를 회원에게 노출하는 조회를 담당한다. 랭킹 기간·경품을
 * 등록·수정하는 관리 화면 조회는 {@code RankManagementQueryPort}가 소유한다.
 *
 * <p>{@link #findMemberRanks}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다.
 */
public interface RankQueryPort {

    Optional<RankDurationResult> findActiveDuration();

    List<RankPrizeResult> findActivePrizes();

    /** 공유 메서드 — {@code RankManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<MemberRankResult> findMemberRanks(RankType rankType, LocalDate baseDate, int limit);

    Optional<MemberRankResult> findMemberRank(Long memberId, RankType rankType, LocalDate baseDate);
}
