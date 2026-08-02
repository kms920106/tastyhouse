package com.tastyhouse.infrastructure.banner.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 회원 노출용 배너 목록 항목 조회 결과.
 *
 * <p>관리 화면용 형제인 {@link BannerManagementListItemResult}와 같은 패키지에 공존하지만
 * 필드 셋이 달라(노출용은 표시에 필요한 최소 필드만) 통합하지 않는다. 이미지는 파일 조인으로
 * 얻은 경로를 DAO가 표시용 URL까지 변환해 담으므로, 소비 모듈은 이 값을 그대로 응답에 전달한다.
 */
public record BannerListItemResult(
    Long id,
    String title,
    String imageUrl,
    String linkUrl
) {

    @QueryProjection
    public BannerListItemResult {
    }
}
