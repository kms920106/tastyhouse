package com.tastyhouse.adminapi.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.notice.domain.model.Notice;
import com.tastyhouse.domain.notice.domain.repository.NoticeRepository;
import com.tastyhouse.domain.notice.domain.vo.NoticeId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 공지사항 관리 command 서비스.
 *
 * <p>domain write 포트({@link NoticeRepository})만 주입해 생성·수정·삭제를 수행한다. 조회는
 * {@link NoticeQueryService}가 담당하며, 이 서비스는 infra query DAO를 주입하지 않는다.
 *
 * <p>{@code Notice}는 순수 POJO라 더티 체킹이 없으므로 도메인 변경 후 명시적으로
 * {@code noticeRepository.save(notice)}를 호출한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeCommandService {

    private final NoticeRepository noticeRepository;

    public Long createNotice(String title, String content, boolean visible) {
        Notice notice = Notice.of(title, content, visible);
        Notice saved = noticeRepository.save(notice);
        return saved.getNoticeId().value();
    }

    public void updateNotice(Long id, String title, String content, boolean visible) {
        NoticeId noticeId = NoticeId.of(id);
        Notice notice = findNoticeOrThrow(noticeId);

        notice.update(title, content, visible);
        noticeRepository.save(notice);
    }

    public void deleteNotice(Long id) {
        NoticeId noticeId = NoticeId.of(id);
        Notice notice = findNoticeOrThrow(noticeId);

        notice.delete();
        noticeRepository.save(notice);
    }

    private Notice findNoticeOrThrow(NoticeId noticeId) {
        return noticeRepository.findById(noticeId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
