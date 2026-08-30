package com.tastyhouse.ceoapplication.shop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopOrderAvailabilityQueryUseCase;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusResult;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusService;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.ceoapplication.shop.response.ShopOrderAvailabilityResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopOrderMethodAvailabilityResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopOrderMethodItemResponse;

/**
 * 점주용 주문가능 상태 조회 서비스(CQRS query 측).
 *
 * <p>조회 전용 기능이라 짝이 되는 CommandService를 두지 않는다 — 주문유형 배정 변경은 관리자 권한
 * 영역(admin-api)이고, 임시중지 등록·해제는 기존 {@code ShopSuspensionCommandService}가 담당한다.
 *
 * <p>영업상태 판정은 여섯 애그리거트를 함께 읽어야 하므로 도메인 서비스
 * {@link ShopOperatingStatusService}에 위임하고, 배정 목록 표시는 infra query DAO를 쓴다.
 * 도메인 enum은 이 계층에서 {@code name()}/{@code getDisplayName()}으로 String 변환해 경계를 지킨다.
 */
@Service
@Transactional(readOnly = true)
public class ShopOrderAvailabilityQueryService implements ShopOrderAvailabilityQueryUseCase {

    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopOperatingStatusService shopOperatingStatusService;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopOrderAvailabilityQueryService(
        ShopOwnershipValidator shopOwnershipValidator,
        ShopOperatingStatusService shopOperatingStatusService,
        ShopBasicInfoQueryPort shopBasicInfoQueryPort
    ) {
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopOperatingStatusService = shopOperatingStatusService;
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    /**
     * 가게 주문가능 상태와 배정된 주문유형별 상태를 함께 조회한다.
     *
     * <p>가게가 불가면 전 유형이 그 사유를 그대로 물려받고, 가게가 가능하면 유형별 임시중지만 개별
     * 유형을 불가로 만든다 — 두 경우 모두 도메인 계산기의 판정 결과를 그대로 옮긴다.
     */
    @Override
    public ShopOrderAvailabilityResponse getOrderAvailability(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        LocalDateTime now = LocalDateTime.now();
        ShopOperatingStatusResult shopStatus = shopOperatingStatusService.findOrderAvailability(shopId, now);
        Map<OrderMethod, ShopOperatingStatusResult> methodStatuses =
            shopOperatingStatusService.findOrderMethodAvailabilities(shopId, now);

        List<ShopOrderMethodAvailabilityResponse> orderMethods = methodStatuses.entrySet().stream()
            .map(entry -> toShopOrderMethodAvailabilityResponse(entry.getKey(), entry.getValue()))
            .toList();

        return ShopOrderAvailabilityResponse.from(
            shopStatus.isOpen(),
            reasonCode(shopStatus.unavailableReason()),
            reasonName(shopStatus.unavailableReason()),
            orderMethods
        );
    }

    /**
     * 가게에 배정된 주문유형 목록을 조회한다. 배정 변경(등록·삭제)은 이 모듈의 범위가 아니다.
     */
    @Override
    public List<ShopOrderMethodItemResponse> getOrderMethods(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findOrderMethods(shopId).stream()
            .map(this::toShopOrderMethodItemResponse)
            .toList();
    }

    private ShopOrderMethodAvailabilityResponse toShopOrderMethodAvailabilityResponse(
        OrderMethod orderMethod,
        ShopOperatingStatusResult result
    ) {
        return ShopOrderMethodAvailabilityResponse.from(
            orderMethod.name(),
            orderMethod.getDisplayName(),
            result.isOpen(),
            reasonCode(result.unavailableReason()),
            reasonName(result.unavailableReason())
        );
    }

    private ShopOrderMethodItemResponse toShopOrderMethodItemResponse(ShopOrderMethodResult result) {
        return ShopOrderMethodItemResponse.from(
            result.id(),
            result.orderMethod().name(),
            result.orderMethod().getDisplayName()
        );
    }

    private String reasonCode(OrderUnavailableReason reason) {
        return reason == null ? null : reason.name();
    }

    private String reasonName(OrderUnavailableReason reason) {
        return reason == null ? null : reason.getDisplayName();
    }
}
