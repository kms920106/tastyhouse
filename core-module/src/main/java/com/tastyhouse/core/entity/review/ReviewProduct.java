package com.tastyhouse.core.entity.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "REVIEW_PRODUCT")
public class ReviewProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "review_id", nullable = false)
    private Long reviewId; // 리뷰 ID (REVIEW.id 참조)

    @Column(name = "product_id", nullable = false)
    private Long productId; // 리뷰에서 선택한 상품 ID (PRODUCT.id 참조)
}
