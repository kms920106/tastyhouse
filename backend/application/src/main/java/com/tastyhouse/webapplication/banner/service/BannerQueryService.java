package com.tastyhouse.webapplication.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.banner.port.out.BannerListItemResult;
import com.tastyhouse.application.banner.port.out.BannerQueryPort;
import com.tastyhouse.webapplication.banner.port.in.BannerQueryUseCase;

/**
 * 배너 조회 서비스.
 *
 * <p>회원 노출용 조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다. infra read
 * 어댑터({@link BannerQueryPort})를 주입해 현재 노출 중인 배너만 조회한다. 이미지 URL은 DAO가
 * 완성해 주므로 여기서는 파일을 알지 않고 값을 그대로 읽기 계약에 담아 넘긴다.
 *
 * <p>배너 유형은 노출 위치별 전용 엔드포인트로 고정되어 있어 HTTP 파라미터로 받지 않고 이 서비스가
 * 직접 core enum 상수를 지정한다.
 */
@Service
@Transactional(readOnly = true)
public class BannerQueryService implements BannerQueryUseCase {

    private final BannerQueryPort bannerQueryPort;

    public BannerQueryService(BannerQueryPort bannerQueryPort) {
        this.bannerQueryPort = bannerQueryPort;
    }

    @Override
    public PageResult<BannerListItemResult> getHomeBanners(int page, int size) {
        return getBannersByType(BannerType.HOME, page, size);
    }

    @Override
    public PageResult<BannerListItemResult> getSidebarBanners(int page, int size) {
        return getBannersByType(BannerType.SIDEBAR, page, size);
    }

    private PageResult<BannerListItemResult> getBannersByType(BannerType type, int page, int size) {
        return bannerQueryPort.findVisibleBannersByType(type, PageQuery.of(page, size));
    }
}
