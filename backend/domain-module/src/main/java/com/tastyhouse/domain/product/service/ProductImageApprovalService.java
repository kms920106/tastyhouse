package com.tastyhouse.domain.product.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.repository.ProductImageChangeRequestRepository;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴 이미지 등록·변경 승인 워크플로의 단일 소유자.
 * 기존 {@code ShopImageApprovalService} 패턴을 본뜬다.
 *
 * <p><b>검수 대상은 "새 이미지의 내용"이다.</b> 그래서 등록만 승인을 거치고
 * <b>순서 변경·삭제는 승인 없이 즉시 반영</b>한다 — 배치는 검수할 대상이 아니다.
 *
 * <p>승인 시 {@code PRODUCT_IMAGE}에 행을 <b>추가</b>한다. {@code PRODUCT}에 이미지 컬럼이 없고
 * 대표 이미지는 "노출 중 최소 sort 1장"으로 자동 결정되므로, 별도의 "대표 이미지 교체" 전이가 없다.
 */
public class ProductImageApprovalService {

    private final ProductImageChangeRequestRepository requestRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public ProductImageApprovalService(
        ProductImageChangeRequestRepository requestRepository,
        ProductImageRepository productImageRepository,
        ProductRepository productRepository
    ) {
        this.requestRepository = requestRepository;
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    /**
     * 이미지 등록을 요청한다. 같은 메뉴에 이미 검수 대기 중인 요청이 있으면 거부한다 —
     * PENDING 2건이 생기면 승인 순서에 따라 결과가 갈린다.
     *
     * @return 생성된 변경요청 식별자
     */
    public Long requestImageChange(ProductId productId, UploadedFileId imageFileId) {
        requireProductExists(productId);
        if (requestRepository.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_ALREADY_PENDING);
        }

        ProductImageChangeRequest saved =
            requestRepository.save(ProductImageChangeRequest.of(productId, imageFileId));
        return saved.getId();
    }

    /**
     * 승인한다 — 요청된 이미지를 그 메뉴의 이미지 목록 <b>맨 뒤</b>에 추가한다.
     *
     * <p>맨 뒤에 넣는 이유는 대표 이미지가 "노출 중 최소 sort"로 결정되기 때문이다. 맨 앞에 넣으면
     * 승인만으로 대표 이미지가 바뀌어 점주가 의도하지 않은 교체가 일어난다.
     */
    public void approve(ProductImageChangeRequestId requestId) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.approve();
        requestRepository.save(request);

        int nextSort = productImageRepository.findAllByProductId(request.getProductId()).size();
        productImageRepository.save(
            ProductImage.of(request.getProductId(), request.getImageFileId(), nextSort, true));
    }

    /** 반려한다. 사유는 필수다 — 점주가 무엇을 고쳐 다시 올려야 하는지 알아야 한다. */
    public void reject(ProductImageChangeRequestId requestId, String rejectReason) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestRepository.save(request);
    }

    /** 점주가 검수 대기 중인 요청을 취소한다. 취소하면 같은 메뉴에 새 요청을 낼 수 있다. */
    public void cancel(ProductImageChangeRequestId requestId) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.cancel();
        requestRepository.save(request);
    }

    /**
     * 이미지 순서를 통째로 교체한다. <b>승인을 거치지 않는다</b>(검수 대상은 내용이지 배치가 아니다).
     *
     * <p>{@code sort} 값을 받지 않고 순서 있는 id 배열만 받아 {@code 0..N-1}을 부여한다.
     */
    public void reorderImages(ProductId productId, List<Long> orderedImageIds) {
        List<ProductImage> current = productImageRepository.findAllByProductId(productId);
        Set<Long> currentIds = current.stream().map(ProductImage::getId).collect(Collectors.toSet());
        List<Long> requested = orderedImageIds == null ? List.of()
            : orderedImageIds.stream().filter(Objects::nonNull).distinct().toList();

        if (currentIds.size() != requested.size() || !currentIds.containsAll(requested)) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            Long imageId = requested.get(index);
            ProductImage image = current.stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
            productImageRepository.save(rebuildWithSort(image, index));
        }
    }

    /**
     * {@code ProductImage}는 전이 메서드가 없는 불변 애그리거트라, 순서 변경은 같은 식별자로
     * 재구성한 인스턴스를 저장해 반영한다 — 모델에 setter를 뚫지 않기 위한 선택이다.
     */
    private ProductImage rebuildWithSort(ProductImage image, int sort) {
        return ProductImage.reconstitute(
            image.getId(),
            image.getProductId(),
            image.getImageFileId(),
            sort,
            image.isVisible()
        );
    }

    private ProductImageChangeRequest loadRequest(ProductImageChangeRequestId requestId) {
        return requestRepository.findById(requestId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_NOT_FOUND));
    }

    private void requireProductExists(ProductId productId) {
        if (productRepository.findById(productId).isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
