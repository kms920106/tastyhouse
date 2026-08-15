package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 주문 라인의 상품·옵션 검증(도메인 서비스, product 컨텍스트 소유).
 *
 * <p>주문 접수가 상품에 대해 확인해야 하는 것 — <b>상품이 실재하는가·판매중지는 아닌가·선택한 옵션과
 * 그 그룹이 실재하는가</b> — 를 모아 수행하고, 주문 라인에 박제할 스냅샷({@link OrderProductSnapshot})을
 * 돌려준다.
 *
 * <p><b>왜 product가 소유하는가</b>: 이 검증은 상품 애그리거트의 규칙이므로 그 소유 컨텍스트가 판정해야
 * 한다. 과거에는 {@code OrderPlacementService}가 product의 모델 3개와 리포지토리 4개를 직접 주입해
 * 검증을 수행했는데, 그러면 "판매중지 상품은 주문할 수 없다" 같은 규칙이 order 안에서 재구현되어
 * product의 판매 정책이 바뀔 때 두 곳을 함께 고쳐야 한다. 지금은 order가 이 서비스 하나만 주입한다.
 *
 * <p><b>동작을 바꾸지 않는다</b> — 검증 순서(상품 로드 → 판매중지 → 대표 이미지 → 옵션 그룹 → 옵션)와
 * 에러코드({@code ORDER_PRODUCT_NOT_FOUND}·{@code ORDER_PRODUCT_SOLD_OUT}·
 * {@code ORDER_OPTION_GROUP_NOT_FOUND}·{@code ORDER_OPTION_NOT_FOUND})는 이관 전과 동일하다.
 * 에러코드 상수명이 {@code ORDER_} 접두인 것은 이 검증이 <b>주문 맥락</b>의 거절 사유이기 때문이며,
 * 응답 {@code code}는 프론트가 분기하는 wire 계약이라 이관을 이유로 바꾸지 않는다.
 *
 * <p><b>같은 트랜잭션 안의 동기 호출이다</b> — 주문 접수는 한 트랜잭션이어야 하므로 이 이관으로
 * 트랜잭션 경계가 바뀌지 않는다(이벤트로 바꾸지 않는다).
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ProductDomainConfig}가 담당한다.
 */
public class OrderProductValidationService {

    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;

    public OrderProductValidationService(
        ProductRepository productRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository
    ) {
        this.productRepository = productRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
    }

    /**
     * 주문 라인들을 <b>요청 순서 그대로</b> 검증해 스냅샷 목록을 돌려준다. 첫 위반에서 즉시 실패한다.
     *
     * <p>순서를 보존하는 이유는 두 가지다 — 실패 시 어느 라인에서 걸렸는지가 요청과 대응되어야 하고,
     * 주문 라인 저장 순서가 화면 표시 순서가 되기 때문이다.
     */
    public List<OrderProductSnapshot> validate(List<OrderLineSelection> lines) {
        List<OrderProductSnapshot> snapshots = new ArrayList<>();
        for (OrderLineSelection line : lines) {
            snapshots.add(validateLine(line));
        }
        return snapshots;
    }

    private OrderProductSnapshot validateLine(OrderLineSelection line) {
        Product product = productRepository.findById(ProductId.of(line.productId()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND,
                ErrorCode.ORDER_PRODUCT_NOT_FOUND.getDefaultMessage() + ": " + line.productId()));

        if (product.isSoldOut()) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_SOLD_OUT,
                ErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + product.getName());
        }

        UploadedFileId representativeImageFileId =
            productImageRepository.findRepresentativeImageFileId(product.getProductId());

        return new OrderProductSnapshot(
            product.getProductId(),
            product.getName(),
            representativeImageFileId,
            line.quantity(),
            product.getOriginalPrice(),
            product.getDiscountPrice(),
            validateOptions(line)
        );
    }

    private List<OrderProductOptionSnapshot> validateOptions(OrderLineSelection line) {
        List<OrderProductOptionSnapshot> options = new ArrayList<>();
        for (OrderLineOptionSelection selected : line.selectedOptions()) {
            ProductOptionGroup optionGroup = productOptionGroupRepository
                .findById(ProductOptionGroupId.of(selected.groupId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_GROUP_NOT_FOUND));

            ProductOption option = productOptionRepository
                .findById(ProductOptionId.of(selected.optionId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND));

            options.add(new OrderProductOptionSnapshot(
                optionGroup.getProductOptionGroupId(),
                optionGroup.getName(),
                option.getProductOptionId(),
                option.getName(),
                option.getAdditionalPrice()
            ));
        }
        return options;
    }
}
