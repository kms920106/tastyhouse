package com.tastyhouse.infrastructure.mybatis.notice.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.application.notice.port.out.write.NoticeLoadPort;
import com.tastyhouse.application.notice.port.out.write.NoticeSavePort;

@Repository
class NoticeMyBatisPersistenceAdapter implements NoticeLoadPort, NoticeSavePort {

    private final NoticeMyBatisMapper noticeMyBatisMapper;

    public NoticeMyBatisPersistenceAdapter(NoticeMyBatisMapper noticeMyBatisMapper) {
        this.noticeMyBatisMapper = noticeMyBatisMapper;
    }

    @Override
    public Optional<Notice> findActiveById(NoticeId noticeId) {
        return noticeMyBatisMapper.selectActiveById(noticeId.value()).map(NoticeRowMapper::toDomain);
    }

    @Override
    public Notice save(Notice notice) {
        LocalDateTime now = LocalDateTime.now();

        if (notice.getId() == null) {
            NoticeWriteRow row = NoticeRowMapper.toWriteRow(notice, now, now);
            noticeMyBatisMapper.insert(row);
            return NoticeRowMapper.toDomain(row);
        }

        NoticeWriteRow row = NoticeRowMapper.toWriteRow(notice, notice.getCreatedAt(), now);
        if (noticeMyBatisMapper.update(row) == 0) {
            throw new IllegalStateException("존재하지 않는 공지사항입니다: " + notice.getId());
        }
        return NoticeRowMapper.toDomain(row);
    }
}
