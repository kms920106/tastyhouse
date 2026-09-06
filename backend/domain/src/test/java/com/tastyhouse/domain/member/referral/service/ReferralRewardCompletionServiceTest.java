package com.tastyhouse.domain.member.referral.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.model.MemberReferralStatus;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 추천 보상 완료 전이 도메인 서비스 단위 테스트.
 *
 * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로, 전이 후 <b>명시적으로 저장되는지</b>가 핵심
 * 검증 대상이다 — 저장이 빠지면 전이가 조용히 유실되어 보상은 적립됐는데 관계는 계속 PENDING으로 남는다.
 */
class ReferralRewardCompletionServiceTest {

    private final FakeMemberReferralRepository referralRepository = new FakeMemberReferralRepository();
    private final ReferralRewardCompletionService service =
        new ReferralRewardCompletionService(referralRepository);

    @Test
    @DisplayName("보상 완료 전이는 REWARDED로 바꾼 결과를 저장까지 반영한다")
    void completesAndPersists() {
        MemberReferral saved = referralRepository.save(
            MemberReferral.register(MemberId.of(101L), MemberId.of(202L))
        );

        service.complete(saved.getReferralId());

        assertThat(referralRepository.findById(saved.getReferralId()).orElseThrow().getStatus())
            .as("전이 후 save를 호출하지 않으면 변경이 유실된다")
            .isEqualTo(MemberReferralStatus.REWARDED);
    }

    @Test
    @DisplayName("이미 보상 완료된 추천 관계를 다시 전이시키면 거절한다")
    void rejectsDuplicateCompletion() {
        MemberReferral saved = referralRepository.save(
            MemberReferral.register(MemberId.of(101L), MemberId.of(202L))
        );
        service.complete(saved.getReferralId());

        assertThatThrownBy(() -> service.complete(saved.getReferralId()))
            .as("중복 전이가 조용히 통과하면 재처리 시 이중 적립을 감지할 수 없다")
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("존재하지 않는 추천 관계면 REFERRAL_NOT_FOUND로 실패한다")
    void rejectsUnknownReferral() {
        assertThatThrownBy(() -> service.complete(new ReferralId(999L)))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
