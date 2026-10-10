package com.tastyhouse.infrastructure.mybatis.notice.query;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

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

class NoticeMyBatisQueryAdapterRegistrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(MyBatisAdapterOnly.class)
        .withBean(NoticeQueryMyBatisMapper.class, () -> mock(NoticeQueryMyBatisMapper.class));

    @Test
    @DisplayName("속성 없이 등록되고 같은 빈 하나가 NoticeQueryPort와 NoticeManagementQueryPort 둘 다로 주입된다")
    void registersUnconditionallyAndServesBothPorts() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(NoticeMyBatisQueryAdapter.class);
            assertThat(context.getBean(NoticeQueryPort.class)).isSameAs(context.getBean(NoticeManagementQueryPort.class));
            assertThat(context.getBean(NoticeQueryPort.class)).isInstanceOf(NoticeMyBatisQueryAdapter.class);
        });
    }

    @Test
    @DisplayName("@Primary 구현이 따로 있으면 그쪽이 주입된다 — MyBatis 구현은 대표가 아니다")
    void yieldsToPrimaryImplementation() {
        runner.withUserConfiguration(PrimaryOther.class).run(context -> {
            assertThat(context.getBean(NoticeQueryPort.class)).isInstanceOf(OtherNoticeQuery.class);
            assertThat(context.getBean(NoticeManagementQueryPort.class)).isInstanceOf(OtherNoticeQuery.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import(NoticeMyBatisQueryAdapter.class)
    static class MyBatisAdapterOnly {
    }

    @Configuration(proxyBeanMethods = false)
    static class PrimaryOther {

        @Bean
        @Primary
        OtherNoticeQuery otherNoticeQuery() {
            return new OtherNoticeQuery();
        }
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
