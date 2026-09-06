package com.tastyhouse.domain.product.repository;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 메뉴 노출 요일·시간대 write 포트.
 *
 * <p>설정은 <b>replace-all</b>로 교체한다({@code deleteAllByProductId} → {@code saveAll}) —
 * 요일 묶음과 개별 요일의 혼용 금지가 집합 전체를 봐야 판정되는 규칙이라, 행 단위로 열면
 * 중간 상태가 반드시 규칙을 위반한다.
 */
public interface ProductExposureHourRepository {

    List<ProductExposureHour> saveAll(List<ProductExposureHour> hours);

    List<ProductExposureHour> findAllByProductId(ProductId productId);

    void deleteAllByProductId(ProductId productId);
}
