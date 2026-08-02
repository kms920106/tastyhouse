package com.tastyhouse.adminapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.service.ShopImageApprovalService;

/**
 * admin용 가게 이미지 변경요청 검수 변경 서비스(CQRS command 측).
 *
 * <p>승인 시 요청 상태 전이와 가게 이미지 반영이 한 트랜잭션에서 함께 일어나야 하는 원자 연산은
 * 도메인 서비스 {@link ShopImageApprovalService}가 담당한다(요청자 ceo·검수자 admin 공유 규칙).
 */
@Service
@Transactional
public class ShopImageChangeCommandService {

    private final ShopImageApprovalService shopImageApprovalService;

    public ShopImageChangeCommandService(ShopImageApprovalService shopImageApprovalService) {
        this.shopImageApprovalService = shopImageApprovalService;
    }

    public void approveImageChange(Long id) {
        shopImageApprovalService.approveImageChange(id);
    }

    public void rejectImageChange(Long id, String reason) {
        shopImageApprovalService.rejectImageChange(id, reason);
    }
}
