package com.tastyhouse.infrastructure.persistence.banner.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BannerJpaProviderConditionTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(JpaAdapterOnly.class)
        .withBean(JPAQueryFactory.class, () -> mock(JPAQueryFactory.class))
        .withBean(BannerJpaRepository.class, () -> mock(BannerJpaRepository.class));

    @Test
    @DisplayName("속성이 없거나 jpa이면 JPA 구현이 등록된다")
    void registersByDefaultAndForJpa() {
        runner.run(context -> assertThat(context).hasSingleBean(BannerJpaPersistenceAdapter.class));
        runner.withPropertyValues("persistence.banner.write.provider=jpa")
            .run(context -> assertThat(context).hasSingleBean(BannerJpaPersistenceAdapter.class));
    }

    @Test
    @DisplayName("mybatis나 알 수 없는 값이면 JPA 구현이 등록되지 않는다")
    void skipsForOtherProviders() {
        runner.withPropertyValues("persistence.banner.write.provider=mybatis")
            .run(context -> assertThat(context).doesNotHaveBean(BannerJpaPersistenceAdapter.class));
        runner.withPropertyValues("persistence.banner.write.provider=foo")
            .run(context -> assertThat(context).doesNotHaveBean(BannerJpaPersistenceAdapter.class));
    }

    @Configuration(proxyBeanMethods = false)
    @Import(BannerJpaPersistenceAdapter.class)
    static class JpaAdapterOnly {
    }
}
