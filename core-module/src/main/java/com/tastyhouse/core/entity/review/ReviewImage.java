package com.tastyhouse.core.entity.review;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
    name = "REVIEW_IMAGE",
    indexes = {
        @Index(name = "idx_review_image_review_id", columnList = "review_id")
    }
)
public class ReviewImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "review_id", nullable = false)
    private Long reviewId; // 리뷰 ID (REVIEW.id 참조)

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId; // 이미지 파일 ID (FILE.id 참조)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 이미지 노출 순서 (오름차순 정렬)

    private ReviewImage(
        Long reviewId,
        Long imageFileId,
        Integer sort
    ) {
        this.reviewId = reviewId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    public static ReviewImage of(
        Long reviewId,
        Long imageFileId,
        Integer sort
    ) {
        return new ReviewImage(
            reviewId,
            imageFileId,
            sort
        );
    }
}
