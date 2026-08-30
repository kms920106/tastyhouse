package com.tastyhouse.adminapplication.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapplication.banner.port.in.BannerCommandUseCase;
import com.tastyhouse.adminapplication.banner.port.in.BannerCreateCommand;
import com.tastyhouse.adminapplication.banner.port.in.BannerDeleteCommand;
import com.tastyhouse.adminapplication.banner.port.in.BannerUpdateCommand;
import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.banner.repository.BannerRepository;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 배너 관리 command 서비스.
 *
 * <p>domain write 포트({@link BannerRepository})만 주입해 생성·수정·삭제를 수행한다. 조회는
 * {@link BannerQueryService}가 담당하며, 이 서비스는 infra query DAO를 주입하지 않는다.
 *
 * <p>경계 타입만 담은 command를 받아 {@code BannerType.from(String)}·{@code UploadedFileId.of(Long)}
 * 같은 도메인 승격을 이 서비스에서 수행한다(도메인 enum 경계 규칙). {@code Banner}는 순수 POJO라
 * 더티 체킹이 없으므로 도메인 변경 후 명시적으로 {@code bannerRepository.save(banner)}를 호출한다.
 */
@Service
@Transactional
public class BannerCommandService implements BannerCommandUseCase {

    private final BannerRepository bannerRepository;

    public BannerCommandService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Override
    public Long createBanner(BannerCreateCommand command) {
        Banner banner = Banner.of(
            BannerType.from(command.type()),
            command.title(),
            UploadedFileId.of(command.imageFileId()),
            command.linkUrl(),
            command.startDate(),
            command.endDate(),
            command.sort(),
            command.visible()
        );
        Banner saved = bannerRepository.save(banner);
        return saved.getBannerId().value();
    }

    @Override
    public void updateBanner(BannerUpdateCommand command) {
        BannerId bannerId = BannerId.of(command.bannerId());
        Banner banner = findBannerOrThrow(bannerId);

        banner.update(
            BannerType.from(command.type()),
            command.title(),
            UploadedFileId.of(command.imageFileId()),
            command.linkUrl(),
            command.startDate(),
            command.endDate(),
            command.sort(),
            command.visible()
        );
        bannerRepository.save(banner);
    }

    @Override
    public void deleteBanner(BannerDeleteCommand command) {
        BannerId bannerId = BannerId.of(command.bannerId());
        Banner banner = findBannerOrThrow(bannerId);

        banner.delete();
        bannerRepository.save(banner);
    }

    private Banner findBannerOrThrow(BannerId bannerId) {
        return bannerRepository.findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BANNER_NOT_FOUND));
    }
}
