package com.tastyhouse.application.member.follow.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class MemberFollowStateMapperTest {

    @Test
    @DisplayName("MemberFollow → MemberFollowState → MemberFollow 왕복 시 팔로워·팔로잉이 뒤바뀌지 않는다")
    void roundTrip() {
        MemberFollow original = MemberFollow.reconstitute(121L, MemberId.of(122L), MemberId.of(123L));

        MemberFollow restored = MemberFollowStateMapper.toDomain(MemberFollowStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
