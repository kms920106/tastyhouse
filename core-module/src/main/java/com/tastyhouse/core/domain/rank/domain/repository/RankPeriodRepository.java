package com.tastyhouse.core.domain.rank.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.rank.domain.model.RankPeriod;
import com.tastyhouse.core.domain.rank.domain.vo.RankPeriodId;

/**
 * 랭킹 기간 write 포트.
 *
 * <p>목록·상세 조회(표현 목적)는 infrastructure-module의 {@code rank/query/RankQueryDao}로 이관했고,
 * command 경로에서 소비되는 단건 로드·저장·소프트 삭제만 남긴다.
 */
public interface RankPeriodRepository {

    RankPeriod save(RankPeriod rankPeriod);

    Optional<RankPeriod> findById(RankPeriodId id);

    void delete(RankPeriod rankPeriod);
}
