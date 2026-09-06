package com.tastyhouse.domain.member.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public class MemberWithdrawal {
    private final Long id;
    private final MemberId memberId;
    private final MemberWithdrawalReason reason;
    private final String reasonDetail;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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

    public static MemberWithdrawal of(MemberId memberId, MemberWithdrawalReason reason, String reasonDetail) {
        return new MemberWithdrawal(null, memberId, reason, reasonDetail, null, null);
    }

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
