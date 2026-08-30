package com.tastyhouse.ceoapplication.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.product.response.ProductAvailabilityChangeResponse;
import com.tastyhouse.ceoapplication.product.response.ProductAvailabilityFailureResponse;
import com.tastyhouse.ceoapplication.product.port.in.ProductHideCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductHideUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionHideCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionHideUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionReleaseCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionReleaseUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionSoldOutCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionSoldOutUntilChangeCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionSoldOutUntilChangeUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionSoldOutUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionTargetCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductReleaseCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductReleaseUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductSoldOutCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductSoldOutUntilChangeCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductSoldOutUntilChangeUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductSoldOutUseCase;
import com.tastyhouse.ceoapplication.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.product.model.ProductOptionType;
import com.tastyhouse.domain.product.model.ReleaseTarget;
import com.tastyhouse.domain.product.service.ProductAvailabilityChangeResult;
import com.tastyhouse.domain.product.service.ProductAvailabilityFailure;
import com.tastyhouse.domain.product.service.ProductAvailabilityService;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.service.ShopNextOpenTimeCalculator;
import com.tastyhouse.domain.shop.service.ShopNextOpenTimeContext;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주용 메뉴·옵션 품절·숨김 변경 서비스(CQRS command 측).
 *
 * <p>부분실패 제약과 전이 규칙은 도메인 서비스 {@link ProductAvailabilityService}가 소유하고, 이 서비스는
 * 트랜잭션 경계·소유권 검증·경계 타입 승격(String → {@link ReleaseTarget}, Long → ID VO)과
 * <b>품절 기간 기본값 정책</b>만 담당한다.
 *
 * <p><b>두 컨텍스트의 조립 지점이 여기다</b> — product 도메인 서비스가 {@code ShopBusinessHour}를 직접
 * 참조하면 컨텍스트 경계 위반이므로, 다음 오픈 시각 산출({@link ShopNextOpenTimeCalculator}, shop 컨텍스트)과
 * 품절 전이(product 컨텍스트)를 이 서비스가 각각 주입해 연결한다.
 */
@Service
@Transactional
public class ProductAvailabilityCommandService implements ProductSoldOutUseCase, ProductHideUseCase, ProductReleaseUseCase, ProductSoldOutUntilChangeUseCase, ProductOptionSoldOutUseCase, ProductOptionHideUseCase, ProductOptionReleaseUseCase, ProductOptionSoldOutUntilChangeUseCase {

    /**
     * 다음 오픈 시각을 산출할 수 없을 때의 폴백 — 영업시간 미등록이거나 +7일 내 영업일이 없는 가게다.
     *
     * <p>이 정책이 계산기가 아니라 여기 있는 이유: "오픈 시각을 정할 수 없다"는 사실과 "그러면 얼마로
     * 할까"라는 정책은 서로 다른 판단이므로, 순수 계산기가 정책을 삼키지 않게 한다.
     */
    private static final long FALLBACK_SOLD_OUT_HOURS = 24L;

    /** 공휴일 조회 구간 — 계산기의 탐색 범위(익일~+7일)와 같다. */
    private static final long HOLIDAY_LOOKUP_DAYS = 7L;

    private final ProductAvailabilityService productAvailabilityService;
    private final ShopNextOpenTimeCalculator shopNextOpenTimeCalculator;
    private final ShopDetailRepository shopDetailRepository;
    private final PublicHolidayCalendar publicHolidayCalendar;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductAvailabilityCommandService(
        ProductAvailabilityService productAvailabilityService,
        ShopNextOpenTimeCalculator shopNextOpenTimeCalculator,
        ShopDetailRepository shopDetailRepository,
        PublicHolidayCalendar publicHolidayCalendar,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityService = productAvailabilityService;
        this.shopNextOpenTimeCalculator = shopNextOpenTimeCalculator;
        this.shopDetailRepository = shopDetailRepository;
        this.publicHolidayCalendar = publicHolidayCalendar;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    // ── 메뉴 ────────────────────────────────────────────────────────────────────────

    /**
     * 메뉴를 일괄 품절 처리한다.
     *
     * <p>{@code soldOutUntil}이 {@code null}이면 서버가 다음 영업일 오픈 시각으로 채운다 — 클라이언트가
     * 기본값을 계산하지 않는다(영업시간·휴무일·공휴일을 알아야 하고, 그 규칙은 서버가 소유한다).
     */
    @Override
    public ProductAvailabilityChangeResponse markProductsSoldOut(ProductSoldOutCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();
        LocalDateTime soldOutUntil = command.soldOutUntil();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resolved = resolveSoldOutUntil(shopId, soldOutUntil, now);

        return toChangeResponse(productAvailabilityService.markProductsSoldOut(
            ShopId.of(shopId), toProductIds(productIds), resolved, now));
    }

    @Override
    public ProductAvailabilityChangeResponse hideProducts(ProductHideCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeResponse(productAvailabilityService.hideProducts(ShopId.of(shopId), toProductIds(productIds)));
    }

    @Override
    public ProductAvailabilityChangeResponse releaseProducts(ProductReleaseCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();
        String target = command.target();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeResponse(productAvailabilityService.releaseProducts(
            ShopId.of(shopId), toProductIds(productIds), ReleaseTarget.from(target)));
    }

    @Override
    public ProductAvailabilityChangeResponse changeProductsSoldOutUntil(ProductSoldOutUntilChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();
        LocalDateTime soldOutUntil = command.soldOutUntil();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeResponse(productAvailabilityService.changeProductsSoldOutUntil(
            ShopId.of(shopId), toProductIds(productIds), soldOutUntil, LocalDateTime.now()));
    }

    // ── 옵션 ────────────────────────────────────────────────────────────────────────

    /**
     * 옵션을 일괄 품절 처리한다. {@code soldOutUntil}이 {@code null}이면 메뉴와 동일하게 서버가 채운다.
     */
    @Override
    public ProductAvailabilityChangeResponse markOptionsSoldOut(ProductOptionSoldOutCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> optionIds = command.options().stream().map(ProductOptionTargetCommand::optionId).toList();
        List<String> optionTypes = command.options().stream().map(ProductOptionTargetCommand::optionType).toList();
        LocalDateTime soldOutUntil = command.soldOutUntil();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resolved = resolveSoldOutUntil(shopId, soldOutUntil, now);

        return toChangeResponse(productAvailabilityService.markOptionsSoldOut(
            ShopId.of(shopId), toOptionIds(optionIds, optionTypes), toCommonOptionIds(optionIds, optionTypes),
            resolved, now));
    }

    @Override
    public ProductAvailabilityChangeResponse hideOptions(ProductOptionHideCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> optionIds = command.options().stream().map(ProductOptionTargetCommand::optionId).toList();
        List<String> optionTypes = command.options().stream().map(ProductOptionTargetCommand::optionType).toList();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeResponse(productAvailabilityService.hideOptions(
            ShopId.of(shopId), toOptionIds(optionIds, optionTypes), toCommonOptionIds(optionIds, optionTypes)));
    }

    @Override
    public ProductAvailabilityChangeResponse releaseOptions(ProductOptionReleaseCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> optionIds = command.options().stream().map(ProductOptionTargetCommand::optionId).toList();
        List<String> optionTypes = command.options().stream().map(ProductOptionTargetCommand::optionType).toList();
        String target = command.target();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeResponse(productAvailabilityService.releaseOptions(
            ShopId.of(shopId), toOptionIds(optionIds, optionTypes), toCommonOptionIds(optionIds, optionTypes),
            ReleaseTarget.from(target)));
    }

    @Override
    public ProductAvailabilityChangeResponse changeOptionsSoldOutUntil(ProductOptionSoldOutUntilChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> optionIds = command.options().stream().map(ProductOptionTargetCommand::optionId).toList();
        List<String> optionTypes = command.options().stream().map(ProductOptionTargetCommand::optionType).toList();
        LocalDateTime soldOutUntil = command.soldOutUntil();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeResponse(productAvailabilityService.changeOptionsSoldOutUntil(
            ShopId.of(shopId), toOptionIds(optionIds, optionTypes), toCommonOptionIds(optionIds, optionTypes),
            soldOutUntil, LocalDateTime.now()));
    }

    // ── 기간 기본값 ─────────────────────────────────────────────────────────────────

    /**
     * 지정된 품절 기간이 없으면 다음 영업일 오픈 시각으로 채우고, 그것도 산출할 수 없으면
     * {@code now + 24시간}으로 폴백한다.
     */
    private LocalDateTime resolveSoldOutUntil(Long shopId, LocalDateTime soldOutUntil, LocalDateTime now) {
        if (soldOutUntil != null) {
            return soldOutUntil;
        }

        LocalDate today = now.toLocalDate();
        Set<LocalDate> publicHolidays =
            publicHolidayCalendar.findBetween(today, today.plusDays(HOLIDAY_LOOKUP_DAYS));

        ShopNextOpenTimeContext context = ShopNextOpenTimeContext.of(
            now,
            shopDetailRepository.findBusinessHoursByShopId(shopId),
            shopDetailRepository.findClosedDaysByShopId(shopId),
            publicHolidays
        );

        LocalDateTime nextOpenTime = shopNextOpenTimeCalculator.calculate(context);
        return nextOpenTime != null ? nextOpenTime : now.plusHours(FALLBACK_SOLD_OUT_HOURS);
    }

    /**
     * 도메인 결과를 응답으로 옮긴다.
     *
     * <p>이 변환이 컨트롤러가 아니라 여기 있는 이유: 컨트롤러는 {@code com.tastyhouse.domain..}를
     * import하지 않는다(ArchUnit {@code LayerRulesTest}가 강제). 도메인 타입은 이 서비스 경계에서 멈춘다.
     */
    private ProductAvailabilityChangeResponse toChangeResponse(ProductAvailabilityChangeResult result) {
        List<ProductAvailabilityFailureResponse> failed = result.failed().stream()
            .map(this::toFailureResponse)
            .toList();

        return ProductAvailabilityChangeResponse.from(
            result.succeeded(),
            failed
        );
    }

    private ProductAvailabilityFailureResponse toFailureResponse(ProductAvailabilityFailure failure) {
        return ProductAvailabilityFailureResponse.from(
            failure.id(),
            failure.name(),
            failure.errorCode().getCode(),
            failure.errorCode().getDefaultMessage()
        );
    }

    /**
     * 메뉴 id를 VO로 승격한다.
     *
     * <p>빈 목록을 {@code PRODUCT_AVAILABILITY_TARGET_EMPTY}(400)로 거부한다 — Bean Validation
     * {@code @NotEmpty}가 먼저 걸러 주지만, 그 경로는 일반 검증 실패 응답이라 스펙이 약속한 이
     * {@code code}가 프론트에 전달되지 않는다. 서비스에서 한 번 더 판정해 계약을 실제로 성립시킨다.
     */
    private List<ProductId> toProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }
        return productIds.stream().map(ProductId::of).toList();
    }

    /**
     * 요청의 (id, 종류) 쌍에서 일반 옵션 id만 골라 VO로 승격한다.
     *
     * <p>{@code optionType}을 {@link ProductOptionType}으로 승격시키므로 알 수 없는 값은 이 지점에서
     * {@code PRODUCT_OPTION_TYPE_UNKNOWN}(400)으로 거부된다 — 오타가 조용히 한쪽 갈래로 분류돼 엉뚱한
     * 테이블을 조회하는 일이 없다.
     */
    private List<ProductOptionId> toOptionIds(List<Long> optionIds, List<String> optionTypes) {
        return filterByType(optionIds, optionTypes, ProductOptionType.NORMAL).stream()
            .map(ProductOptionId::of)
            .toList();
    }

    private List<ProductCommonOptionId> toCommonOptionIds(List<Long> optionIds, List<String> optionTypes) {
        return filterByType(optionIds, optionTypes, ProductOptionType.COMMON).stream()
            .map(ProductCommonOptionId::of)
            .toList();
    }

    /**
     * 같은 순서로 넘어온 id·종류 목록에서 원하는 갈래의 id만 추린다.
     */
    private List<Long> filterByType(List<Long> optionIds, List<String> optionTypes, ProductOptionType wanted) {
        if (optionIds == null || optionIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }
        List<Long> filtered = new ArrayList<>();
        for (int i = 0; i < optionIds.size(); i++) {
            if (ProductOptionType.from(optionTypes.get(i)) == wanted) {
                filtered.add(optionIds.get(i));
            }
        }
        return filtered;
    }
}
