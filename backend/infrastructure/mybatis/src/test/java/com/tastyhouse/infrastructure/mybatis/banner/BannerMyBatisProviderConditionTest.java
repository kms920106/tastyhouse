package com.tastyhouse.infrastructure.mybatis.banner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BannerMyBatisProviderConditionTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(MyBatisAdapterOnly.class)
        .withBean(BannerMyBatisMapper.class, () -> mock(BannerMyBatisMapper.class));

    @Test
    @DisplayName("provider=mybatis이면 MyBatis 구현이 등록된다")
    void registersForMyBatis() {
        runner.withPropertyValues("persistence.banner.write.provider=mybatis")
            .run(context -> assertThat(context).hasSingleBean(BannerMyBatisPersistenceAdapter.class));
    }

    @Test
    @DisplayName("속성이 없거나 jpa·알 수 없는 값이면 MyBatis 구현이 등록되지 않는다")
    void skipsOtherwise() {
        runner.run(context -> assertThat(context).doesNotHaveBean(BannerMyBatisPersistenceAdapter.class));
        runner.withPropertyValues("persistence.banner.write.provider=jpa")
            .run(context -> assertThat(context).doesNotHaveBean(BannerMyBatisPersistenceAdapter.class));
        runner.withPropertyValues("persistence.banner.write.provider=foo")
            .run(context -> assertThat(context).doesNotHaveBean(BannerMyBatisPersistenceAdapter.class));
    }

    @Configuration(proxyBeanMethods = false)
    @Import(BannerMyBatisPersistenceAdapter.class)
    static class MyBatisAdapterOnly {
    }
}
