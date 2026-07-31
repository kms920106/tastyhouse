package com.tastyhouse.domain.banner.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.banner.domain.model.Banner;
import com.tastyhouse.domain.banner.domain.vo.BannerId;

/**
 * 배너 write 포트.
 *
 * <p>도메인 모델을 주고받는 CRUD만 노출한다. 목록·검색·페이징 등 표현 목적 read는 이 포트가 아니라
 * infrastructure-module의 {@code banner/query/BannerQueryDao}가 담당한다(CQRS 분리).
 */
public interface BannerRepository {

    Optional<Banner> findById(BannerId id);

    Banner save(Banner banner);
}
