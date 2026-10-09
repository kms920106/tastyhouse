package com.tastyhouse.application.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopMediaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryViewResult;

@Service
@Transactional(readOnly = true)
class ShopPhotoListQueryService implements ShopPhotoListQueryUseCase {

    private final ShopMediaQueryPort shopMediaQueryPort;

    public ShopPhotoListQueryService(ShopMediaQueryPort shopMediaQueryPort) {
        this.shopMediaQueryPort = shopMediaQueryPort;
    }

    @Override
    public List<ShopPhotoCategoryViewResult> getShopPhotos(Long shopId) {
        List<ShopPhotoCategoryResult> categories = shopMediaQueryPort.findPhotoCategories(shopId);
        List<ShopPhotoCategoryImageResult> images = shopMediaQueryPort.findAllPhotoCategoryImages();

        Map<Long, List<ShopPhotoCategoryImageResult>> imagesByCategory = images.stream()
            .filter(image -> image.shopPhotoCategoryId() != null)
            .collect(Collectors.groupingBy(ShopPhotoCategoryImageResult::shopPhotoCategoryId));

        return categories.stream()
            .map(category -> {
                List<ShopPhotoCategoryImageResult> categoryImages =
                    imagesByCategory.getOrDefault(category.id(), new ArrayList<>());
                List<String> imageUrls = categoryImages.stream()
                    .map(ShopPhotoCategoryImageResult::imageUrl)
                    .toList();
                return new ShopPhotoCategoryViewResult(
                    category.name(),
                    imageUrls
                );
            })
            .toList();
    }
}
