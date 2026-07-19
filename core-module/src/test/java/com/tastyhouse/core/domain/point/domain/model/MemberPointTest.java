package com.tastyhouse.core.domain.point.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class MemberPointTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)이고 사용 가능 포인트·이번달 소멸 포인트가 0이다")
    void of_createsTransientMemberPoint() {
        MemberPoint memberPoint = MemberPoint.of(MemberId.of(1L));

        assertThat(memberPoint.getId()).isNull();
        assertThat(memberPoint.getMemberId()).isEqualTo(MemberId.of(1L));
        assertThat(memberPoint.getAvailablePoints()).isEqualTo(0);
        assertThat(memberPoint.getExpiredThisMonth()).isEqualTo(0);
    }

    @Test
    @DisplayName("addPoints는 사용 가능 포인트를 증가시킨다")
    void addPoints_increasesAvailablePoints() {
        MemberPoint memberPoint = MemberPoint.of(MemberId.of(1L));

        memberPoint.addPoints(1000);

        assertThat(memberPoint.getAvailablePoints()).isEqualTo(1000);
    }

    @Test
    @DisplayName("deductPoints는 사용 가능 포인트를 차감한다")
    void deductPoints_decreasesAvailablePoints() {
        MemberPoint memberPoint = MemberPoint.of(MemberId.of(1L));
        memberPoint.addPoints(1000);

        memberPoint.deductPoints(300);

        assertThat(memberPoint.getAvailablePoints()).isEqualTo(700);
    }

    @Test
    @DisplayName("deductPoints는 잔액이 부족하면 예외가 발생한다")
    void deductPoints_onInsufficientBalance_throws() {
        MemberPoint memberPoint = MemberPoint.of(MemberId.of(1L));
        memberPoint.addPoints(100);

        assertThatThrownBy(() -> memberPoint.deductPoints(200))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·포인트를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        MemberPoint memberPoint = MemberPoint.reconstitute(1L, MemberId.of(2L), 500, 50);

        assertThat(memberPoint.getId()).isEqualTo(1L);
        assertThat(memberPoint.getMemberId()).isEqualTo(MemberId.of(2L));
        assertThat(memberPoint.getAvailablePoints()).isEqualTo(500);
        assertThat(memberPoint.getExpiredThisMonth()).isEqualTo(50);
    }
}
