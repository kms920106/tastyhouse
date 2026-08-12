package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 요청처리 현황 인덱스 JPA 영속 모델. 순수 도메인 모델 {@code ShopRequestIndex}와 분리된 영속 전용
 * 엔티티다.
 *
 * <p>enum 필드는 {@code @Enumerated(EnumType.STRING)}과 {@code columnDefinition = "VARCHAR(n)"}을
 * 병기한다 — {@code columnDefinition}을 빠뜨리면 Hibernate 6의 {@code MySQLDialect}가 네이티브
 * {@code ENUM(...)}을 기대해 {@code ddl-auto=validate}에서 부팅이 실패한다. {@code n}은 {@code schema.sql}과
 * 일치해야 한다(requestType 40, status 20).
 *
 * <p><b>두 컬럼의 한계</b>: 두 원본 테이블 모두 처리 시각 전용 컬럼과 요청자 컬럼이 없다. 따라서
 * {@code processed_at}은 신규 전이 시점부터 정확하고 <b>백필분은 {@code updated_at} 근사치</b>이며,
 * {@code requested_by_ceo_id}는 <b>백필분이 전부 NULL</b>이다.
 *
 * <p>{@code UNIQUE (request_type, source_request_id)}가 동기화 멱등성의 구조적 보증이다 — 배선 중복이
 * 조용한 중복행이 되지 않고 즉시 드러난다.
 */
@Entity
@Table(name = "SHOP_REQUEST_INDEX")
public class ShopRequestIndexJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK — 요청의 대외 식별자

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private ShopRequestType requestType; // 요청 유형 (TRADEMARK_CHANGE, THUMBNAIL_CHANGE, DELIVERY_AREA_ADJUSTMENT)

    @Column(name = "source_request_id", nullable = false)
    private Long sourceRequestId; // 원본 요청 행 ID (유형별 원본 테이블의 id 참조)

    @Column(name = "summary", nullable = false)
    private String summary; // 요청 내용 한 줄 요약

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopRequestStatus status; // 통합 상태 (PENDING, IN_PROGRESS, APPROVED, REJECTED, CANCELED)

    @Column(name = "reject_reason", length = 500)
    private String rejectReason; // 반려 사유 (REJECTED일 때만)

    @Column(name = "attachment_file_id")
    private Long attachmentFileId; // 첨부 파일 ID (UPLOADED_FILE.id 참조)

    @Column(name = "requested_by_ceo_id")
    private Long requestedByCeoId; // 요청 점주 ID (CEO.id 참조 / 백필분은 NULL)

    @Column(name = "processed_at")
    private LocalDateTime processedAt; // 최근 상태 전이 시각 (접수 직후 NULL)

    protected ShopRequestIndexJpaEntity() {
    }

    private ShopRequestIndexJpaEntity(
        Long shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        ShopRequestStatus status,
        String rejectReason,
        Long attachmentFileId,
        Long requestedByCeoId,
        LocalDateTime processedAt
    ) {
        this.shopId = shopId;
        this.requestType = requestType;
        this.sourceRequestId = sourceRequestId;
        this.summary = summary;
        this.status = status;
        this.rejectReason = rejectReason;
        this.attachmentFileId = attachmentFileId;
        this.requestedByCeoId = requestedByCeoId;
        this.processedAt = processedAt;
    }

    static ShopRequestIndexJpaEntity create(
        Long shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        ShopRequestStatus status,
        String rejectReason,
        Long attachmentFileId,
        Long requestedByCeoId,
        LocalDateTime processedAt
    ) {
        return new ShopRequestIndexJpaEntity(shopId, requestType, sourceRequestId, summary, status, rejectReason,
            attachmentFileId, requestedByCeoId, processedAt);
    }

    /**
     * 상태 동기화 결과를 managed 엔티티에 복사한다(load-copy-save). 접수 시점에 확정되는 값
     * ({@code shopId}·{@code requestType}·{@code sourceRequestId}·{@code summary}·첨부·요청자)은 전이로
     * 바뀌지 않으므로 복사 대상이 아니다.
     */
    void applyChanges(ShopRequestStatus status, String rejectReason, LocalDateTime processedAt) {
        this.status = status;
        this.rejectReason = rejectReason;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
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

    public Long getAttachmentFileId() {
        return this.attachmentFileId;
    }

    public Long getRequestedByCeoId() {
        return this.requestedByCeoId;
    }

    public LocalDateTime getProcessedAt() {
        return this.processedAt;
    }
}
