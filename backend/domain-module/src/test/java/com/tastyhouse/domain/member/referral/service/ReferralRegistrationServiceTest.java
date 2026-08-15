package com.tastyhouse.domain.member.referral.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.event.ReferralRegisteredEvent;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.model.MemberReferralStatus;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 추천인 등록 도메인 서비스 단위 테스트.
 *
 * <p>이 서비스가 <b>더 이상 포인트를 적립하지 않는다</b>는 것이 핵심 검증 대상이다. 적립은 커밋 이후
 * {@code ReferralRegisteredEventListener}가 point 컨텍스트를 경유해 수행하므로, 여기서는 추천 관계가
 * {@code PENDING}으로 남고 등록 이벤트에 <b>식별자가 채워진 채</b> 실리는지를 본다 — 식별자가 없으면
 * 리스너가 보상 완료 전이 대상을 찾지 못한다.
 */
class ReferralRegistrationServiceTest {

    private static final MemberId REFERRER_ID = MemberId.of(101L);
    private static final MemberId REFEREE_ID = MemberId.of(202L);

    private final FakeMemberReferralRepository referralRepository = new FakeMemberReferralRepository();
    private final DomainEventPublisherStub eventPublisher = new DomainEventPublisherStub();
    private final ReferralRegistrationService service =
        new ReferralRegistrationService(referralRepository, eventPublisher);

    @Test
    @DisplayName("등록하면 추천 관계는 PENDING으로 저장되고 보상 완료 전이는 일어나지 않는다")
    void registersReferralAsPending() {
        service.register(REFERRER_ID, REFEREE_ID);

        ReferralId referralId = publishedEvent().referralId();
        MemberReferral saved = referralRepository.findById(referralId).orElseThrow();

        assertThat(saved.getStatus())
            .as("보상 적립 전이므로 REWARDED가 아니어야 한다 — 적립 실패 건을 상태로 식별하기 위함")
            .isEqualTo(MemberReferralStatus.PENDING);
        assertThat(saved.getReferrerId()).isEqualTo(REFERRER_ID);
        assertThat(saved.getRefereeId()).isEqualTo(REFEREE_ID);
    }

    @Test
    @DisplayName("등록 이벤트에는 저장으로 채워진 식별자와 추천인·피추천인이 제 자리에 실린다")
    void publishesEventWithPersistedIdentifier() {
        service.register(REFERRER_ID, REFEREE_ID);

        ReferralRegisteredEvent event = publishedEvent();

        assertThat(event.referralId().value())
            .as("save 반환값을 재할당하지 않으면 식별자가 비어 리스너가 전이 대상을 찾지 못한다")
            .isNotNull();
        // 추천인·피추천인이 둘 다 MemberId라 순서를 바꿔도 컴파일된다 — 각각 제 자리인지 확인한다.
        assertThat(event.referrerId()).isEqualTo(REFERRER_ID);
        assertThat(event.refereeId()).isEqualTo(REFEREE_ID);
        assertThat(event.registeredAt()).isNotNull();
    }

    @Test
    @DisplayName("자기 자신을 추천인으로 지정하면 거절하고 아무것도 저장·발행하지 않는다")
    void rejectsSelfReferral() {
        assertThatThrownBy(() -> service.register(REFERRER_ID, REFERRER_ID))
            .isInstanceOf(BusinessException.class);

        assertThat(eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("이미 추천인이 등록된 회원이면 거절한다")
    void rejectsDuplicateReferee() {
        service.register(REFERRER_ID, REFEREE_ID);
        eventPublisher.published.clear();

        assertThatThrownBy(() -> service.register(MemberId.of(303L), REFEREE_ID))
            .isInstanceOf(BusinessException.class);

        assertThat(eventPublisher.published).isEmpty();
    }

    private ReferralRegisteredEvent publishedEvent() {
        assertThat(eventPublisher.published).hasSize(1);
        assertThat(eventPublisher.published.getFirst()).isInstanceOf(ReferralRegisteredEvent.class);
        return (ReferralRegisteredEvent) eventPublisher.published.getFirst();
    }

    private static final class DomainEventPublisherStub implements DomainEventPublisher {

        private final List<Object> published = new ArrayList<>();

        @Override
        public void publish(Object event) {
            published.add(event);
        }
    }
}
