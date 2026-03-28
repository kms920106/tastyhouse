package com.tastyhouse.core.repository.member;

import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findByNickname(String nickname);

    Page<Member> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);

    boolean existsByPhoneNumberValueAndMemberStatusNot(String phoneNumber, MemberStatus memberStatus);
}