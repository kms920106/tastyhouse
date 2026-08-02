package com.tastyhouse.adminapi.banner;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.domain.model.BannerType;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.banner.query.BannerDetailResult;
import com.tastyhouse.infrastructure.banner.query.BannerManagementListItemResult;
import com.tastyhouse.infrastructure.banner.query.BannerQueryDao;
import com.tastyhouse.infrastructure.banner.query.BannerSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.banner.response.BannerDetailResponse;
import com.tastyhouse.adminapi.banner.response.BannerListItemResponse;
import com.tastyhouse.adminapi.file.response.FileResponse;

/**
 * 배너 관리 조회 서비스.
 *
 * <p>infra read 어댑터({@link BannerQueryDao})만 주입해 조회하고 Response를 조립한다. write 포트를
 * 주입하지 않으며, 쓰기는 {@link BannerCommandService}가 담당한다.
 *
 * <p>HTTP 경계에서 {@code String}으로 받은 배너 유형은 여기서 {@code BannerType.from(String)}으로
 * 승격해 검색 조건에 담는다. 파일 정보는 DAO가 조인으로 함께 투영하고 표시용 URL까지 완성해 주므로,
 * 여기서는 파일을 다시 조회하지도 변환하지도 않고 {@code FileResponse}로 묶기만 한다.
 */
@Service
@Transactional(readOnly = true)
public class BannerQueryService {

    private final BannerQueryDao bannerQueryDao;

    public BannerQueryService(BannerQueryDao bannerQueryDao) {
        this.bannerQueryDao = bannerQueryDao;
    }

    public PaginationResponse<BannerListItemResponse> getBanners(String type, String title, Boolean visible, int page, int size) {
        BannerType bannerType = type == null ? null : BannerType.from(type);
        BannerSearchCondition condition = BannerSearchCondition.of(bannerType, title, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<BannerListItemResponse> pageResult = bannerQueryDao.findAllBanners(condition, pageQuery)
            .map(this::toBannerListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public BannerDetailResponse getBanner(Long id) {
        BannerDetailResult bannerDetail = bannerQueryDao.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BANNER_NOT_FOUND));
        return toBannerDetailResponse(bannerDetail);
    }

    private BannerListItemResponse toBannerListItemResponse(BannerManagementListItemResult dto) {
        return BannerListItemResponse.from(
            dto.id(),
            dto.type().name(),
            dto.title(),
            toFileResponse(dto.imageFileId(), dto.imageFileName(), dto.imageUrl()),
            dto.linkUrl(),
            dto.startDate(),
            dto.endDate(),
            dto.sort(),
            dto.visible()
        );
    }

    private BannerDetailResponse toBannerDetailResponse(BannerDetailResult dto) {
        return BannerDetailResponse.from(
            dto.id(),
            dto.type().name(),
            dto.title(),
            toFileResponse(dto.imageFileId(), dto.imageFileName(), dto.imageUrl()),
            dto.linkUrl(),
            dto.startDate(),
            dto.endDate(),
            dto.sort(),
            dto.visible(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }

    private FileResponse toFileResponse(Long imageFileId, String imageFileName, String imageUrl) {
        if (imageFileId == null) {
            return null;
        }
        return FileResponse.of(imageFileId, imageFileName, imageUrl);
    }
}
