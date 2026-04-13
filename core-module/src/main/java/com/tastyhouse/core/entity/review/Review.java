package com.tastyhouse.core.entity.review;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "REVIEW")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // 내용

    @Column(name = "total_rating", nullable = false)
    private Double totalRating; // 총 평점

    @Column(name = "taste_rating")
    private Double tasteRating; // 맛

    @Column(name = "amount_rating")
    private Double amountRating; // 양

    @Column(name = "price_rating")
    private Double priceRating; // 가격

    @Column(name = "atmosphere_rating")
    private Double atmosphereRating; // 분위기

    @Column(name = "kindness_rating")
    private Double kindnessRating; // 친절도

    @Column(name = "hygiene_rating")
    private Double hygieneRating; // 위생

    @Column(name = "will_revisit")
    private Boolean willRevisit; // 재방문 의사

    @Column(name = "order_id")
    private Long orderId; // null이면 일반 리뷰, 값이 있으면 주문/결제 기반 인증 리뷰

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false; // 관리자 미노출 여부

    private Review(
        Long placeId,
        Long productId,
        Long memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        Boolean willRevisit,
        Long orderId
    ) {
        this.placeId = placeId;
        this.productId = productId;
        this.memberId = memberId;
        this.content = content;
        this.totalRating = totalRating;
        this.tasteRating = tasteRating;
        this.amountRating = amountRating;
        this.priceRating = priceRating;
        this.atmosphereRating = atmosphereRating;
        this.kindnessRating = kindnessRating;
        this.hygieneRating = hygieneRating;
        this.willRevisit = willRevisit;
        this.orderId = orderId;
        this.isHidden = false;
    }

    public static Review of(
        Long placeId,
        Long productId,
        Long memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        Boolean willRevisit,
        Long orderId
    ) {
        return new Review(
            placeId,
            productId,
            memberId,
            content,
            totalRating,
            tasteRating,
            amountRating,
            priceRating,
            atmosphereRating,
            kindnessRating,
            hygieneRating,
            willRevisit,
            orderId
        );
    }

    public void updateContent(
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        Boolean willRevisit
    ) {
        this.content = content;
        this.totalRating = totalRating;
        this.tasteRating = tasteRating;
        this.amountRating = amountRating;
        this.priceRating = priceRating;
        this.atmosphereRating = atmosphereRating;
        this.kindnessRating = kindnessRating;
        this.hygieneRating = hygieneRating;
        this.willRevisit = willRevisit;
    }
}
