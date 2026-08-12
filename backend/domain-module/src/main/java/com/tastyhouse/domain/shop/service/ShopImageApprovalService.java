package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 가게 이미지(상표·대표이미지) 변경 승인 워크플로 불변식(도메인 서비스).
 *
 * <p>이미지 변경은 "점주 요청 → 관리자 검수 → 승인 시 가게에 반영"이라는 워크플로를 따르며, 그 규칙은
 * 요청자(ceo)와 검수자(admin)가 서로 다른 액터임에도 동일하게 유지되어야 한다. 특히 승인
 * ({@link #approveImageChange(Long)})은 <b>요청 애그리거트의 상태 전이와 가게 애그리거트의 이미지
 * 교체가 한 트랜잭션에서 반드시 함께</b> 일어나야 하는 원자 연산이다(둘 중 하나만 반영되면 "승인됐는데
 * 이미지가 안 바뀐" 상태가 남는다). {@code ShopImageChangeRequest}와 {@code Shop} 두 애그리거트 타입을
 * 함께 load &amp; save 하는 불변식 오케스트레이션(분류 C)이므로 도메인 계층에 둔다.
 *
 * <p>같은 가게·같은 이미지 유형에 PENDING 요청이 2건 생기지 않도록 요청 생성 시 중복을 막고
 * ({@code SHOP_IMAGE_CHANGE_REQUEST_ALREADY_PENDING}), 진행 중 요청이 있으면 가게 노출정지 변경을
 * 차단하는 판정({@link #existsPendingByShopId(Long)})도 이 서비스가 제공한다.
 *
 * <p><b>변경이력 기록도 이 서비스가 소유한다</b>({@code TRADEMARK_CHANGE_REQUEST}·
 * {@code THUMBNAIL_CHANGE_REQUEST}). 기록 시점은 <b>점주의 요청</b>이며 관리자의 승인·반려는 검수 조치라
 * 이력에 담지 않는다 — 이 이력은 "점주가 무엇을 바꿨는가"를 답하는 자료이고, 검수 결과는 변경요청
 * 애그리거트 자신의 상태({@link ApprovalStatus})가 이미 보존한다.
 *
 * <p><b>요청처리 현황 인덱스 동기화도 이 서비스가 소유한다</b>({@link ShopRequestIndexRecorder}). 변경이력과
 * 달리 <b>모든 상태 전이</b>(요청·승인·반려)를 기록한다 — 인덱스는 "내가 낸 요청이 어떻게 처리됐는가"를
 * 답하는 통합 목록이라 검수 결과가 곧 그 목록의 본문이기 때문이다. Recorder는 생성자 필수 의존이므로 새
 * 전이 메서드를 추가하면 여기서 동기화를 배선해야 한다는 것이 컴파일 단계에서 드러난다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p>도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 */
public class ShopImageApprovalService {

    private final ShopImageChangeRequestRepository shopImageChangeRequestRepository;
    private final ShopRepository shopRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopImageApprovalService(
        ShopImageChangeRequestRepository shopImageChangeRequestRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopImageChangeRequestRepository = shopImageChangeRequestRepository;
        this.shopRepository = shopRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    /**
     * 이미지 변경을 요청한다. 같은 가게·같은 이미지 유형에 이미 PENDING 요청이 있으면 거부한다.
     *
     * <p>변경이력을 <b>요청 시점</b>에 남긴다({@code TRADEMARK_CHANGE_REQUEST} /
     * {@code THUMBNAIL_CHANGE_REQUEST}, 이미지 유형에 따라 갈린다). 점주가 한 행동은 "변경 신청"이므로
     * 액션 유형은 {@code CREATE}이고 변경 전 값은 없다 — 실제 이미지 교체는 관리자 승인
     * ({@link #approveImageChange(Long)}) 시점에 일어나는 별개 사건이고, 그쪽은 관리자 검수 조치라
     * 이 이력에 담지 않는다(반려도 마찬가지다).
     *
     * @return 생성된 변경요청 식별자
     */
    public Long requestImageChange(Long shopId, ShopImageType imageType, Long imageFileId, ShopChangeActor actor) {
        if (shopImageChangeRequestRepository.existsByShopIdAndImageTypeAndStatus(shopId, imageType, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_ALREADY_PENDING);
        }

        ShopImageChangeRequest saved = shopImageChangeRequestRepository.save(
            ShopImageChangeRequest.of(ShopId.of(shopId), imageType, UploadedFileId.of(imageFileId))
        );

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            changeTypeOf(imageType),
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeImageChangeRequest(imageType, imageFileId)
        );

        shopRequestIndexRecorder.record(
            ShopId.of(shopId),
            requestTypeOf(imageType),
            saved.getId(),
            describeImageChangeRequest(imageType, imageFileId),
            saved.getImageFileId(),
            actor.actorId()
        );
        return saved.getId();
    }

    /**
     * 이미지 유형에 대응하는 요청처리 현황 유형. 상표와 대표이미지가 통합 목록에서 갈리는 지점이다.
     */
    private ShopRequestType requestTypeOf(ShopImageType imageType) {
        return imageType == ShopImageType.TRADEMARK
            ? ShopRequestType.TRADEMARK_CHANGE
            : ShopRequestType.THUMBNAIL_CHANGE;
    }

    /**
     * 이미지 유형에 대응하는 변경이력 중분류. 상표와 대표이미지는 화면상 별개 메뉴이므로 분류도 갈린다.
     */
    private ShopChangeType changeTypeOf(ShopImageType imageType) {
        return imageType == ShopImageType.TRADEMARK
            ? ShopChangeType.TRADEMARK_CHANGE_REQUEST
            : ShopChangeType.THUMBNAIL_CHANGE_REQUEST;
    }

    /**
     * 이미지 변경요청을 한 줄로 요약한다(예: {@code "상표 변경요청(파일 #4821)"}).
     *
     * <p>파일 이름이 아니라 식별자를 적는다 — 도메인은 파일 저장소를 모르고, 이름은 업로드 시점 값이라
     * 나중에 같은 파일을 다시 특정하는 근거로는 ID가 낫다.
     */
    private String describeImageChangeRequest(ShopImageType imageType, Long imageFileId) {
        return changeTypeOf(imageType).getDescription() + "(파일 #" + imageFileId + ")";
    }

    /**
     * 이미지 변경요청을 승인하고, 승인된 이미지를 가게에 즉시 반영한다(원자 연산).
     *
     * <p>요청 인덱스 동기화를 <b>이미지 반영이 끝난 뒤 마지막에</b> 한다. 지금은 전체가 한 트랜잭션이라
     * 순서를 바꿔도 결과가 같지만(중간 실패 시 통째로 롤백된다), 인덱스는 "요청이 어떻게 처리됐는가"를
     * 답하는 기록이므로 <b>승인이 실제로 반영된 뒤</b>에 APPROVED가 되는 순서가 그 의미와 맞는다. 나중에
     * 트랜잭션 경계가 쪼개지면(예: 반영을 리스너로 옮기거나 관리자 일괄 처리가 건별 커밋으로 바뀌면)
     * 이 순서만이 "인덱스는 승인인데 가게 이미지는 그대로"를 막는다.
     */
    public void approveImageChange(Long id) {
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.approve();
        shopImageChangeRequestRepository.save(shopImageChangeRequest);

        ShopId shopId = shopImageChangeRequest.getShopId();
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (shopImageChangeRequest.getImageType() == ShopImageType.TRADEMARK) {
            shop.changeTrademarkImage(shopImageChangeRequest.getImageFileId());
        } else {
            shop.changeThumbnailImage(shopImageChangeRequest.getImageFileId());
        }
        shopRepository.save(shop);

        shopRequestIndexRecorder.syncImageChangeStatus(
            requestTypeOf(shopImageChangeRequest.getImageType()),
            id,
            shopImageChangeRequest.getStatus(),
            null
        );
    }

    /**
     * 이미지 변경요청을 반려한다. 가게 이미지는 바뀌지 않는다.
     */
    public void rejectImageChange(Long id, String reason) {
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.reject(reason);
        shopImageChangeRequestRepository.save(shopImageChangeRequest);
        shopRequestIndexRecorder.syncImageChangeStatus(
            requestTypeOf(shopImageChangeRequest.getImageType()),
            id,
            shopImageChangeRequest.getStatus(),
            reason
        );
    }

    /**
     * 그 가게에 진행 중(PENDING)인 이미지 변경요청이 있는지. 노출정지 변경 차단 판정에 쓰인다.
     */
    public boolean existsPendingByShopId(Long shopId) {
        return shopImageChangeRequestRepository.existsByShopIdAndStatus(shopId, ApprovalStatus.PENDING);
    }
}
