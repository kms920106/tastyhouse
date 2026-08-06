package com.tastyhouse.infrastructure.holiday.persistence;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 법정 공휴일 JPA 영속 모델. 순수 도메인 모델 {@code PublicHoliday}와 분리된 영속 전용 엔티티다.
 *
 * <p>시드 SQL로만 관리하는 read-only 마스터라 생성·변경 팩토리를 두지 않는다. 감사 컬럼이 없어
 * {@code BaseEntity}를 상속하지 않는다({@code MailVerification} 선례).
 */
@Entity
@Table(name = "PUBLIC_HOLIDAY")
public class PublicHolidayJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate; // 공휴일 날짜

    @Column(name = "name", nullable = false, length = 50)
    private String name; // 공휴일 명칭

    @Column(name = "is_substitute", nullable = false)
    private boolean substitute; // 대체공휴일 여부

    protected PublicHolidayJpaEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public LocalDate getHolidayDate() {
        return this.holidayDate;
    }

    public String getName() {
        return this.name;
    }

    public boolean isSubstitute() {
        return this.substitute;
    }
}
