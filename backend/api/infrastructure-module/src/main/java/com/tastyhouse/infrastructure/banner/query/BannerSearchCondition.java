package com.tastyhouse.infrastructure.banner.query;

import com.tastyhouse.domain.banner.model.BannerType;

/**
 * 배너 관리 목록 검색 조건.
 *
 * <p>표현 목적 read의 입력이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다.
 * 소비 모듈(admin-api)의 {@code BannerQueryService}가 원시 파라미터를 받아 {@code BannerType.from(String)}
 * 으로 승격한 뒤 조립해 전달한다(api 모듈은 core enum을 HTTP 경계에 노출하지 않는다).
 */
public record BannerSearchCondition(
    BannerType type,
    String title,
    Boolean visible
) {

    public static BannerSearchCondition of(BannerType type, String title, Boolean visible) {
        return new BannerSearchCondition(type, title, visible);
    }
}
