package com.tastyhouse.infrastructure.mybatis.notice.query;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.application.notice.port.out.NoticeDetailResult;
import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementQueryPort;
import com.tastyhouse.application.notice.port.out.NoticeQueryPort;
import com.tastyhouse.application.notice.port.out.NoticeSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Repository
class NoticeMyBatisQueryAdapter implements NoticeQueryPort, NoticeManagementQueryPort {

    private static final char LIKE_ESCAPE = '!';

    private final NoticeQueryMyBatisMapper noticeQueryMyBatisMapper;

    public NoticeMyBatisQueryAdapter(NoticeQueryMyBatisMapper noticeQueryMyBatisMapper) {
        this.noticeQueryMyBatisMapper = noticeQueryMyBatisMapper;
    }

    @Override
    public PageResult<NoticeManagementListItemResult> findAllNotices(NoticeSearchCondition condition, PageQuery pageQuery) {
        String titlePattern = containsPattern(condition.title());
        String contentPattern = containsPattern(condition.content());

        long total = noticeQueryMyBatisMapper.countManagement(titlePattern, contentPattern, condition.visible());
        List<NoticeManagementListItemResult> notices = noticeQueryMyBatisMapper.selectManagement(
            titlePattern,
            contentPattern,
            condition.visible(),
            offsetOf(pageQuery),
            pageQuery.size()
        );

        return PageResult.of(notices, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<NoticeDetailResult> findDetailById(Long id) {
        return noticeQueryMyBatisMapper.selectDetailById(id);
    }

    @Override
    public PageResult<NoticeListItemResult> findVisibleNotices(PageQuery pageQuery) {
        long total = noticeQueryMyBatisMapper.countVisible();
        List<NoticeListItemResult> notices = noticeQueryMyBatisMapper.selectVisible(offsetOf(pageQuery), pageQuery.size());

        return PageResult.of(notices, total, pageQuery.page(), pageQuery.size());
    }

    static String containsPattern(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        StringBuilder escaped = new StringBuilder("%");
        for (char ch : keyword.toLowerCase(Locale.ROOT).toCharArray()) {
            if (ch == LIKE_ESCAPE || ch == '%' || ch == '_') {
                escaped.append(LIKE_ESCAPE);
            }
            escaped.append(ch);
        }
        return escaped.append('%').toString();
    }

    private static long offsetOf(PageQuery pageQuery) {
        return (long) pageQuery.page() * pageQuery.size();
    }
}
