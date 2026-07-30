package com.tastyhouse.core.domain.rank.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;

/**
 * 회원 리뷰 랭킹 write 포트.
 *
 * <p>표현 목적 조회(랭킹 목록·내 랭킹 등 회원·파일 join 투영)는 infrastructure-module의
 * {@code rank/query/RankQueryDao}로 이관했다. 여기에는 랭킹 확정(도메인 서비스) 트랜잭션 안에서
 * 소비되는 일괄 삭제·적재와, 회원 등급 산정이 리뷰 수를 읽는 단건 도메인 모델 로드만 남긴다.
 */
public interface MemberReviewRankRepository {

    /**
     * 회원의 가장 최근 기준일 랭킹을 도메인 모델로 로드한다 — 회원 등급 산정이 리뷰 수를 읽는 경로다.
     */
    Optional<MemberReviewRank> findLatestByMemberIdAndRankType(MemberId memberId, RankType rankType);

    void saveAll(List<MemberReviewRank> ranks);

    void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate);
}
