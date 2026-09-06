package com.tastyhouse.domain.member.referral.service;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.repository.MemberReferralRepository;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 추천 보상 완료 전이(도메인 서비스).
 *
 * <p>보상 적립이 끝난 추천 관계를 {@code REWARDED}로 전이시킨다. 적립 자체는 point 컨텍스트의
 * {@code PointLedgerService}가 수행하므로, 그 둘을 잇는 것은 infrastructure의
 * {@code ReferralRegisteredEventListener}다.
 *
 * <p><b>왜 별도 서비스인가</b>: 전이 대상 로드와 상태 전이는 referral 컨텍스트의 불변식이므로,
 * 리스너가 {@code MemberReferralRepository}를 직접 주입해 {@code findById → reward → save}를
 * 조립하게 두지 않는다. 그렇게 두면 "보상 완료로 넘기는 절차"가 infrastructure에 흩어져, 나중에
 * 다른 경로(관리자 수동 보정 등)가 생길 때 규칙이 갈린다.
 *
 * <p><b>보상 적립 이후에 호출해야 한다.</b> 이 전이가 적립보다 먼저 커밋되면 "완료로 표시됐지만
 * 포인트는 없는" 상태가 남아, 미적립 건을 상태로 식별할 수 없게 된다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code MemberDomainConfig}가 담당한다.
 */
public class ReferralRewardCompletionService {

    private final MemberReferralRepository memberReferralRepository;

    public ReferralRewardCompletionService(MemberReferralRepository memberReferralRepository) {
        this.memberReferralRepository = memberReferralRepository;
    }

    /**
     * 추천 관계를 보상 완료 상태로 전이시킨다.
     *
     * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로 전이 후 명시적으로 {@code save}를 호출한다.
     * 이미 {@code PENDING}이 아니면 {@code MemberReferral#reward}가
     * {@code REFERRAL_INVALID_STATUS}로 거절하므로 중복 전이가 조용히 통과하지 않는다.
     */
    public void complete(ReferralId referralId) {
        MemberReferral referral = memberReferralRepository.findById(referralId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REFERRAL_NOT_FOUND,
                "추천 관계를 찾을 수 없습니다. referralId=" + referralId.value()));

        referral.reward();
        memberReferralRepository.save(referral);
    }
}
