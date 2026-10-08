package com.tastyhouse.infrastructure.persistence.member.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface MemberDeliveryAddressJpaRepository extends JpaRepository<MemberDeliveryAddressJpaEntity, Long> {
}
