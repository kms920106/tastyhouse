package com.tastyhouse.domain.bug.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.admin.domain.vo.AdminId;
import com.tastyhouse.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 버그 신고 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code BugReportJpaEntity} + {@code BugReportMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code BugReportRepository#save}를
 * 호출해야 한다.
 */
public class BugReport {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId; // 신고한 회원 ID
    private final String device; // 기기 정보 (제보자 원문)
    private final String title; // 신고 제목
    private final String content; // 신고 내용
    private BugReportStatus status; // 처리 상태
    private BugReportCategory category; // 분류 (미분류 시 null)
    private BugReportPriority priority; // 우선순위 (미지정 시 null)
    private AdminId assigneeAdminId; // 담당 관리자 ID (미배정 시 null)
    private String adminAnswer; // 처리 결과/반려 사유 (미처리 시 null)
    private LocalDateTime resolvedAt; // 처리 완료 일시 (RESOLVED/REJECTED 시 기록)
    private final String appVersion; // 앱 버전 (제보자 입력, 선택)
    private final BugReportPlatform platform; // 플랫폼 (선택)
    private final String osVersion; // OS 버전 (제보자 입력, 선택)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private BugReport(
        Long id,
        MemberId memberId,
        String device,
        String title,
        String content,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority,
        AdminId assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt,
        String appVersion,
        BugReportPlatform platform,
        String osVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.device = device;
        this.title = title;
        this.content = content;
        this.status = status;
        this.category = category;
        this.priority = priority;
        this.assigneeAdminId = assigneeAdminId;
        this.adminAnswer = adminAnswer;
        this.resolvedAt = resolvedAt;
        this.appVersion = appVersion;
        this.platform = platform;
        this.osVersion = osVersion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 버그 신고를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다. 초기 상태는 RECEIVED다.
     */
    public static BugReport of(
        MemberId memberId,
        String device,
        String title,
        String content,
        String appVersion,
        BugReportPlatform platform,
        String osVersion
    ) {
        return new BugReport(
            null, memberId, device, title, content,
            BugReportStatus.RECEIVED, null, null, null, null, null,
            appVersion, platform, osVersion, null, null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static BugReport reconstitute(
        Long id,
        MemberId memberId,
        String device,
        String title,
        String content,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority,
        AdminId assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt,
        String appVersion,
        BugReportPlatform platform,
        String osVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new BugReport(
            id, memberId, device, title, content,
            status, category, priority, assigneeAdminId, adminAnswer, resolvedAt,
            appVersion, platform, osVersion, createdAt, updatedAt
        );
    }

    public BugReportId getBugReportId() {
        return BugReportId.of(id);
    }

    /**
     * 트리아지: 분류/우선순위 지정. 어떤 상태에서도 재분류 가능.
     */
    public void classify(BugReportCategory category, BugReportPriority priority) {
        this.category = category;
        this.priority = priority;
    }

    /**
     * 담당자 배정. 어떤 상태에서도 재배정 가능.
     */
    public void assignTo(AdminId adminId) {
        this.assigneeAdminId = adminId;
    }

    /**
     * 처리 시작: RECEIVED|ON_HOLD -> IN_PROGRESS
     */
    public void startProgress() {
        if (this.status != BugReportStatus.RECEIVED && this.status != BugReportStatus.ON_HOLD) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.IN_PROGRESS;
    }

    /**
     * 처리 완료: RECEIVED|IN_PROGRESS|ON_HOLD -> RESOLVED (처리 결과 기록, 완료 시각 세팅)
     */
    public void resolve(String answer) {
        if (this.status == BugReportStatus.RESOLVED || this.status == BugReportStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.RESOLVED;
        this.adminAnswer = answer;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * 반려: RECEIVED|IN_PROGRESS|ON_HOLD -> REJECTED (반려 사유 기록, 종결 시각 세팅)
     */
    public void reject(String answer) {
        if (this.status == BugReportStatus.RESOLVED || this.status == BugReportStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.REJECTED;
        this.adminAnswer = answer;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * 보류: RECEIVED|IN_PROGRESS -> ON_HOLD
     */
    public void hold() {
        if (this.status != BugReportStatus.RECEIVED && this.status != BugReportStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.ON_HOLD;
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public String getDevice() {
        return this.device;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public BugReportStatus getStatus() {
        return this.status;
    }

    public BugReportCategory getCategory() {
        return this.category;
    }

    public BugReportPriority getPriority() {
        return this.priority;
    }

    public AdminId getAssigneeAdminId() {
        return this.assigneeAdminId;
    }

    public String getAdminAnswer() {
        return this.adminAnswer;
    }

    public LocalDateTime getResolvedAt() {
        return this.resolvedAt;
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    public BugReportPlatform getPlatform() {
        return this.platform;
    }

    public String getOsVersion() {
        return this.osVersion;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
