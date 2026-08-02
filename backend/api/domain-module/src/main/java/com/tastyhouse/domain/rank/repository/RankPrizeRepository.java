package com.tastyhouse.domain.rank.repository;

import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

/**
 * 랭킹 경품 write 포트.
 *
 * <p>기간별 목록·상세 조회(파일 join 투영 포함)는 infrastructure-module의
 * {@code rank/query/RankQueryDao}로 이관했고, command 경로에서 소비되는 단건 로드·저장·소프트 삭제만
 * 남긴다.
 */
public interface RankPrizeRepository {

    RankPrize save(RankPrize rankPrize);

    Optional<RankPrize> findById(RankPrizeId id);

    void delete(RankPrize rankPrize);
}
