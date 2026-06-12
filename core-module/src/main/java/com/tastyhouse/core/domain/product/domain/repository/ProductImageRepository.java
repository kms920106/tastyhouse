package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductImage;

import java.util.List;

public interface ProductImageRepository {

    List<ProductImage> findActiveByProductIdOrderBySort(Long productId);

    String findFilePathByImageFileId(Long imageFileId);

    /**
     * 여러 상품의 대표 이미지(활성 + sort 최소) 파일 경로를 한 번에 조회합니다.
     * 대표 이미지가 없는 상품은 결과에 포함되지 않습니다.
     */
    List<ProductRepresentativeImage> findRepresentativeImagePathsByProductIds(List<Long> productIds);

    ProductImage save(ProductImage productImage);

    record ProductRepresentativeImage(Long productId, String filePath) {}
}
