package com.tastyhouse.domain.product.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 상품 write 포트.
 *
 * <p>표현 목적 조회(목록·검색·상세 투영)는 infrastructure-module의 {@code ProductQueryDao}가 담당하고,
 * 이 포트에는 command 경로·도메인 서비스가 불변식 검증과 상태 전이를 위해 쓰는 단건 로드·저장만 남긴다.
 */
public interface ProductRepository {

    Optional<Product> findById(ProductId id);

    Product save(Product product);

    /**
     * 일괄 품절·숨김 처리 대상을 한 번에 로드한다. 소유 가게가 다른 id는 결과에 담기지 않으므로
     * 호출부가 요청 id와 대조해 소유권 위반·미존재를 함께 판정할 수 있다.
     *
     * <p>이 조회가 write 포트에 있는 이유는 부분실패 제약 검증(노출 메뉴 ≥1 · 추천 메뉴 ≥1)이
     * 애그리거트 불변식이고, 이 로드 없이는 그 검증과 상태 전이가 불가능하기 때문이다.
     */
    List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids);

    /**
     * 가게의 현재 노출 메뉴 수. 숨김 처리 후에도 메뉴판에 최소 1개가 남는지 판정하는 데 쓴다.
     */
    long countVisibleByShopId(ShopId shopId);

    /**
     * 가게의 현재 노출 중인 사장님 추천 메뉴 수. 추천 메뉴가 0개가 되는 숨김을 막는 데 쓴다.
     */
    long countVisibleRepresentativeByShopId(ShopId shopId);

    /**
     * 자동해제 시각이 지난 품절 상품을 조회한다({@code soldOut = true} 이고
     * {@code soldOutUntil <= 기준시각}). 품절 자동해제 배치가 대상을 뽑는 데 쓴다.
     */
    List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime);
}
