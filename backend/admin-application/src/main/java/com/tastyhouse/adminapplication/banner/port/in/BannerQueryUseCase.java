package com.tastyhouse.adminapplication.banner.port.in;

import com.tastyhouse.adminapplication.banner.response.BannerDetailResponse;
import com.tastyhouse.adminapplication.banner.response.BannerListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 배너 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code BannerQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface BannerQueryUseCase {

    PaginationResponse<BannerListItemResponse> getBanners(String type, String title, Boolean visible, int page, int size);

    BannerDetailResponse getBanner(Long id);
}
