package com.tastyhouse.application.banner.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.banner.model.BannerType;

/**
 * 배너 관리 목록 항목 조회 결과.
 *
 * <p>비노출·노출기간 만료 배너를 포함해 조회하므로 노출 여부(visible)와 노출 기간을 갖는다.
 * 회원 노출용 형제인 {@link BannerListItemResult}와 같은 패키지에 공존해 이름이 충돌하므로
 * 관리 화면 용도를 나타내는 {@code Management} 한정어를 붙였다.
 *
 * <p>이미지 경로는 DAO가 표시용 URL까지 변환해 담는다({@code imageUrl}). 식별자·파일명은 관리 화면
 * 응답({@code FileResponse})이 함께 노출하므로 유지한다.
 */
public record BannerManagementListItemResult(
    Long id,
    BannerType type,
    String title,
    Long imageFileId,
    String imageFileName,
    String imageUrl,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible
) {
}
