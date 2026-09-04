package com.tastyhouse.application.banner.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.banner.port.out.BannerDetailResult;
import com.tastyhouse.application.banner.port.out.BannerManagementListItemResult;
import com.tastyhouse.application.banner.port.out.BannerManagementQueryPort;
import com.tastyhouse.application.banner.port.out.BannerSearchCondition;
import com.tastyhouse.application.banner.port.in.BannerManagementQueryUseCase;

/**
 * 배너 관리 조회 서비스.
 *
 * <p>읽기 포트({@link BannerManagementQueryPort})만 주입해 조회한다. write 포트를 주입하지 않으며,
 * 쓰기는 {@link BannerCommandService}가 담당한다.
 *
 * <p>HTTP 경계에서 {@code String}으로 받은 배너 유형은 여기서 {@code BannerType.from(String)}으로
 * 승격해 검색 조건에 담는다. 파일 정보는 DAO가 조인으로 함께 투영하고 표시용 URL까지 완성해 주므로,
 * 여기서는 파일을 다시 조회하지도 변환하지도 않는다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·{@code FileResponse}·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@AdminApp
@Transactional(readOnly = true)
public class BannerManagementQueryService implements BannerManagementQueryUseCase {

    private final BannerManagementQueryPort bannerManagementQueryPort;

    public BannerManagementQueryService(BannerManagementQueryPort bannerManagementQueryPort) {
        this.bannerManagementQueryPort = bannerManagementQueryPort;
    }

    @Override
    public PageResult<BannerManagementListItemResult> getBanners(String type, String title, Boolean visible, int page, int size) {
        BannerType bannerType = type == null ? null : BannerType.from(type);
        BannerSearchCondition condition = BannerSearchCondition.of(bannerType, title, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        return bannerManagementQueryPort.findAllBanners(condition, pageQuery);
    }

    @Override
    public BannerDetailResult getBanner(Long id) {
        return bannerManagementQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BANNER_NOT_FOUND));
    }
}
