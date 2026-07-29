package com.tastyhouse.infrastructure.banner.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;

/**
 * 배너 상세 조회 결과.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다. 식별자는 HTTP 경계까지
 * 그대로 전달되는 표현용 값이므로 도메인 VO({@code BannerId})가 아니라 {@code Long}으로 투영한다.
 * 이미지 파일 정보도 같은 쿼리의 조인으로 함께 투영해, 소비 모듈이 파일을 다시 조회하지 않는다.
 */
public record BannerDetailResult(
    Long id,
    BannerType type,
    String title,
    Long imageFileId,
    String imageFileName,
    String imageFilePath,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    @QueryProjection
    public BannerDetailResult {
    }
}
