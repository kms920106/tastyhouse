package com.tastyhouse.core.domain.member.infrastructure.persistence;

import com.tastyhouse.core.domain.member.domain.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberJpaRepository extends JpaRepository<Member, Long> {
}
