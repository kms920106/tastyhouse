package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopContentBoardResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.ceoapi.shop.response.ShopContentBoardResponse;

/**
 * 점주용 가게 콘텐츠보드 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopContentBoardQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public List<ShopContentBoardResponse> getContentBoards(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopQueryDao.findContentBoards(shopId).stream()
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
