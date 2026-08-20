package com.tastyhouse.ceoapi.product;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductExposureService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 메뉴 노출기간 설정 서비스(CQRS command 측).
 *
 * <p>기간 검증({@code endDate < startDate})과 요일 묶음·개별 요일 혼용 금지는 도메인
 * ({@code Product}·{@link ProductExposureService})이 소유하고, 이 서비스는 트랜잭션 경계·소유권
 * 검증·경계 타입 승격(String → {@link DayType})만 담당한다.
 */
@Service
@Transactional
public class ProductExposureCommandService {

    private final ProductExposureService productExposureService;
    private final ProductRepository productRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductExposureCommandService(
        ProductExposureService productExposureService,
        ProductRepository productRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productExposureService = productExposureService;
        this.productRepository = productRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 노출기간(기간 + 요일·시간대)을 통째로 교체한다.
     *
     * <p>요일·시간대 목록의 세 축(요일·시작·종료)을 <b>같은 순서</b>의 병렬 목록으로 받는다 —
     * 컨트롤러는 {@code com.tastyhouse.domain..}를 import하지 않으므로(ArchUnit
     * {@code LayerRulesTest}) {@link DayType} 승격이 이 서비스 몫이기 때문이다.
     */
    public void replaceExposure(
        Long ceoId,
        Long shopId,
        Long productId,
        LocalDate startDate,
        LocalDate endDate,
        List<String> dayTypes,
        List<LocalTime> startTimes,
        List<LocalTime> endTimes
    ) {
        requireOwnedProduct(ceoId, shopId, productId);

        ProductId targetProductId = ProductId.of(productId);
        List<ProductExposureHour> hours = toExposureHours(targetProductId, dayTypes, startTimes, endTimes);
        productExposureService.replaceSchedule(targetProductId, startDate, endDate, hours);
    }

    /**
     * 스케줄을 해제해 상시 노출로 되돌린다.
     *
     * <p>숨김({@code visible})은 건드리지 않는다 — 숨김은 점주의 별개 의사이므로 스케줄 해제가
     * 숨긴 메뉴를 되살리면 안 된다.
     */
    public void clearExposure(Long ceoId, Long shopId, Long productId) {
        requireOwnedProduct(ceoId, shopId, productId);
        productExposureService.clearSchedule(ProductId.of(productId));
    }

    private List<ProductExposureHour> toExposureHours(
        ProductId productId,
        List<String> dayTypes,
        List<LocalTime> startTimes,
        List<LocalTime> endTimes
    ) {
        return IntStream.range(0, dayTypes.size())
            .mapToObj(index -> ProductExposureHour.of(
                productId,
                DayType.from(dayTypes.get(index)),
                startTimes.get(index),
                endTimes.get(index)
            ))
            .toList();
    }

    /**
     * 로그인 점주가 대상 가게의 소유자이고 그 메뉴가 정말 그 가게 것인지 확인한다.
     *
     * <p>가게 소유권만 확인하면 다른 가게의 메뉴 id를 넣은 요청이 통과하므로 두 축을 함께 검증한다.
     * "메뉴 없음"과 "남의 가게 메뉴"를 같은 {@code PRODUCT_NOT_FOUND}(404)로 합쳐 존재 여부가 새지
     * 않게 한다.
     */
    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<Product> found = productRepository.findAllByShopIdAndIdIn(
            ShopId.of(shopId), List.of(ProductId.of(productId)));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
