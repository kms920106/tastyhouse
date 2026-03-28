package com.tastyhouse.core.repository.verification;

import com.tastyhouse.core.entity.verification.PhoneVerification;
import com.tastyhouse.core.entity.verification.PhoneVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PhoneVerificationJpaRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findTopByPhoneNumberValueAndStatusOrderByCreatedAtDesc(
        String phoneNumber, PhoneVerificationStatus status
    );

    @Modifying
    @Query("UPDATE PhoneVerification pv SET pv.status = 'EXPIRED' " +
           "WHERE pv.phoneNumber.value = :phoneNumber AND pv.status = 'PENDING'")
    void expireAllPendingByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Modifying
    @Query("UPDATE PhoneVerification pv SET pv.status = 'EXPIRED' " +
           "WHERE pv.status = 'PENDING' AND pv.expiresAt < :now")
    void expireAllOverdue(@Param("now") LocalDateTime now);
}
