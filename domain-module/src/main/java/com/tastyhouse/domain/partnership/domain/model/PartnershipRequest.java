package com.tastyhouse.domain.partnership.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.partnership.domain.vo.PartnershipRequestId;

/**
 * 제휴 문의 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code PartnershipRequestJpaEntity} + {@code PartnershipRequestMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code PartnershipRepository#save}를 호출해야 한다.
 */
@Getter
public class PartnershipRequest {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final String businessName; // 사업체명
    private final String address; // 주소
    private final String addressDetail; // 상세 주소
    private final String contactName; // 담당자명
    private final String contactPhone; // 담당자 연락처
    private final LocalDateTime consultationRequestedAt; // 상담 요청 일시
    private PartnershipStatus status; // 처리 상태
    private boolean deleted; // 삭제 여부 (true: 삭제됨, Soft Delete)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private PartnershipRequest(
        Long id,
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt,
        PartnershipStatus status,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.businessName = businessName;
        this.address = address;
        this.addressDetail = addressDetail;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.consultationRequestedAt = consultationRequestedAt;
        this.status = status;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 제휴 문의를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static PartnershipRequest of(
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt
    ) {
        return new PartnershipRequest(
            null, businessName, address, addressDetail, contactName, contactPhone,
            consultationRequestedAt, PartnershipStatus.PENDING, false, null, null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static PartnershipRequest reconstitute(
        Long id,
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt,
        PartnershipStatus status,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new PartnershipRequest(
            id, businessName, address, addressDetail, contactName, contactPhone,
            consultationRequestedAt, status, deleted, createdAt, updatedAt
        );
    }

    public PartnershipRequestId getPartnershipRequestId() {
        return PartnershipRequestId.of(this.id);
    }

    public void changeStatus(PartnershipStatus status) {
        this.status = status;
    }

    public void delete() {
        this.deleted = true;
    }
}
