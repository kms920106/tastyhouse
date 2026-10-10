package com.tastyhouse.infrastructure.jpa.notice.query;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tastyhouse.application.notice.port.out.NoticeDetailResult;
import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementQueryPort;
import com.tastyhouse.application.notice.port.out.NoticeQueryPort;
import com.tastyhouse.application.notice.port.out.NoticeSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NoticeJpaQueryAdapterRegistrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(JpaAdapterOnly.class)
        .withBean(JPAQueryFactory.class, () -> mock(JPAQueryFactory.class));

    @Test
    @DisplayName("속성 없이 등록되고 같은 빈 하나가 NoticeQueryPort와 NoticeManagementQueryPort 둘 다로 주입된다")
    void registersUnconditionallyAndServesBothPorts() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(NoticeJpaQueryAdapter.class);
            assertThat(context.getBean(NoticeQueryPort.class)).isSameAs(context.getBean(NoticeManagementQueryPort.class));
            assertThat(context.getBean(NoticeQueryPort.class)).isInstanceOf(NoticeJpaQueryAdapter.class);
        });
    }

    @Test
    @DisplayName("@Primary가 없는 다른 구현이 함께 있어도 두 포트 모두 JPA 구현으로 주입된다")
    void winsOverNonPrimaryImplementation() {
        runner.withBean(OtherNoticeQuery.class, OtherNoticeQuery::new).run(context -> {
            assertThat(context.getBean(NoticeQueryPort.class)).isInstanceOf(NoticeJpaQueryAdapter.class);
            assertThat(context.getBean(NoticeManagementQueryPort.class)).isInstanceOf(NoticeJpaQueryAdapter.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import(NoticeJpaQueryAdapter.class)
    static class JpaAdapterOnly {
    }

    static class OtherNoticeQuery implements NoticeQueryPort, NoticeManagementQueryPort {

        @Override
        public PageResult<NoticeListItemResult> findVisibleNotices(PageQuery pageQuery) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        @Override
        public PageResult<NoticeManagementListItemResult> findAllNotices(NoticeSearchCondition condition, PageQuery pageQuery) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        @Override
        public Optional<NoticeDetailResult> findDetailById(Long id) {
            return Optional.empty();
        }
    }
}
