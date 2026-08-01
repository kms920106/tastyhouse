package com.tastyhouse.webapi.bug;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.bug.domain.model.BugReport;
import com.tastyhouse.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.domain.bug.domain.service.BugReportRegistrationService;
import com.tastyhouse.domain.member.domain.vo.MemberId;

/**
 * 회원 버그 제보 등록 서비스.
 *
 * <p>제보 등록은 제보 애그리거트와 첨부 이미지 애그리거트를 함께 저장하는 크로스 애그리거트
 * 오케스트레이션이라, 로직을 이 서비스가 직접 갖지 않고 도메인 서비스
 * {@link BugReportRegistrationService}에 위임한다(공통 지침 패턴 1·2). 이 서비스는 트랜잭션 경계와
 * 경계 타입 승격(Long→VO, String→core enum)만 담당한다.
 *
 * <p>CQRS 규칙대로 <b>식별자만</b> 반환하며, 컨트롤러도 그 식별자를 그대로 응답한다 — 등록 응답은
 * 생성된 id 하나이므로 재조회로 상세를 조립하지 않는다(등록 API 응답 본문 규칙). 상세가 필요한
 * 클라이언트는 반환받은 id로 별도 조회를 호출한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BugReportCommandService {

    private final BugReportRegistrationService bugReportRegistrationService;

    /**
     * @return 등록된 제보 식별자
     */
    public Long createBugReport(
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

        return bugReport.getBugReportId().value();
    }
}
