package com.tastyhouse.core.domain.coupon.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;

/**
 * 쿠폰 write 포트.
 *
 * <p>표현 목적 조회(목록·상세·발급현황)는 infrastructure-module의 {@code CouponQueryDao}가 담당하므로
 * 이 포트에는 command 경로와 도메인 서비스가 트랜잭션 안에서 소비하는 단건 로드·저장만 남긴다.
 */
public interface CouponRepository {

    Optional<Coupon> findById(CouponId id);

    Coupon save(Coupon coupon);
}
