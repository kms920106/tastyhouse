package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 사장님 추천(대표 메뉴) 지정 요청 write 포트.
 *
 * <p>{@code existsByProductIdAndStatus}는 "같은 메뉴에 PENDING 2건 금지" 불변식 검증용이고,
 * {@code countByShopIdAndStatus}는 <b>최대 6개 제한</b> 검증용이다 — 이미 켜진 대표 메뉴 수만
 * 세면 점주가 대기 중인 요청을 여러 건 쌓아 승인 시점에 6개를 넘길 수 있으므로, 대기 건수도
 * 함께 세야 제한이 실제로 성립한다.
 *
 * <p>둘 다 화면용 집계가 아니라 불변식 검증이므로 query DAO가 아니라 이 포트에 있다.
 */
public interface ProductRepresentativeRequestRepository {

    ProductRepresentativeRequest save(ProductRepresentativeRequest request);

    Optional<ProductRepresentativeRequest> findById(ProductRepresentativeRequestId id);

    List<ProductRepresentativeRequest> findAllByProductId(ProductId productId);

    boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status);

    /** 가게의 특정 상태 요청 건수. 최대 6개 제한 검증에서 대기 건수를 세는 데 쓴다. */
    long countByShopIdAndStatus(ShopId shopId, ApprovalStatus status);
}
