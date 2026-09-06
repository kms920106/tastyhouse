package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 프랜차이즈 배달지역 조정 신청 순수 도메인 모델.
 *
 * <p>가맹점 간 배달지역이 중첩될 때 점주가 가맹본부의 중재를 신청하는 접수 창구다. 플랫폼은 형식적
 * 사항만 확인해 자료를 가맹본부에 전달할 뿐 조정 과정·결과에는 관여하지 않으므로, 상대 가맹점과
 * 가맹본부는 FK가 아니라 <b>신청서에 기재된 텍스트</b>로 받는다(미등록 가맹점도 접수 가능해야 한다).
 *
 * <p><b>{@link #complete()}는 배달가능지역을 반영하지 않는다.</b> {@code ShopImageChangeRequest}가 승인
 * 시 가게 이미지를 즉시 반영하는 것과 이 부분이 다르다 — 조정 성립 여부는 가맹본부가 판정하고 플랫폼은
 * 그 결과만 기록하므로, 실제 배달가능지역 변경은 기존 배달가능지역 등록·삭제 API로 별도 수행한다.
 * 자동 반영으로 만들면 시스템이 조정 성립을 판정하는 셈이 되고, "어느 행정동으로 조정되었는가"는
 * 신청서에 담기지도 않는다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopDeliveryAreaAdjustmentRequestJpaEntity} + {@code ...Mapper}가 담당하며, 더티 체킹이 없으므로
 * 상태 전이 후 저장은 호출부가 명시적으로 {@code save}를 호출해야 한다.
 */
public class ShopDeliveryAreaAdjustmentRequest {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final String counterpartShopName;
    private final String counterpartBusinessNumber;
    private final String franchiseName;
    private final String reason;
    private final UploadedFileId consentFileId; // 조정신청 관련 정보제공 동의서
    private DeliveryAreaAdjustmentStatus status;
    private String rejectReason; // nullable
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopDeliveryAreaAdjustmentRequest(
        Long id,
        ShopId shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        UploadedFileId consentFileId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.counterpartShopName = counterpartShopName;
        this.counterpartBusinessNumber = counterpartBusinessNumber;
        this.franchiseName = franchiseName;
        this.reason = reason;
        this.consentFileId = consentFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 조정 신청을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ShopDeliveryAreaAdjustmentRequest of(
        ShopId shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        UploadedFileId consentFileId
    ) {
        return new ShopDeliveryAreaAdjustmentRequest(
            null,
            shopId,
            counterpartShopName,
            counterpartBusinessNumber,
            franchiseName,
            reason,
            consentFileId,
            DeliveryAreaAdjustmentStatus.PENDING,
            null,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopDeliveryAreaAdjustmentRequest reconstitute(
        Long id,
        ShopId shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        UploadedFileId consentFileId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopDeliveryAreaAdjustmentRequest(
            id,
            shopId,
            counterpartShopName,
            counterpartBusinessNumber,
            franchiseName,
            reason,
            consentFileId,
            status,
            rejectReason,
            createdAt,
            updatedAt
        );
    }

    /**
     * 가맹본부에 자료를 전달해 조정 절차를 시작한다.
     */
    public void startProgress() {
        if (this.status != DeliveryAreaAdjustmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_PENDING);
        }
        this.status = DeliveryAreaAdjustmentStatus.IN_PROGRESS;
    }

    /**
     * 조정 성립을 기록한다. <b>배달가능지역 반영은 이 메서드가 하지 않는다</b>(클래스 Javadoc 참조).
     */
    public void complete() {
        if (this.status != DeliveryAreaAdjustmentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_IN_PROGRESS);
        }
        this.status = DeliveryAreaAdjustmentStatus.COMPLETED;
    }

    /**
     * 반려한다. 접수 대기·조정 중 어느 쪽에서든 반려할 수 있다.
     *
     * <p>{@code PENDING → REJECTED} 직행을 허용하는 이유는 "신청 자체에 의하더라도 배달지역 조정이
     * 필요하지 않음이 명백한 경우 가맹본부의 판단으로 조정절차가 개시되지 않을 수 있다"는 규정 때문이다 —
     * 그런 신청은 IN_PROGRESS를 거치지 않고 바로 종결된다.
     */
    public void reject(String reason) {
        if (this.status == DeliveryAreaAdjustmentStatus.COMPLETED
            || this.status == DeliveryAreaAdjustmentStatus.REJECTED
            || this.status == DeliveryAreaAdjustmentStatus.CANCELED) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_ALREADY_CLOSED);
        }
        this.status = DeliveryAreaAdjustmentStatus.REJECTED;
        this.rejectReason = reason;
    }

    /**
     * 점주가 접수 대기 중인 신청을 스스로 철회한다.
     *
     * <p><b>{@code IN_PROGRESS}는 취소할 수 없다</b> — 이미 가맹본부에 자료가 전달된 뒤라 플랫폼이 일방
     * 취소하면 외부 절차와 시스템 상태가 어긋난다.
     *
     * <p>취소를 인덱스가 아니라 이 애그리거트에 두므로 {@code OPEN_STATUSES}(PENDING·IN_PROGRESS)에
     * CANCELED가 없어 재신청이 자동으로 열리고, {@link #reject(String)}의 종결 조건이 취소된 신청의 반려를
     * 막는다.
     */
    public void cancel() {
        if (this.status != DeliveryAreaAdjustmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_NOT_CANCELABLE);
        }
        this.status = DeliveryAreaAdjustmentStatus.CANCELED;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getCounterpartShopName() {
        return this.counterpartShopName;
    }

    public String getCounterpartBusinessNumber() {
        return this.counterpartBusinessNumber;
    }

    public String getFranchiseName() {
        return this.franchiseName;
    }

    public String getReason() {
        return this.reason;
    }

    public UploadedFileId getConsentFileId() {
        return this.consentFileId;
    }

    public DeliveryAreaAdjustmentStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
