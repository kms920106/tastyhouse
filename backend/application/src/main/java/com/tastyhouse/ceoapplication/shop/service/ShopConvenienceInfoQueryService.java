package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopConvenienceInfoQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

/**
 * 점주용 가게 편의정보·편의시설 조회 서비스(CQRS query 측).
 *
 * <p>편의정보가 아직 등록되지 않은 가게는 빈 기본값 응답을 돌려준다(기존 동작 유지).
 */
@Service
@Transactional(readOnly = true)
public class ShopConvenienceInfoQueryService implements ShopConvenienceInfoQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopConvenienceInfoQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Optional<ShopConvenienceInfoResult> getConvenienceInfo(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findConvenienceInfo(shopId);
    }

    @Override
    public List<ShopAmenityAssignmentResult> getAmenities(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findAmenityAssignments(shopId);
    }
}
