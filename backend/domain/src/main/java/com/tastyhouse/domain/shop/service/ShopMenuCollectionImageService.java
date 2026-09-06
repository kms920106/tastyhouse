package com.tastyhouse.domain.shop.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.repository.ShopMenuCollectionImageRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

/**
 * 메뉴모음컷 등록·검수·배치 워크플로의 단일 소유자.
 *
 * <p><b>검수 대상은 "새 이미지의 내용"이다.</b> 그래서 등록만 승인을 거치고 <b>순서 변경·삭제는 승인
 * 없이 즉시 반영</b>한다 — 배치는 검수할 대상이 아니다. {@code ProductImageApprovalService}가 메뉴
 * 이미지에 대해 세운 원칙을 그대로 따른다.
 *
 * <p>여기 모인 불변식은 모두 <b>행 하나만 보고는 판정할 수 없는 집합 차원 규칙</b>이라 애그리거트가
 * 아니라 도메인 서비스가 소유한다.
 * <ul>
 *   <li>최대 6개 — 선택지가 많으면 손님이 오히려 고민한다</li>
 *   <li>최소 1개 유지 — 원문 규격이 "메뉴모음컷 1개 이상 필수 등록"으로 규정한다</li>
 *   <li>순서 변경은 전체 목록 replace-all — 몇 번째인지 직접 지정하지 않아 순서 충돌이 없다</li>
 * </ul>
 *
 * <p>요청자는 점주(ceo-api), 검수자는 관리자(admin-api)로 액터가 갈리지만 규칙은 하나여야 하므로
 * 도메인 계층에 이 서비스 하나만 둔다.
 */
public class ShopMenuCollectionImageService {

    /**
     * 가게당 등록 가능한 메뉴모음컷 최대 개수.
     *
     * <p>대기·반려 건도 이 정원을 차지한다 — 반려된 것을 지우지 않고 계속 올리면 검수 큐가 한 가게로
     * 채워지고, 점주 화면에도 상태 목록이 무한히 쌓인다.
     */
    private static final int MAX_IMAGE_COUNT = 6;

    private final ShopMenuCollectionImageRepository imageRepository;
    private final ShopRepository shopRepository;

    public ShopMenuCollectionImageService(
        ShopMenuCollectionImageRepository imageRepository,
        ShopRepository shopRepository
    ) {
        this.imageRepository = imageRepository;
        this.shopRepository = shopRepository;
    }

    /**
     * 메뉴모음컷을 등록한다 — {@code PENDING}으로 시작하며 승인 후에만 손님 화면에 노출된다.
     *
     * <p>{@code sort}는 클라이언트가 지정하지 않고 <b>현재 개수를 그대로 부여해 맨 뒤에 붙인다</b>.
     * 새 이미지를 앞에 끼우면 승인만으로 첫 화면 이미지가 바뀌어 점주가 의도하지 않은 교체가 일어난다.
     *
     * @return 생성된 메뉴모음컷 식별자
     */
    public Long register(ShopId shopId, UploadedFileId imageFileId) {
        requireShopExists(shopId);

        List<ShopMenuCollectionImage> current = imageRepository.findAllByShopId(shopId);
        if (current.size() >= MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_LIMIT_EXCEEDED);
        }

        ShopMenuCollectionImage saved =
            imageRepository.save(ShopMenuCollectionImage.of(shopId, imageFileId, current.size()));
        return saved.getId();
    }

    /** 승인한다 — 이 시점부터 손님 화면 최상단에 노출된다. */
    public void approve(ShopMenuCollectionImageId imageId) {
        ShopMenuCollectionImage image = loadImage(imageId);
        image.approve();
        imageRepository.save(image);
    }

    /** 반려한다. 사유는 필수다 — 점주가 무엇을 고쳐 다시 올려야 하는지 알아야 한다. */
    public void reject(ShopMenuCollectionImageId imageId, String rejectReason) {
        ShopMenuCollectionImage image = loadImage(imageId);
        image.reject(rejectReason);
        imageRepository.save(image);
    }

    /**
     * 표시 순서를 통째로 교체한다. <b>승인을 거치지 않는다</b>(검수 대상은 내용이지 배치가 아니다).
     *
     * <p>{@code sort} 값을 받지 않고 <b>순서 있는 id 배열 전체</b>만 받아 서버가 {@code 0..N-1}을
     * 다시 부여한다 — 클라이언트가 "몇 번째"를 직접 지정하면 동시 편집 시 두 이미지가 같은 순서를
     * 갖는 상태가 만들어진다.
     *
     * <p>보낸 목록이 현재 목록과 <b>집합으로 일치하지 않으면</b> 거절한다
     * ({@code SHOP_MENU_COLLECTION_IMAGE_ORDER_TARGET_MISMATCH}). 부분 목록을 허용하면 화면이
     * 낡은 상태에서 보낸 요청이 빠진 이미지를 목록 끝으로 밀어내는데, 점주는 순서만 바꿨다고 믿는다.
     */
    public void reorder(ShopId shopId, List<Long> orderedImageIds) {
        List<ShopMenuCollectionImage> current = imageRepository.findAllByShopId(shopId);
        Set<Long> currentIds = current.stream()
            .map(ShopMenuCollectionImage::getId)
            .collect(Collectors.toSet());
        List<Long> requested = orderedImageIds == null ? List.of()
            : orderedImageIds.stream().filter(Objects::nonNull).distinct().toList();

        if (currentIds.size() != requested.size() || !currentIds.containsAll(requested)) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            Long imageId = requested.get(index);
            ShopMenuCollectionImage image = current.stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND));
            image.changeSort(index);
            imageRepository.save(image);
        }
    }

    /**
     * 메뉴모음컷을 삭제한다. 순서 변경과 마찬가지로 승인을 거치지 않는다.
     *
     * <p><b>승인된 것이 최소 1개는 남아야 한다.</b> 메뉴모음컷은 손님이 가게를 열었을 때 가장 먼저
     * 보는 자리라 0개가 되면 그 자리가 빈 채로 노출된다. 마지막 1개를 지우려는 요청은
     * {@code SHOP_MENU_COLLECTION_IMAGE_LAST_CANNOT_DELETE}로 거절하고, 교체가 필요하면 새로 등록한
     * 뒤 지우게 한다.
     *
     * <p>판정 기준을 <b>전체 건수가 아니라 승인분 건수</b>로 두는 이유는 이 불변식이 지키려는 것이
     * "손님에게 보이는 이미지가 있는가"이기 때문이다. 전체로 세면 승인 1건 + 반려 1건인 가게에서
     * 승인분을 지우는 요청이 통과해(전체가 2건이므로) 손님 화면이 비게 된다 — 반려된 행은 노출되지
     * 않으므로 하한을 지탱하지 못한다. 대기 중인 건도 아직 노출되지 않으므로 같은 이유로 제외한다.
     *
     * <p>삭제 후 남은 것의 {@code sort}를 {@code 0..N-1}로 다시 매긴다 — 재부여하지 않으면 구멍이
     * 생겨 다음 등록이 이미 쓰인 순서를 받는다.
     */
    public void delete(ShopId shopId, ShopMenuCollectionImageId imageId) {
        List<ShopMenuCollectionImage> current = imageRepository.findAllByShopId(shopId);
        ShopMenuCollectionImage target = current.stream()
            .filter(candidate -> candidate.getId().equals(imageId.value()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND));

        // 지운 뒤에도 승인분이 남는지 본다. 대상이 승인분이 아니면 노출 건수가 줄지 않으므로 통과한다.
        if (target.getStatus() == ApprovalStatus.APPROVED && countApproved(current) <= 1) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_LAST_CANNOT_DELETE);
        }
        imageRepository.delete(target);

        renumberSort(current.stream().filter(candidate -> !candidate.getId().equals(imageId.value())).toList());
    }

    /** 승인되어 손님에게 노출되는 건수를 센다. */
    private long countApproved(List<ShopMenuCollectionImage> images) {
        return images.stream().filter(image -> image.getStatus() == ApprovalStatus.APPROVED).count();
    }

    /** 남은 목록에 {@code 0..N-1}을 다시 부여한다. 이미 맞는 값이면 저장을 건너뛴다. */
    private void renumberSort(List<ShopMenuCollectionImage> remaining) {
        for (int index = 0; index < remaining.size(); index++) {
            ShopMenuCollectionImage image = remaining.get(index);
            if (image.getSort() != index) {
                image.changeSort(index);
                imageRepository.save(image);
            }
        }
    }

    private ShopMenuCollectionImage loadImage(ShopMenuCollectionImageId imageId) {
        return imageRepository.findById(imageId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND));
    }

    private void requireShopExists(ShopId shopId) {
        if (shopRepository.findById(shopId).isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        }
    }
}
