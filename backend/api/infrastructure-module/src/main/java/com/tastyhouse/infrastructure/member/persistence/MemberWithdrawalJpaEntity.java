package com.tastyhouse.infrastructure.member.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 탈퇴 이력 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code MemberWithdrawal}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MemberWithdrawalMapper}가 수행한다.
 * update 경로가 없어(insert 전용) {@code applyChanges}는 두지 않는다.
 */
@Entity
@Table(name = "MEMBER_WITHDRAWAL")
public class MemberWithdrawalJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private MemberWithdrawalReason reason;

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail;

    protected MemberWithdrawalJpaEntity() {
    }

    private MemberWithdrawalJpaEntity(MemberId memberId, MemberWithdrawalReason reason, String reasonDetail) {
        this.memberId = memberId;
        this.reason = reason;
        this.reasonDetail = reasonDetail;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MemberWithdrawalMapper#toEntity}에서만 호출한다.
     */
    static MemberWithdrawalJpaEntity create(MemberId memberId, MemberWithdrawalReason reason, String reasonDetail) {
        return new MemberWithdrawalJpaEntity(memberId, reason, reasonDetail);
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
}
