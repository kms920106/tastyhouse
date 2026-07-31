package com.tastyhouse.domain.bug.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class BugReportTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 초기 상태는 RECEIVED다")
    void of_createsTransientBugReport() {
        MemberId memberId = MemberId.of(1L);

        BugReport bugReport = BugReport.of(memberId, "iPhone 15", "제목", "내용", "1.0.0", BugReportPlatform.IOS, "17.0");

        assertThat(bugReport.getId()).isNull();
        assertThat(bugReport.getMemberId()).isEqualTo(memberId);
        assertThat(bugReport.getDevice()).isEqualTo("iPhone 15");
        assertThat(bugReport.getTitle()).isEqualTo("제목");
        assertThat(bugReport.getContent()).isEqualTo("내용");
        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.RECEIVED);
        assertThat(bugReport.getCategory()).isNull();
        assertThat(bugReport.getPriority()).isNull();
        assertThat(bugReport.getAssigneeAdminId()).isNull();
        assertThat(bugReport.getAdminAnswer()).isNull();
        assertThat(bugReport.getResolvedAt()).isNull();
        assertThat(bugReport.getCreatedAt()).isNull();
        assertThat(bugReport.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("classify는 분류·우선순위를 지정한다")
    void classify_setsCategoryAndPriority() {
        BugReport bugReport = createBugReport();

        bugReport.classify(BugReportCategory.PAYMENT, BugReportPriority.HIGH);

        assertThat(bugReport.getCategory()).isEqualTo(BugReportCategory.PAYMENT);
        assertThat(bugReport.getPriority()).isEqualTo(BugReportPriority.HIGH);
    }

    @Test
    @DisplayName("assignTo는 담당자를 배정한다")
    void assignTo_setsAssigneeAdminId() {
        BugReport bugReport = createBugReport();

        bugReport.assignTo(99L);

        assertThat(bugReport.getAssigneeAdminId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("startProgress는 RECEIVED에서 IN_PROGRESS로 전이한다")
    void startProgress_fromReceived_movesToInProgress() {
        BugReport bugReport = createBugReport();

        bugReport.startProgress();

        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("startProgress는 ON_HOLD에서도 IN_PROGRESS로 전이한다")
    void startProgress_fromOnHold_movesToInProgress() {
        BugReport bugReport = createBugReport();
        bugReport.hold();

        bugReport.startProgress();

        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("resolve는 처리 결과를 기록하고 RESOLVED로 전이하며 완료 시각을 세팅한다")
    void resolve_marksResolvedWithAnswerAndTimestamp() {
        BugReport bugReport = createBugReport();

        bugReport.resolve("수정 완료했습니다");

        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.RESOLVED);
        assertThat(bugReport.getAdminAnswer()).isEqualTo("수정 완료했습니다");
        assertThat(bugReport.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("reject는 반려 사유를 기록하고 REJECTED로 전이하며 종결 시각을 세팅한다")
    void reject_marksRejectedWithAnswerAndTimestamp() {
        BugReport bugReport = createBugReport();

        bugReport.reject("재현되지 않습니다");

        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.REJECTED);
        assertThat(bugReport.getAdminAnswer()).isEqualTo("재현되지 않습니다");
        assertThat(bugReport.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("hold는 RECEIVED|IN_PROGRESS에서 ON_HOLD로 전이한다")
    void hold_fromReceived_movesToOnHold() {
        BugReport bugReport = createBugReport();

        bugReport.hold();

        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.ON_HOLD);
    }

    @Test
    @DisplayName("RESOLVED 상태에서 startProgress를 호출하면 불변식 위반으로 예외가 발생한다")
    void startProgress_onResolved_throws() {
        BugReport bugReport = createBugReport();
        bugReport.resolve("완료");

        assertThatThrownBy(bugReport::startProgress)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("RESOLVED 상태에서 resolve를 다시 호출하면 불변식 위반으로 예외가 발생한다")
    void resolve_onResolved_throws() {
        BugReport bugReport = createBugReport();
        bugReport.resolve("완료");

        assertThatThrownBy(() -> bugReport.resolve("다시 완료"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("REJECTED 상태에서 reject를 다시 호출하면 불변식 위반으로 예외가 발생한다")
    void reject_onRejected_throws() {
        BugReport bugReport = createBugReport();
        bugReport.reject("반려");

        assertThatThrownBy(() -> bugReport.reject("다시 반려"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("RESOLVED 상태에서 hold를 호출하면 불변식 위반으로 예외가 발생한다")
    void hold_onResolved_throws() {
        BugReport bugReport = createBugReport();
        bugReport.resolve("완료");

        assertThatThrownBy(bugReport::hold)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·전 필드·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        MemberId memberId = MemberId.of(1L);
        LocalDateTime resolvedAt = LocalDateTime.of(2026, 1, 3, 0, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        BugReport bugReport = BugReport.reconstitute(
            1L, memberId, "iPhone 15", "제목", "내용",
            BugReportStatus.RESOLVED, BugReportCategory.PAYMENT, BugReportPriority.HIGH,
            99L, "처리 결과", resolvedAt,
            "1.0.0", BugReportPlatform.IOS, "17.0",
            createdAt, updatedAt
        );

        assertThat(bugReport.getId()).isEqualTo(1L);
        assertThat(bugReport.getBugReportId()).isEqualTo(BugReportId.of(1L));
        assertThat(bugReport.getMemberId()).isEqualTo(memberId);
        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.RESOLVED);
        assertThat(bugReport.getCategory()).isEqualTo(BugReportCategory.PAYMENT);
        assertThat(bugReport.getPriority()).isEqualTo(BugReportPriority.HIGH);
        assertThat(bugReport.getAssigneeAdminId()).isEqualTo(99L);
        assertThat(bugReport.getAdminAnswer()).isEqualTo("처리 결과");
        assertThat(bugReport.getResolvedAt()).isEqualTo(resolvedAt);
        assertThat(bugReport.getPlatform()).isEqualTo(BugReportPlatform.IOS);
        assertThat(bugReport.getCreatedAt()).isEqualTo(createdAt);
        assertThat(bugReport.getUpdatedAt()).isEqualTo(updatedAt);
    }

    private BugReport createBugReport() {
        return BugReport.of(
            MemberId.of(1L), "iPhone 15", "제목", "내용", "1.0.0", BugReportPlatform.IOS, "17.0"
        );
    }
}
