package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.file.service.FileUploadOwnerCommandService;
import com.tastyhouse.application.product.port.in.ProductImageChangeRequestCommand;
import com.tastyhouse.application.product.port.in.ProductImageCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductImageDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductImageReorderCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductImageApprovalService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주용 메뉴 이미지 변경 서비스(CQRS command 측).
 *
 * <p>검수 워크플로(PENDING 중복 차단·승인 시 목록 말미 추가·순서 replace-all 불변식)는 도메인 서비스
 * {@link ProductImageApprovalService}가 소유하고, 이 서비스는 트랜잭션 경계·소유권 검증·경계 타입
 * 승격(Long → ID VO)과 <b>업로드 전 규격 검증</b>만 담당한다.
 *
 * <p>{@link MultipartFile}을 파라미터로 받는 것은 파일 업로드 경계의 문서화된 예외다 — 규격 검증이
 * 업로드보다 앞서야 하고({@link ProductImageSpecValidator}), {@code domain}은 통과분의 {@code fileId}만
 * 받는다.
 */
@Service
@CeoApp
@Transactional
public class ProductImageCommandService implements ProductImageCommandUseCase {

    private final ProductImageApprovalService productImageApprovalService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductImageSpecValidator productImageSpecValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;

    public ProductImageCommandService(
        ProductImageApprovalService productImageApprovalService,
        ProductRepository productRepository,
        ProductImageRepository productImageRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductImageSpecValidator productImageSpecValidator,
        FileUploadOwnerCommandService fileUploadCommandService
    ) {
        this.productImageApprovalService = productImageApprovalService;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productImageSpecValidator = productImageSpecValidator;
        this.fileUploadCommandService = fileUploadCommandService;
    }

    /**
     * 메뉴 이미지 등록을 요청한다. 규격 통과분만 업로드하므로 규격 미달 파일은 스토리지에 남지 않는다.
     *
     * @return 생성된 검수 요청 식별자
     */
    @Override
    public Long requestImageChange(ProductImageChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        requireOwnedProduct(ceoId, shopId, productId);
        productImageSpecValidator.validate(file);

        Long imageFileId = fileUploadCommandService.upload(file);
        return productImageApprovalService.requestImageChange(
            ProductId.of(productId), UploadedFileId.of(imageFileId)
        );
    }

    /**
     * 메뉴 이미지 순서를 통째로 교체한다. <b>승인을 거치지 않는다</b> — 검수 대상은 새 이미지의 내용이지
     * 배치가 아니다.
     */
    @Override
    public void reorderImages(ProductImageReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        List<Long> imageIds = command.imageIds();

        requireOwnedProduct(ceoId, shopId, productId);
        productImageApprovalService.reorderImages(ProductId.of(productId), imageIds);
    }

    /**
     * 메뉴 이미지를 삭제한다. 순서 변경과 마찬가지로 승인을 거치지 않는다.
     *
     * <p><b>경로에 메뉴·가게 식별자가 없어 역조회가 필수다.</b> 이미지 → 메뉴 → 가게 순으로 거슬러
     * 올라가 body의 {@code shopId}가 실제 소유 가게와 일치하는지 확인한다 — 이 확인을 빠뜨리면
     * 자기 가게 하나만 가진 점주가 임의의 {@code imageId}로 남의 가게 이미지를 지울 수 있다(IDOR).
     * 이 저장소는 배달가능지역 삭제에서 정확히 그 형태의 사고를 낸 전례가 있다.
     *
     * <p>"이미지가 없음"과 "남의 가게 이미지"를 같은 {@code PRODUCT_IMAGE_NOT_FOUND}(404)로 합친다 —
     * 코드가 갈리면 존재 여부가 새어 나가 식별자 열거에 쓰인다.
     */
    @Override
    public void deleteImage(ProductImageDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long imageId = command.imageId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductImage image = productImageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
        if (notOwnedBy(shopId, image.getProductId())) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }

        productImageRepository.delete(image);
    }

    /**
     * 로그인 점주가 대상 가게의 소유자이고 그 메뉴가 정말 그 가게 것인지 확인한다.
     *
     * <p>가게 소유권만 확인하고 끝내면 <b>다른 가게의 메뉴 id</b>를 넣은 요청이 통과한다 — 두 축을
     * 반드시 함께 검증한다.
     */
    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        if (notOwnedBy(shopId, ProductId.of(productId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private boolean notOwnedBy(Long shopId, ProductId productId) {
        List<Product> found = productRepository.findAllByShopIdAndIdIn(ShopId.of(shopId), List.of(productId));
        return found.isEmpty();
    }
}
