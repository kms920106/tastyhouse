package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 요청처리 현황 인덱스 — <b>파생 읽기모델이고 진실원은 원본 애그리거트다.</b>
 *
 * <p>유형별 요청 테이블({@code SHOP_IMAGE_CHANGE_REQUEST},
 * {@code SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST})이 분리돼 있어 통합 목록을 만들려면 UNION 아니면 인덱스
 * 테이블인데, UNION은 QueryDSL 페이징이 까다롭고 유형이 늘 때마다 조회 쿼리를 손봐야 한다. 더 결정적인
 * 이유는 <b>이 행의 id가 요청의 유일한 대외 식별자</b>가 되어 상세·취소·댓글의 모든 URL이
 * {@code requestId} 하나만 쓴다는 점이다 — 요청 유형별 FK 없는 범용 댓글 스레드가 이 단일 식별자 위에서만
 * 성립한다.
 *
 * <p><b>이 행의 값을 신뢰의 근거로 쓰지 말 것.</b> 상세 조회는 여기서 {@code requestType}/
 * {@code sourceRequestId}만 얻어 유형별 원본을 투영하고 {@code status}·{@code rejectReason}도 원본 값으로
 * 응답한다. 그래서 drift가 생겨도 영향 범위가 "목록 배지" 하나로 좁혀진다. 갱신은 원본 상태 전이와
 * <b>같은 트랜잭션</b>에서 {@code ShopRequestIndexRecorder}가 수행하며, 도메인 이벤트·
 * {@code AFTER_COMMIT} 리스너를 쓰지 않는다(기록 유실이 곧 "요청이 목록에서 사라짐"이다).
 *
 * <p><b>두 필드의 한계</b>: 두 원본 테이블 모두 처리 시각 전용 컬럼과 요청자 컬럼이 없다. 따라서
 * {@code processedAt}은 신규 전이 시점부터 정확하고 백필분은 {@code updated_at} 근사치이며,
 * {@code requestedByCeoId}는 백필분이 전부 {@code null}이다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopRequestIndexJpaEntity} + {@code ShopRequestIndexMapper}가 담당하며, 더티 체킹이 없으므로
 * {@link #syncStatus} 후 저장은 호출부가 명시적으로 {@code save}를 호출해야 한다.
 */
public class ShopRequestIndex {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final ShopRequestType requestType;
    private final Long sourceRequestId; // 원본 요청 행 ID
    private final String summary;
    private ShopRequestStatus status;
    private String rejectReason; // nullable — REJECTED일 때만
    private final UploadedFileId attachmentFileId; // nullable
    private final Long requestedByCeoId; // nullable — 백필분은 null
    private LocalDateTime processedAt; // nullable — 접수 직후 null
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재

    private ShopRequestIndex(
        Long id,
        ShopId shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        ShopRequestStatus status,
        String rejectReason,
        UploadedFileId attachmentFileId,
        Long requestedByCeoId,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.requestType = requestType;
        this.sourceRequestId = sourceRequestId;
        this.summary = summary;
        this.status = status;
        this.rejectReason = rejectReason;
        this.attachmentFileId = attachmentFileId;
        this.requestedByCeoId = requestedByCeoId;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 요청 접수분 인덱스 행을 만든다. 접수 직후이므로 상태는 {@code PENDING}이고 처리 시각·반려 사유는 없다.
     */
    public static ShopRequestIndex of(
        ShopId shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        UploadedFileId attachmentFileId,
        Long requestedByCeoId
    ) {
        return new ShopRequestIndex(
            null,
            shopId,
            requestType,
            sourceRequestId,
            summary,
            ShopRequestStatus.PENDING,
            null,
            attachmentFileId,
            requestedByCeoId,
            null,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopRequestIndex reconstitute(
        Long id,
        ShopId shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        ShopRequestStatus status,
        String rejectReason,
        UploadedFileId attachmentFileId,
        Long requestedByCeoId,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopRequestIndex(
            id,
            shopId,
            requestType,
            sourceRequestId,
            summary,
            status,
            rejectReason,
            attachmentFileId,
            requestedByCeoId,
            processedAt,
            createdAt,
            updatedAt
        );
    }

    /**
     * 원본의 상태 전이를 인덱스에 반영한다. <b>전이 판정은 하지 않는다</b> — 이 행은 파생 읽기모델이므로
     * 불변식은 원본 애그리거트가 이미 검증했고, 여기서 다시 막으면 두 판정이 어긋날 여지만 생긴다.
     *
     * @param rejectReason 반려 사유. 반려가 아닌 전이면 {@code null}
     * @param processedAt 전이 시각
     */
    public void syncStatus(ShopRequestStatus status, String rejectReason, LocalDateTime processedAt) {
        this.status = status;
        this.rejectReason = rejectReason;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopRequestType getRequestType() {
        return this.requestType;
    }

    public Long getSourceRequestId() {
        return this.sourceRequestId;
    }

    public String getSummary() {
        return this.summary;
    }

    public ShopRequestStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public UploadedFileId getAttachmentFileId() {
        return this.attachmentFileId;
    }

    public Long getRequestedByCeoId() {
        return this.requestedByCeoId;
    }

    public LocalDateTime getProcessedAt() {
        return this.processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
