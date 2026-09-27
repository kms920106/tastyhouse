package com.tastyhouse.application.bug.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class BugReportStateMapperTest {

    @Test
    @DisplayName("BugReport → BugReportState → BugReport 왕복 시 모든 필드가 보존된다")
    void bugReportRoundTrip() {
        BugReport original = BugReport.reconstitute(
            3L, MemberId.of(41L), "iPhone 15", "제목", "본문",
            BugReportStatus.IN_PROGRESS, BugReportCategory.ORDER,
            BugReportPriority.HIGH,
            AdminId.of(52L), "관리자 답변",
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            "1.2.3", BugReportPlatform.ANDROID, "17.4",
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        BugReport restored = BugReportStateMapper.toDomain(BugReportStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("nullable 필드가 null이어도 왕복된다")
    void bugReportRoundTripWithNulls() {
        BugReport original = BugReport.reconstitute(
            4L, MemberId.of(42L), "Galaxy", "t", "c",
            BugReportStatus.RECEIVED, null, null, null, null, null, null, null, null,
            LocalDateTime.of(2026, 4, 5, 6, 7, 8),
            LocalDateTime.of(2026, 5, 6, 7, 8, 9));

        BugReport restored = BugReportStateMapper.toDomain(BugReportStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("BugReportImage → BugReportImageState → BugReportImage 왕복 시 모든 필드가 보존된다")
    void bugReportImageRoundTrip() {
        BugReportImage original = BugReportImage.reconstitute(7L, BugReportId.of(8L), UploadedFileId.of(9L), 2);

        BugReportImage restored = BugReportImageStateMapper.toDomain(BugReportImageStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
