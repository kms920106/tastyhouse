package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopImageChangeApproveCommand;
import com.tastyhouse.application.shop.port.in.ShopImageChangeCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopImageChangeRejectCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopImageApprovalService;

/**
 * admin용 가게 이미지 변경요청 검수 변경 서비스(CQRS command 측).
 *
 * <p>승인 시 요청 상태 전이와 가게 이미지 반영이 한 트랜잭션에서 함께 일어나야 하는 원자 연산은
 * 도메인 서비스 {@link ShopImageApprovalService}가 담당한다(요청자 ceo·검수자 admin 공유 규칙).
 */
@Service
@AdminApp
@Transactional
public class ShopImageChangeCommandService implements ShopImageChangeCommandUseCase {

    private final ShopImageApprovalService shopImageApprovalService;

    public ShopImageChangeCommandService(ShopImageApprovalService shopImageApprovalService) {
        this.shopImageApprovalService = shopImageApprovalService;
    }

    @Override
    public void approveImageChange(ShopImageChangeApproveCommand command) {
        Long id = command.requestId();
        shopImageApprovalService.approveImageChange(id);
    }

    @Override
    public void rejectImageChange(ShopImageChangeRejectCommand command) {
        Long id = command.requestId();
        String reason = command.reason();
        shopImageApprovalService.rejectImageChange(id, reason);
    }
}
