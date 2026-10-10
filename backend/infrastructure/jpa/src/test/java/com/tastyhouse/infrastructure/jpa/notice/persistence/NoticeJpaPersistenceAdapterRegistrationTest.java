package com.tastyhouse.infrastructure.jpa.notice.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.application.notice.port.out.write.NoticeLoadPort;
import com.tastyhouse.application.notice.port.out.write.NoticeSavePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NoticeJpaPersistenceAdapterRegistrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(JpaAdapterOnly.class)
        .withBean(JPAQueryFactory.class, () -> mock(JPAQueryFactory.class))
        .withBean(NoticeJpaRepository.class, () -> mock(NoticeJpaRepository.class));

    @Test
    @DisplayName("속성 없이 등록되고 같은 빈 하나가 NoticeLoadPort와 NoticeSavePort 둘 다로 주입된다")
    void registersUnconditionallyAndServesBothPorts() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(NoticeJpaPersistenceAdapter.class);
            assertThat(context.getBean(NoticeLoadPort.class)).isSameAs(context.getBean(NoticeSavePort.class));
            assertThat(context.getBean(NoticeLoadPort.class)).isInstanceOf(NoticeJpaPersistenceAdapter.class);
        });
    }

    @Test
    @DisplayName("@Primary가 없는 다른 구현이 함께 있어도 두 포트 모두 JPA 구현으로 주입된다")
    void winsOverNonPrimaryImplementation() {
        runner.withBean(OtherNoticePersistence.class, OtherNoticePersistence::new).run(context -> {
            assertThat(context.getBean(NoticeLoadPort.class)).isInstanceOf(NoticeJpaPersistenceAdapter.class);
            assertThat(context.getBean(NoticeSavePort.class)).isInstanceOf(NoticeJpaPersistenceAdapter.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import(NoticeJpaPersistenceAdapter.class)
    static class JpaAdapterOnly {
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
