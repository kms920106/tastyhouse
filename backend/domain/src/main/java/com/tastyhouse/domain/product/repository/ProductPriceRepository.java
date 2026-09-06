package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 가격 write 포트. 표현 목적 조회는 {@code ProductPriceQueryDao}가 담당한다.
 *
 * <p>{@code findAllByShopId}는 화면용 목록이 아니라 <b>불변식 검증용</b>이라 이 포트에 있다 —
 * '매장가격 픽업' 뱃지의 "전체 메뉴 기준 80% 이상" 판정과 가게 단위 인증 상태 재판정이 가게의
 * 모든 가격 행을 함께 봐야 성립한다.
 *
 * <p>{@code deleteAllByIdIn}이 필요한 이유는 가격 등록·수정이 <b>전체 교체(PUT)</b>이기 때문이다 —
 * 요청에 담기지 않은 기존 행은 삭제돼야 한다.
 */
public interface ProductPriceRepository {

    ProductPrice save(ProductPrice productPrice);

    Optional<ProductPrice> findById(ProductPriceId id);

    /** 메뉴의 가격 행 전체를 {@code sort} 오름차순으로 조회한다. */
    List<ProductPrice> findAllByProductId(ProductId productId);

    /** 가게의 모든(삭제되지 않은) 메뉴의 가격 행. 뱃지 조건·인증 재판정용이다. */
    List<ProductPrice> findAllByShopId(ShopId shopId);

    void deleteAllByIdIn(List<ProductPriceId> ids);
}
