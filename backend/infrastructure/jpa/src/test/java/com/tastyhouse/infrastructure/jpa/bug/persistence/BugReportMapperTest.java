package com.tastyhouse.infrastructure.jpa.bug.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class BugReportMapperTest {

    @Test
    @DisplayName("BugReport → 엔티티 변환 시 enum은 name, VO는 value로 컬럼에 채워진다")
    void toEntity() {
        BugReport bugReport = BugReport.reconstitute(
            3L, MemberId.of(41L), "iPhone 15", "제목", "본문",
            BugReportStatus.IN_PROGRESS, BugReportCategory.ORDER,
            BugReportPriority.HIGH,
            AdminId.of(52L), "관리자 답변",
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            "1.2.3", BugReportPlatform.ANDROID, "17.4",
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        BugReportJpaEntity entity = BugReportMapper.toEntity(bugReport);

        assertThat(entity.getMemberId()).isEqualTo(41L);
        assertThat(entity.getDevice()).isEqualTo("iPhone 15");
        assertThat(entity.getTitle()).isEqualTo("제목");
        assertThat(entity.getContent()).isEqualTo("본문");
        assertThat(entity.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(entity.getCategory()).isEqualTo("ORDER");
        assertThat(entity.getPriority()).isEqualTo("HIGH");
        assertThat(entity.getAssigneeAdminId()).isEqualTo(52L);
        assertThat(entity.getAdminAnswer()).isEqualTo("관리자 답변");
        assertThat(entity.getResolvedAt()).isEqualTo(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        assertThat(entity.getAppVersion()).isEqualTo("1.2.3");
        assertThat(entity.getPlatform()).isEqualTo("ANDROID");
        assertThat(entity.getOsVersion()).isEqualTo("17.4");
    }

    @Test
    @DisplayName("nullable 필드가 null인 BugReport도 엔티티 컬럼이 null로 채워진다")
    void toEntityWithNulls() {
        BugReport bugReport = BugReport.reconstitute(
            4L, MemberId.of(42L), "Galaxy", "t", "c",
            BugReportStatus.RECEIVED, null, null, null, null, null, null, null, null,
            LocalDateTime.of(2026, 4, 5, 6, 7, 8),
            LocalDateTime.of(2026, 5, 6, 7, 8, 9));

        BugReportJpaEntity entity = BugReportMapper.toEntity(bugReport);

        assertThat(entity.getMemberId()).isEqualTo(42L);
        assertThat(entity.getDevice()).isEqualTo("Galaxy");
        assertThat(entity.getTitle()).isEqualTo("t");
        assertThat(entity.getContent()).isEqualTo("c");
        assertThat(entity.getStatus()).isEqualTo("RECEIVED");
        assertThat(entity.getCategory()).isNull();
        assertThat(entity.getPriority()).isNull();
        assertThat(entity.getAssigneeAdminId()).isNull();
        assertThat(entity.getAdminAnswer()).isNull();
        assertThat(entity.getResolvedAt()).isNull();
        assertThat(entity.getAppVersion()).isNull();
        assertThat(entity.getPlatform()).isNull();
        assertThat(entity.getOsVersion()).isNull();
    }

    @Test
    @DisplayName("엔티티 → BugReport 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        BugReportJpaEntity entity = BugReportJpaEntity.create(
            41L, "iPhone 15", "제목", "본문", "IN_PROGRESS", "ORDER", "HIGH",
            52L, "관리자 답변", LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            "1.2.3", "ANDROID", "17.4");
        ReflectionTestUtils.setField(entity, "id", 3L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 3, 4, 5, 6));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        BugReport bugReport = BugReportMapper.toDomain(entity);

        BugReport expected = BugReport.reconstitute(
            3L, MemberId.of(41L), "iPhone 15", "제목", "본문",
            BugReportStatus.IN_PROGRESS, BugReportCategory.ORDER,
            BugReportPriority.HIGH,
            AdminId.of(52L), "관리자 답변",
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            "1.2.3", BugReportPlatform.ANDROID, "17.4",
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));
        assertThat(bugReport).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("nullable 컬럼이 null인 엔티티도 BugReport로 복원된다")
    void toDomainWithNulls() {
        BugReportJpaEntity entity = BugReportJpaEntity.create(
            42L, "Galaxy", "t", "c", "RECEIVED", null, null, null, null, null, null, null, null);
        ReflectionTestUtils.setField(entity, "id", 4L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 4, 5, 6, 7, 8));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 5, 6, 7, 8, 9));

        BugReport bugReport = BugReportMapper.toDomain(entity);

        BugReport expected = BugReport.reconstitute(
            4L, MemberId.of(42L), "Galaxy", "t", "c",
            BugReportStatus.RECEIVED, null, null, null, null, null, null, null, null,
            LocalDateTime.of(2026, 4, 5, 6, 7, 8),
            LocalDateTime.of(2026, 5, 6, 7, 8, 9));
        assertThat(bugReport).usingRecursiveComparison().isEqualTo(expected);
    }
}
