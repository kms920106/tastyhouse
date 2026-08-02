package com.tastyhouse.domain.review.domain.model;

import lombok.Getter;

import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

/**
 * 리뷰 이미지 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ReviewImageJpaEntity} + {@code ReviewImageMapper}가 담당한다. 불변 애그리거트로
 * 상태전이가 없어 감사 시각을 소비하지 않는다.
 */
@Getter
public class ReviewImage {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ReviewId reviewId;
    private final UploadedFileId imageFileId;
    private final Integer sort;

    private ReviewImage(Long id, ReviewId reviewId, UploadedFileId imageFileId, Integer sort) {
        this.id = id;
        this.reviewId = reviewId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    /**
     * 신규 리뷰 이미지를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static ReviewImage of(ReviewId reviewId, UploadedFileId imageFileId, Integer sort) {
        return new ReviewImage(null, reviewId, imageFileId, sort);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ReviewImage reconstitute(Long id, ReviewId reviewId, UploadedFileId imageFileId, Integer sort) {
        return new ReviewImage(id, reviewId, imageFileId, sort);
    }
}
