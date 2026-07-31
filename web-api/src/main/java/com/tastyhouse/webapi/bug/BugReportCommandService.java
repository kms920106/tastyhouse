package com.tastyhouse.webapi.bug;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.core.domain.bug.domain.service.BugReportRegistrationService;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.webapi.bug.response.BugReportResponse;
import com.tastyhouse.webapi.file.FileService;

/**
 * 회원 버그 제보 등록 서비스.
 *
 * <p>제보 등록은 제보 애그리거트와 첨부 이미지 애그리거트를 함께 저장하는 크로스 애그리거트
 * 오케스트레이션이라, 로직을 이 서비스가 직접 갖지 않고 도메인 서비스
 * {@link BugReportRegistrationService}에 위임한다(공통 지침 패턴 1·2). 이 서비스는 트랜잭션 경계와
 * 경계 타입 승격(Long→VO, String→core enum), 그리고 Response 조립만 담당한다.
 *
 * <p>web-api에는 제보 조회 엔드포인트가 없어 QueryService를 두지 않는다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BugReportCommandService {

    private final BugReportRegistrationService bugReportRegistrationService;
    private final FileService fileService;

    public BugReportResponse createBugReport(
        Long memberId,
        String device,
        String title,
        String content,
        String appVersion,
        String platform,
        String osVersion,
        List<Long> uploadedFileIds
    ) {
        MemberId reporterId = MemberId.of(memberId);
        BugReportPlatform bugReportPlatform = platform == null ? null : BugReportPlatform.from(platform);

        BugReport bugReport = bugReportRegistrationService.register(
            reporterId,
            device,
            title,
            content,
            appVersion,
            bugReportPlatform,
            osVersion,
            uploadedFileIds
        );

        return toBugReportResponse(bugReport, uploadedFileIds);
    }

    private BugReportResponse toBugReportResponse(BugReport bugReport, List<Long> uploadedFileIds) {
        return BugReportResponse.from(
            bugReport.getBugReportId().value(),
            bugReport.getDevice(),
            bugReport.getTitle(),
            bugReport.getContent(),
            bugReport.getAppVersion(),
            bugReport.getPlatform() != null ? bugReport.getPlatform().name() : null,
            bugReport.getOsVersion(),
            bugReport.getStatus() != null ? bugReport.getStatus().name() : null,
            toImageUrls(uploadedFileIds),
            bugReport.getCreatedAt()
        );
    }

    private List<String> toImageUrls(List<Long> imageFileIds) {
        if (imageFileIds == null || imageFileIds.isEmpty()) {
            return List.of();
        }
        return imageFileIds.stream()
            .map(fileService::getUrlByFileId)
            .filter(Objects::nonNull)
            .toList();
    }
}
