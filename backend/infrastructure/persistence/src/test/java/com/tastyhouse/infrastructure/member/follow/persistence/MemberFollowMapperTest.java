package com.tastyhouse.infrastructure.member.follow.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class MemberFollowMapperTest {

    @Test
    @DisplayName("MemberFollow → 엔티티 변환 시 팔로워·팔로잉이 뒤바뀌지 않는다")
    void toEntity() {
        MemberFollow original = MemberFollow.reconstitute(121L, MemberId.of(122L), MemberId.of(123L));

        MemberFollowJpaEntity entity = MemberFollowMapper.toEntity(original);

        assertThat(entity.getFollowerId()).isEqualTo(122L);
        assertThat(entity.getFollowingId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("엔티티 → MemberFollow 변환 시 팔로워·팔로잉이 뒤바뀌지 않는다")
    void toDomain() {
        MemberFollowJpaEntity entity = MemberFollowJpaEntity.create(122L, 123L);
        ReflectionTestUtils.setField(entity, "id", 121L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 2, 0, 0));

        MemberFollow restored = MemberFollowMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison()
            .isEqualTo(MemberFollow.reconstitute(121L, MemberId.of(122L), MemberId.of(123L)));
    }
}
