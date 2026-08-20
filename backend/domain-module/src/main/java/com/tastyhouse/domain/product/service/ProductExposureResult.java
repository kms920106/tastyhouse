package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.product.model.ProductHiddenReason;

/**
 * {@link ProductExposureCalculator}의 판정 결과.
 *
 * <p>노출 여부만 돌려주면 화면이 "왜 안 보이는지"를 설명할 수 없고, 점주 문의 때 어느 조건에
 * 걸렸는지 추적할 수 없다. 그래서 사유를 함께 담는다({@code ShopOperatingStatusResult} 선례).
 */
public record ProductExposureResult(boolean exposed, ProductHiddenReason hiddenReason) {

    /** 노출 중. 사유는 없다. */
    public static ProductExposureResult ofExposed() {
        return new ProductExposureResult(true, null);
    }

    /** 숨김. 어느 조건에 걸렸는지를 사유로 함께 담는다. */
    public static ProductExposureResult ofHidden(ProductHiddenReason reason) {
        return new ProductExposureResult(false, reason);
    }
}
