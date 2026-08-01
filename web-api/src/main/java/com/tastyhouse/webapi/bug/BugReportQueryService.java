package com.tastyhouse.webapi.bug;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.infrastructure.bug.query.BugReportDetailResult;
import com.tastyhouse.infrastructure.bug.query.BugReportQueryDao;
import com.tastyhouse.webapi.bug.response.BugReportResponse;
import com.tastyhouse.webapi.file.FileService;

/**
 * 회원 버그 제보 조회 서비스(CQRS query 측).
 *
 * <p>회원 화면에는 제보 목록·상세 엔드포인트가 없고, 이 서비스는 <b>등록 직후 응답 조립</b>만을 위해
 * 존재한다 — {@link BugReportCommandService}가 CQRS 규칙대로 식별자만 반환하므로, 컨트롤러가 커밋 이후
 * 이 서비스로 재조회해 기존과 동일한 {@link BugReportResponse}를 만든다.
 *
 * <p>첨부 이미지는 DAO가 파일 식별자를 투영해 주므로 표시용 URL 변환은 이 계층에서
 * {@link FileService}로 수행한다(응답에 파일 식별자를 노출하지 않는다는 규칙).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BugReportQueryService {

    private final BugReportQueryDao bugReportQueryDao;
    private final FileService fileService;

    /**
     * 제보 등록 응답 — 명령이 돌려준 식별자로 커밋 이후 재조회해 조립한다.
     */
    public BugReportResponse getBugReportResponse(Long id) {
        BugReportDetailResult detail = bugReportQueryDao.findDetailById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "버그 제보를 찾을 수 없습니다."));

        return toBugReportResponse(detail);
    }

    private BugReportResponse toBugReportResponse(BugReportDetailResult dto) {
        return BugReportResponse.from(
            dto.id(),
            dto.device(),
            dto.title(),
            dto.content(),
            dto.appVersion(),
            dto.platform() != null ? dto.platform().name() : null,
            dto.osVersion(),
            dto.status() != null ? dto.status().name() : null,
            toImageUrls(dto.imageFileIds()),
            dto.createdAt()
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
