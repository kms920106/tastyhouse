package com.tastyhouse.domain.member.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

/**
 * 회원 탈퇴 이력 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code MemberWithdrawalJpaEntity} + {@code MemberWithdrawalMapper}가 담당한다.
 */
public class MemberWithdrawal {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId;
    private final MemberWithdrawalReason reason;
    private final String reasonDetail;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private MemberWithdrawal(
        Long id,
        MemberId memberId,
        MemberWithdrawalReason reason,
        String reasonDetail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.reason = reason;
        this.reasonDetail = reasonDetail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 탈퇴 이력을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static MemberWithdrawal of(MemberId memberId, MemberWithdrawalReason reason, String reasonDetail) {
        return new MemberWithdrawal(null, memberId, reason, reasonDetail, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static MemberWithdrawal reconstitute(
        Long id,
        MemberId memberId,
        MemberWithdrawalReason reason,
        String reasonDetail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new MemberWithdrawal(id, memberId, reason, reasonDetail, createdAt, updatedAt);
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public MemberWithdrawalReason getReason() {
        return this.reason;
    }

    public String getReasonDetail() {
        return this.reasonDetail;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
