package com.tastyhouse.infrastructure.banner.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;

/**
 * 배너 관리 목록 항목 조회 결과.
 *
 * <p>비노출·노출기간 만료 배너를 포함해 조회하므로 노출 여부(visible)와 노출 기간을 갖는다.
 * 회원 노출용 형제인 {@link BannerListItemResult}와 같은 패키지에 공존해 이름이 충돌하므로
 * 관리 화면 용도를 나타내는 {@code Management} 한정어를 붙였다.
 */
public record BannerManagementListItemResult(
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
    boolean visible
) {

    @QueryProjection
    public BannerManagementListItemResult {
    }
}
