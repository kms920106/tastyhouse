package com.tastyhouse.external.crawling.bbq;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.crawling.bbq.port.out.BbqMenuPort;
import com.tastyhouse.application.crawling.bbq.port.out.BbqProductCategoryResponse;
import com.tastyhouse.application.crawling.bbq.port.out.BbqProductResponse;
import com.tastyhouse.application.crawling.bbq.port.out.BbqProductSubOptionResponse;
import com.tastyhouse.application.crawling.bbq.port.out.SubOptionItemDetailResponse;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuCategoryResponse;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuResponse;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuSubOptionResponse;

@Component
public class BbqMenuAdapter implements BbqMenuPort {

    private final BbqApiClient bbqApiClient;

    public BbqMenuAdapter(BbqApiClient bbqApiClient) {
        this.bbqApiClient = bbqApiClient;
    }

    @Override
    public List<BbqProductCategoryResponse> fetchMenuCategories() {
        return bbqApiClient.getMenuCategoriesSync().stream()
            .map(BbqMenuAdapter::toProductCategoryResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<BbqProductResponse> fetchMenusByCategoryId(Long categoryId) {
        return bbqApiClient.getMenusByCategoryIdSync(categoryId).stream()
            .map(BbqMenuAdapter::toProductResponse)
            .collect(Collectors.toList());
    }

    @Override
    public BbqProductResponse fetchMenuDetail(Long menuId) {
        return toProductResponse(bbqApiClient.getMenuDetailSync(menuId));
    }

    @Override
    public List<BbqProductSubOptionResponse> fetchMenuSubOptions(Long menuId) {
        return bbqApiClient.getMenuSubOptionsSync(menuId).stream()
            .map(BbqMenuAdapter::toProductSubOptionResponse)
            .collect(Collectors.toList());
    }

    private static BbqProductCategoryResponse toProductCategoryResponse(BbqMenuCategoryResponse externalResponse) {
        return BbqProductCategoryResponse.from(
            externalResponse.getId(),
            null,
            externalResponse.getCategoryName(),
            externalResponse.getPriority(),
            true
        );
    }

    private static BbqProductResponse toProductResponse(BbqMenuResponse externalResponse) {
        return BbqProductResponse.from(
            externalResponse.getId(),
            externalResponse.getMenuName(),
            externalResponse.getDescription(),
            externalResponse.getMenuImageUrl(),
            externalResponse.getMenuPrice(),
            externalResponse.getAddPrice(),
            externalResponse.getSoldOut() != null && externalResponse.getSoldOut(),
            externalResponse.getAdultOnly() != null && externalResponse.getAdultOnly(),
            externalResponse.getCanDeliver() != null && externalResponse.getCanDeliver(),
            externalResponse.getCanTakeout() != null && externalResponse.getCanTakeout()
        );
    }

    private static BbqProductSubOptionResponse toProductSubOptionResponse(BbqMenuSubOptionResponse externalResponse) {
        List<SubOptionItemDetailResponse> itemDetails = null;
        if (externalResponse.getSubOptionItemDetailResponseList() != null) {
            itemDetails = externalResponse.getSubOptionItemDetailResponseList().stream()
                .map(item -> SubOptionItemDetailResponse.from(
                    item.getId(),
                    item.getItemTitle(),
                    item.getAddPrice(),
                    item.getSoldOut() != null && item.getSoldOut(),
                    item.getHidden() != null && item.getHidden()
                ))
                .collect(Collectors.toList());
        }
        return BbqProductSubOptionResponse.from(
            externalResponse.getId(),
            externalResponse.getSubOptionTitle(),
            externalResponse.getRequiredSelectCount(),
            externalResponse.getMaxSelectCount(),
            itemDetails
        );
    }
}
