package com.tastyhouse.domain.member.follow.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.domain.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class MemberFollowTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)이고 팔로워·팔로잉 관계를 담는다")
    void of_createsTransientMemberFollow() {
        MemberId followerId = MemberId.of(1L);
        MemberId followingId = MemberId.of(2L);

        MemberFollow memberFollow = MemberFollow.of(followerId, followingId);

        assertThat(memberFollow.getId()).isNull();
        assertThat(memberFollow.getFollowerId()).isEqualTo(followerId);
        assertThat(memberFollow.getFollowingId()).isEqualTo(followingId);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        MemberId followerId = MemberId.of(1L);
        MemberId followingId = MemberId.of(2L);

        MemberFollow memberFollow = MemberFollow.reconstitute(10L, followerId, followingId);

        assertThat(memberFollow.getId()).isEqualTo(10L);
        assertThat(memberFollow.getFollowerId()).isEqualTo(followerId);
        assertThat(memberFollow.getFollowingId()).isEqualTo(followingId);
    }
}
