package com.tastyhouse.infrastructure.mybatis.notice.persistence;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.application.notice.port.out.write.NoticeLoadPort;
import com.tastyhouse.application.notice.port.out.write.NoticeSavePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NoticeMyBatisPersistenceAdapterRegistrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(MyBatisAdapterOnly.class)
        .withBean(NoticeMyBatisMapper.class, () -> mock(NoticeMyBatisMapper.class));

    @Test
    @DisplayName("속성 없이 등록되고 같은 빈 하나가 NoticeLoadPort와 NoticeSavePort 둘 다로 주입된다")
    void registersUnconditionallyAndServesBothPorts() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(NoticeMyBatisPersistenceAdapter.class);
            assertThat(context.getBean(NoticeLoadPort.class)).isSameAs(context.getBean(NoticeSavePort.class));
            assertThat(context.getBean(NoticeLoadPort.class)).isInstanceOf(NoticeMyBatisPersistenceAdapter.class);
        });
    }

    @Test
    @DisplayName("@Primary 구현이 따로 있으면 그쪽이 주입된다 — MyBatis 구현은 대표가 아니다")
    void yieldsToPrimaryImplementation() {
        runner.withUserConfiguration(PrimaryOther.class).run(context -> {
            assertThat(context.getBean(NoticeLoadPort.class)).isInstanceOf(OtherNoticePersistence.class);
            assertThat(context.getBean(NoticeSavePort.class)).isInstanceOf(OtherNoticePersistence.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import(NoticeMyBatisPersistenceAdapter.class)
    static class MyBatisAdapterOnly {
    }

    @Configuration(proxyBeanMethods = false)
    static class PrimaryOther {

        @Bean
        @Primary
        OtherNoticePersistence otherNoticePersistence() {
            return new OtherNoticePersistence();
        }
    }

    static class OtherNoticePersistence implements NoticeLoadPort, NoticeSavePort {

        @Override
        public Optional<Notice> findActiveById(NoticeId noticeId) {
            return Optional.empty();
        }

        @Override
        public Notice save(Notice notice) {
            return notice;
        }
    }
}
