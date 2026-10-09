package com.tastyhouse.infrastructure.jpa.banner.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.application.banner.port.out.write.BannerLoadPort;
import com.tastyhouse.application.banner.port.out.write.BannerSavePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BannerJpaPersistenceAdapterRegistrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(JpaAdapterOnly.class)
        .withBean(JPAQueryFactory.class, () -> mock(JPAQueryFactory.class))
        .withBean(BannerJpaRepository.class, () -> mock(BannerJpaRepository.class));

    @Test
    @DisplayName("속성 없이 등록되고 같은 빈 하나가 BannerLoadPort와 BannerSavePort 둘 다로 주입된다")
    void registersUnconditionallyAndServesBothPorts() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(BannerJpaPersistenceAdapter.class);
            assertThat(context.getBean(BannerLoadPort.class)).isSameAs(context.getBean(BannerSavePort.class));
            assertThat(context.getBean(BannerLoadPort.class)).isInstanceOf(BannerJpaPersistenceAdapter.class);
        });
    }

    @Test
    @DisplayName("@Primary가 없는 다른 구현이 함께 있어도 두 포트 모두 JPA 구현으로 주입된다")
    void winsOverNonPrimaryImplementation() {
        runner.withBean(OtherBannerPersistence.class, OtherBannerPersistence::new).run(context -> {
            assertThat(context.getBean(BannerLoadPort.class)).isInstanceOf(BannerJpaPersistenceAdapter.class);
            assertThat(context.getBean(BannerSavePort.class)).isInstanceOf(BannerJpaPersistenceAdapter.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import(BannerJpaPersistenceAdapter.class)
    static class JpaAdapterOnly {
    }

    static class OtherBannerPersistence implements BannerLoadPort, BannerSavePort {

        @Override
        public Optional<Banner> findById(BannerId id) {
            return Optional.empty();
        }

        @Override
        public Banner save(Banner banner) {
            return banner;
        }
    }
}
