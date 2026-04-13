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
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "uploaded_file_id", nullable = false)
    private Long uploadedFileId; // UploadedFile PK

    @Column(name = "sort", nullable = false)
    private Integer sort; // 이미지 정렬 순서

    private ReviewImage(
        Long reviewId,
        Long uploadedFileId,
        Integer sort
    ) {
        this.reviewId = reviewId;
        this.uploadedFileId = uploadedFileId;
        this.sort = sort;
    }

    public static ReviewImage of(
        Long reviewId,
        Long uploadedFileId,
        Integer sort
    ) {
        return new ReviewImage(
            reviewId,
            uploadedFileId,
            sort
        );
    }
}
