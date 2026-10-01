package com.tastyhouse.infrastructure.member.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberDeliveryAddressJpaRepository extends JpaRepository<MemberDeliveryAddressJpaEntity, Long> {

    List<MemberDeliveryAddressJpaEntity> findByMemberIdOrderByIdAsc(Long memberId);

    long countByMemberId(Long memberId);

    Optional<MemberDeliveryAddressJpaEntity> findByMemberIdAndDefaultAddressTrue(Long memberId);
}
