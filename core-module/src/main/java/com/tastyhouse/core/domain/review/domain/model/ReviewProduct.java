package com.tastyhouse.core.domain.review.domain.model;

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
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    protected ReviewProduct() {
    }

    public ReviewProduct(Long reviewId, Long productId) {
        this.reviewId = reviewId;
        this.productId = productId;
    }
}
