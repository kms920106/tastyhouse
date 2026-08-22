package com.tastyhouse.infrastructure.product.persistence;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.port.ShopRequestIndexSyncPort;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;

/**
 * {@link ShopRequestIndexSyncPort} 어댑터 — 통합 요청처리 인덱스 기록을
 * {@link ShopRequestIndexRecorder}(shop 컨텍스트 소유)에 위임한다.
 *
 * <p><b>이 어댑터가 존재하는 이유는 컨텍스트 경계다.</b> 매장 가격 인증 요청 애그리거트는 product
 * 컨텍스트 소유인데(승인이 하는 일의 본체가 {@code PRODUCT_PRICE}를 채우는 것이다) 통합 인덱스와
 * 그 기록자는 shop 컨텍스트 소유다. domain-module의 {@code ContextBoundaryTest}가 타 컨텍스트의
 * {@code service}·{@code model} 직접 import를 금지하므로, 도메인 서비스는 포트만 알고 실제 결합은
 * 이 어댑터가 흡수한다.
 *
 * <p><b>상태 문자열을 여기서 enum으로 승격한다</b> — 포트 시그니처가 {@code String}인 것은 통합 상태
 * {@code ShopRequestStatus}가 shop 소유라 product 쪽 포트에 등장할 수 없기 때문이다. 승격 실패는
 * 프로그래밍 오류(양쪽 enum이 어긋난 상태)이므로 {@code from(String)}의 400 변환에 맡기지 않고
 * 그대로 전파시킨다 — 조용히 넘기면 인덱스가 원본과 어긋난 채 남는다.
 *
 * <p>기록은 원본 상태 전이와 <b>같은 트랜잭션</b>에서 동기 수행된다(트랜잭션 경계는 호출하는 api
 * 모듈의 command 서비스가 선언한다). 이벤트·{@code AFTER_COMMIT}을 쓰지 않는 이유는 기록 유실이 곧
 * "요청이 목록에서 사라짐"이기 때문이다.
 */
@Component
public class ShopRequestIndexSyncAdapter implements ShopRequestIndexSyncPort {

    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopRequestIndexSyncAdapter(ShopRequestIndexRecorder shopRequestIndexRecorder) {
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    @Override
    public void syncStorePriceVerificationStatus(Long sourceRequestId, String status, String rejectReason) {
        shopRequestIndexRecorder.syncRequestStatus(
            ShopRequestType.STORE_PRICE_VERIFICATION,
            sourceRequestId,
            ShopRequestStatus.from(status),
            rejectReason
        );
    }
}
