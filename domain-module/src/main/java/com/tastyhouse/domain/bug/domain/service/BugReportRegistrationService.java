package com.tastyhouse.domain.bug.domain.service;

import java.util.List;

import com.tastyhouse.domain.bug.domain.model.BugReport;
import com.tastyhouse.domain.bug.domain.model.BugReportImage;
import com.tastyhouse.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.domain.bug.domain.repository.BugReportImageRepository;
import com.tastyhouse.domain.bug.domain.repository.BugReportRepository;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.member.domain.vo.MemberId;

/**
 * 버그 제보 등록(도메인 서비스).
 *
 * <p>제보 등록은 한 트랜잭션에서 {@code BugReport} 애그리거트를 저장한 뒤, 그때 발급된 식별자로
 * {@code BugReportImage} 애그리거트를 첨부 순서대로 여러 건 저장한다. 애그리거트 타입 2개를 함께
 * load & save 하는 불변식 오케스트레이션(분류 C)이므로 소비 모듈의 command 서비스가 아니라 도메인
 * 계층에 두어, 여러 모듈에서 등록 경로가 생겨도 "이미지는 제보에 종속되며 첨부 순서를 보존한다"는
 * 규칙이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 호출자(command 서비스)의
 * 트랜잭션 안에서 실행된다.
 */
public class BugReportRegistrationService {

    private final BugReportRepository bugReportRepository;
    private final BugReportImageRepository bugReportImageRepository;

    public BugReportRegistrationService(
        BugReportRepository bugReportRepository,
        BugReportImageRepository bugReportImageRepository
    ) {
        this.bugReportRepository = bugReportRepository;
        this.bugReportImageRepository = bugReportImageRepository;
    }

    /**
     * 제보와 첨부 이미지를 함께 등록하고, 식별자가 채워진 제보 도메인 객체를 반환한다.
     *
     * <p>이미지는 목록 순서를 그대로 정렬 순서(sort)로 보존한다. 첨부가 없으면(null/빈 목록) 제보만
     * 저장한다. 도메인이 프레임워크-프리라 더티 체킹이 없으므로 저장은 모두 명시적 save로 수행한다.
     */
    public BugReport register(
        MemberId memberId,
        String device,
        String title,
        String content,
        String appVersion,
        BugReportPlatform platform,
        String osVersion,
        List<Long> uploadedFileIds
    ) {
        BugReport bugReport = BugReport.of(memberId, device, title, content, appVersion, platform, osVersion);
        BugReport saved = bugReportRepository.save(bugReport);

        if (uploadedFileIds == null || uploadedFileIds.isEmpty()) {
            return saved;
        }

        for (int sort = 0; sort < uploadedFileIds.size(); sort++) {
            BugReportImage image = BugReportImage.of(
                saved.getBugReportId(), UploadedFileId.of(uploadedFileIds.get(sort)), sort
            );
            bugReportImageRepository.save(image);
        }

        return saved;
    }
}
