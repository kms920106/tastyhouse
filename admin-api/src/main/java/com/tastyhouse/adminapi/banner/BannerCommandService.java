package com.tastyhouse.adminapi.banner;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.domain.model.Banner;
import com.tastyhouse.domain.banner.domain.model.BannerType;
import com.tastyhouse.domain.banner.domain.repository.BannerRepository;
import com.tastyhouse.domain.banner.domain.vo.BannerId;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 배너 관리 command 서비스.
 *
 * <p>domain write 포트({@link BannerRepository})만 주입해 생성·수정·삭제를 수행한다. 조회는
 * {@link BannerQueryService}가 담당하며, 이 서비스는 infra query DAO를 주입하지 않는다.
 *
 * <p>HTTP 경계에서 {@code String}으로 받은 배너 유형은 이 서비스에서 {@code BannerType.from(String)}
 * 으로 승격한다(도메인 enum 경계 규칙). {@code Banner}는 순수 POJO라 더티 체킹이 없으므로 도메인
 * 변경 후 명시적으로 {@code bannerRepository.save(banner)}를 호출한다.
 */
@Service
@Transactional
public class BannerCommandService {

    private final BannerRepository bannerRepository;

    public BannerCommandService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public Long createBanner(
        String type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible
    ) {
        Banner banner = Banner.of(
            BannerType.from(type),
            title,
            UploadedFileId.of(imageFileId),
            linkUrl,
            startDate,
            endDate,
            sort,
            visible
        );
        Banner saved = bannerRepository.save(banner);
        return saved.getBannerId().value();
    }

    public void updateBanner(
        Long id,
        String type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible
    ) {
        BannerId bannerId = BannerId.of(id);
        Banner banner = findBannerOrThrow(bannerId);

        banner.update(
            BannerType.from(type),
            title,
            UploadedFileId.of(imageFileId),
            linkUrl,
            startDate,
            endDate,
            sort,
            visible
        );
        bannerRepository.save(banner);
    }

    public void deleteBanner(Long id) {
        BannerId bannerId = BannerId.of(id);
        Banner banner = findBannerOrThrow(bannerId);

        banner.delete();
        bannerRepository.save(banner);
    }

    private Banner findBannerOrThrow(BannerId bannerId) {
        return bannerRepository.findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BANNER_NOT_FOUND));
    }
}
