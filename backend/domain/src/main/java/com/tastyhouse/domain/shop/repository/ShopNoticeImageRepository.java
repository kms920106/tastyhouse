package com.tastyhouse.domain.shop.repository;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopNoticeImage;

/**
 * 점주 공지 첨부 이미지 write 포트.
 *
 * <p>이미지는 불변 애그리거트라 update 경로가 없다. 수정은 {@link #deleteByShopNoticeId(Long)} 후
 * {@link #saveAll(List)}로 전량 재등록하는 replace-all이다.
 */
public interface ShopNoticeImageRepository {

    void saveAll(List<ShopNoticeImage> images);

    void deleteByShopNoticeId(Long shopNoticeId);
}
