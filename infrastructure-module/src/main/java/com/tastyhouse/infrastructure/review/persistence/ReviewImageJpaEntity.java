package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;
import com.tastyhouse.infrastructure.file.persistence.UploadedFileIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 리뷰 이미지 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ReviewImage}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ReviewImageMapper}가 수행한다.
 */
@Getter
@Entity
@Table(
    name = "REVIEW_IMAGE",
    indexes = {
        @Index(name = "idx_review_image_review_id", columnList = "review_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImageJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ReviewIdConverter.class)
    @Column(name = "review_id", nullable = false)
    private ReviewId reviewId;

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "image_file_id", nullable = false)
    private UploadedFileId imageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    private ReviewImageJpaEntity(ReviewId reviewId, UploadedFileId imageFileId, Integer sort) {
        this.reviewId = reviewId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ReviewImageMapper#toEntity}에서만 호출한다.
     */
    static ReviewImageJpaEntity create(ReviewId reviewId, UploadedFileId imageFileId, Integer sort) {
        return new ReviewImageJpaEntity(reviewId, imageFileId, sort);
    }
}
