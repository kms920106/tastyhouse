package com.tastyhouse.core.repository.verification;

import com.tastyhouse.core.entity.verification.EmailVerification;
import com.tastyhouse.core.entity.verification.EmailVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailAndStatusOrderByCreatedAtDesc(
        String email, EmailVerificationStatus status
    );

    @Modifying
    @Query("UPDATE EmailVerification ev SET ev.status = 'EXPIRED' " +
           "WHERE ev.email = :email AND ev.status = 'PENDING'")
    void expireAllPendingByEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE EmailVerification ev SET ev.status = 'EXPIRED' " +
           "WHERE ev.status = 'PENDING' AND ev.expiresAt < :now")
    void expireAllOverdue(@Param("now") LocalDateTime now);
}
