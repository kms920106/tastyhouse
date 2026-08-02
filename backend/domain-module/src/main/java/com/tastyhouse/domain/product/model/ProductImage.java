package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 상품 이미지 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductImageJpaEntity} + {@code ProductImageMapper}가 담당한다. 상태전이 메서드가
 * 없는 불변 애그리거트라 전 필드가 {@code final}이다.
 */
public class ProductImage {

    private final Long id;
    private final ProductId productId;
    private final UploadedFileId imageFileId;
    private final Integer sort;
    private final boolean visible;

    private ProductImage(Long id, ProductId productId, UploadedFileId imageFileId, Integer sort, boolean visible) {
        this.id = id;
        this.productId = productId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public static ProductImage of(ProductId productId, UploadedFileId imageFileId, Integer sort, boolean visible) {
        return new ProductImage(null, productId, imageFileId, sort, visible);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ProductImage reconstitute(
        Long id,
        ProductId productId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ProductImage(id, productId, imageFileId, sort, visible);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
