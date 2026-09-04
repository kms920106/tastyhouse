package com.tastyhouse.adminapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.adminapplication.shop.port.in.ShopHygieneBadgeManagementQueryUseCase;

/**
 * admin용 가게 위생 인증 뱃지 조회 서비스(CQRS query 측). 소유권 검증 없이 전체 가게를 대상으로 한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class ShopHygieneBadgeManagementQueryService implements ShopHygieneBadgeManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopHygieneBadgeManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopHygieneBadgeResult> getHygieneBadges(Long shopId) {
        return shopBasicInfoQueryPort.findHygieneBadges(shopId);
    }

}
