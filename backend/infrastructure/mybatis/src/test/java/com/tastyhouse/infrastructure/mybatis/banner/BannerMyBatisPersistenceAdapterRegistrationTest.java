package com.tastyhouse.infrastructure.mybatis.banner;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.application.banner.port.out.write.BannerLoadPort;
import com.tastyhouse.application.banner.port.out.write.BannerSavePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BannerMyBatisPersistenceAdapterRegistrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(MyBatisAdapterOnly.class)
        .withBean(BannerMyBatisMapper.class, () -> mock(BannerMyBatisMapper.class));

    @Test
    @DisplayName("속성 없이 등록되고 같은 빈 하나가 BannerLoadPort와 BannerSavePort 둘 다로 주입된다")
    void registersUnconditionallyAndServesBothPorts() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(BannerMyBatisPersistenceAdapter.class);
            assertThat(context.getBean(BannerLoadPort.class)).isSameAs(context.getBean(BannerSavePort.class));
            assertThat(context.getBean(BannerLoadPort.class)).isInstanceOf(BannerMyBatisPersistenceAdapter.class);
        });
    }

    @Test
    @DisplayName("@Primary 구현이 따로 있으면 그쪽이 주입된다 — MyBatis 구현은 대표가 아니다")
    void yieldsToPrimaryImplementation() {
        runner.withUserConfiguration(PrimaryOther.class).run(context -> {
            assertThat(context.getBean(BannerLoadPort.class)).isInstanceOf(OtherBannerPersistence.class);
            assertThat(context.getBean(BannerSavePort.class)).isInstanceOf(OtherBannerPersistence.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import(BannerMyBatisPersistenceAdapter.class)
    static class MyBatisAdapterOnly {
    }

    @Configuration(proxyBeanMethods = false)
    static class PrimaryOther {

        @Bean
        @Primary
        OtherBannerPersistence otherBannerPersistence() {
            return new OtherBannerPersistence();
        }
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
