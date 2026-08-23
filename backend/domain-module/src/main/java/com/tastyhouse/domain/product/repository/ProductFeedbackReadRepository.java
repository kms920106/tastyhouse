package com.tastyhouse.domain.product.repository;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주의 고객 의견 확인 시각 write 포트. 가게당 1건이므로 자연키 조회 하나면 충분하다.
 */
public interface ProductFeedbackReadRepository {

    ProductFeedbackRead save(ProductFeedbackRead feedbackRead);

    Optional<ProductFeedbackRead> findByShopId(ShopId shopId);
}
