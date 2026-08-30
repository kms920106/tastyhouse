package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.ceoapplication.shop.response.ShopContentBoardResponse;

/**
 * 점주용 가게 콘텐츠보드 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
public class ShopContentBoardQueryService implements ShopContentBoardQueryUseCase {

    private final ShopQueryPort shopQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopContentBoardQueryService(ShopQueryPort shopQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopQueryPort = shopQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopContentBoardResponse> getContentBoards(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopQueryPort.findContentBoards(shopId).stream()
            .map(this::toShopContentBoardResponse)
            .toList();
    }

    private ShopContentBoardResponse toShopContentBoardResponse(ShopContentBoardResult dto) {
        return ShopContentBoardResponse.of(
            dto.id(),
            dto.shopId(),
            dto.contentType().name(),
            dto.topic().name(),
            dto.imageUrl(),
            dto.youtubeUrl(),
            dto.description(),
            dto.hidden()
        );
    }
}
