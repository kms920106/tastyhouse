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
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 장소 ID (PLACE.id 참조)

    @Column(name = "product_id", nullable = false)
    private Long productId; // 상품 ID (PRODUCT.id 참조)

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 회원 ID (MEMBER.id 참조)

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // 리뷰 내용

    @Column(name = "total_rating", nullable = false)
    private Double totalRating; // 종합 평점

    @Column(name = "taste_rating")
    private Double tasteRating; // 맛 평점

    @Column(name = "amount_rating")
    private Double amountRating; // 양 평점

    @Column(name = "price_rating")
    private Double priceRating; // 가격 평점

    @Column(name = "atmosphere_rating")
    private Double atmosphereRating; // 분위기 평점

    @Column(name = "kindness_rating")
    private Double kindnessRating; // 친절도 평점

    @Column(name = "hygiene_rating")
    private Double hygieneRating; // 위생 평점

    @Column(name = "will_revisit")
    private Boolean willRevisit; // 재방문 의사 (true: 재방문 의향 있음)

    @Column(name = "order_id")
    private Long orderId; // 주문 ID (ORDERS.id 참조, null이면 일반 리뷰, 값이 있으면 주문 기반 인증 리뷰)

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false; // 숨김 여부 (true: 관리자에 의해 숨김 처리)

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
