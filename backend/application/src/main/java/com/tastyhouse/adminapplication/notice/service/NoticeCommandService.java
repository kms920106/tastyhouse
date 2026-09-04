package com.tastyhouse.adminapplication.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapplication.notice.port.in.NoticeCommandUseCase;
import com.tastyhouse.adminapplication.notice.port.in.NoticeCreateCommand;
import com.tastyhouse.adminapplication.notice.port.in.NoticeDeleteCommand;
import com.tastyhouse.adminapplication.notice.port.in.NoticeUpdateCommand;
import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.repository.NoticeRepository;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

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
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticeRepository noticeRepository;

    public NoticeCommandService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Override
    public Long createNotice(NoticeCreateCommand command) {
        Notice notice = Notice.of(command.title(), command.content(), command.visible());
        Notice saved = noticeRepository.save(notice);
        return saved.getNoticeId().value();
    }

    @Override
    public void updateNotice(NoticeUpdateCommand command) {
        NoticeId noticeId = NoticeId.of(command.noticeId());
        Notice notice = findNoticeOrThrow(noticeId);

        notice.update(command.title(), command.content(), command.visible());
        noticeRepository.save(notice);
    }

    @Override
    public void deleteNotice(NoticeDeleteCommand command) {
        NoticeId noticeId = NoticeId.of(command.noticeId());
        Notice notice = findNoticeOrThrow(noticeId);

        notice.delete();
        noticeRepository.save(notice);
    }

    private Notice findNoticeOrThrow(NoticeId noticeId) {
        return noticeRepository.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
