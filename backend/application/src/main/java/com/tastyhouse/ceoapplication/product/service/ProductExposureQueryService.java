package com.tastyhouse.ceoapplication.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.out.ProductExposureViewResult;
import com.tastyhouse.ceoapplication.product.port.in.ProductExposureQueryUseCase;
import com.tastyhouse.ceoapplication.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.service.ProductExposureResult;
import com.tastyhouse.domain.product.service.ProductExposureService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.ProductExposurePeriodResult;
import com.tastyhouse.application.product.port.out.ProductOwnerQueryPort;

/**
 * 점주용 메뉴 노출기간 조회 서비스(CQRS query 측).
 *
 * <p>설정값(기간 축)은 query DAO 투영에서, 요일·시간대와 판정은 도메인 서비스
 * {@link ProductExposureService}에서 얻는다 — 판정은 순수 계산기가 소유해야 목록 SQL 술어와
 * 같은 규칙을 공유한다.
 *
 * <p><b>기준 시각은 이 서비스가 정한다</b> — 계산기는 시계도 타임존도 갖지 않으므로
 * 서비스 타임존({@code Asia/Seoul})의 현재 시각과 공휴일 판정을 여기서 해석해 넘긴다.
 */
@Service
@Transactional(readOnly = true)
public class ProductExposureQueryService implements ProductExposureQueryUseCase {

    /** 노출 판정 기준 타임존 — 목록 SQL 술어({@code ProductOwnerQueryPort})와 같은 값이어야 한다. */
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final ProductExposureService productExposureService;
    private final PublicHolidayCalendar publicHolidayCalendar;
    private final ProductOwnerQueryPort productOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductExposureQueryService(
        ProductExposureService productExposureService,
        PublicHolidayCalendar publicHolidayCalendar,
        ProductOwnerQueryPort productOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productExposureService = productExposureService;
        this.publicHolidayCalendar = publicHolidayCalendar;
        this.productOwnerQueryPort = productOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductExposureViewResult getExposure(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductExposurePeriodResult period = productOwnerQueryPort.findExposurePeriod(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!period.shopId().equals(shopId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        ProductId targetProductId = ProductId.of(productId);
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE);
        LocalDate today = now.toLocalDate();
        ProductExposureResult result = productExposureService.evaluate(
            targetProductId,
            now,
            publicHolidayCalendar.isPublicHoliday(today),
            publicHolidayCalendar.isPublicHoliday(today.minusDays(1))
        );

        List<ProductExposureHour> hours = productExposureService.findHours(targetProductId);

        return new ProductExposureViewResult(
            period.startDate(),
            period.endDate(),
            hours.stream()
                .map(hour -> new ProductExposureViewResult.Hour(
                    hour.getDayType().name(),
                    hour.getStartTime(),
                    hour.getEndTime()
                ))
                .toList(),
            result.exposed(),
            result.hiddenReason()
        );
    }


}
