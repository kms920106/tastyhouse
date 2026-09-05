package com.tastyhouse.infrastructure.redis;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * infrastructure:redis 모듈의 auto-configuration — Redis 연결 템플릿과 레이트 리밋 카운터.
 *
 * <p>{@link RedisConfig}(StringRedisTemplate)와 {@code ratelimit} 패키지의
 * {@code RedisRateLimitCounter}를 등록한다. rate limit의 표현 관심사
 * ({@code @RateLimit}·{@code RateLimitAspect}·{@code RateLimitException})는 api-common-module에 있고,
 * aspect 빈은 {@code ApiCommonRateLimitAutoConfiguration}이 이 카운터 빈의 존재를 조건으로 등록한다.
 *
 * <p>클래스패스 존재만으로 활성화된다. batch-module은 Redis를 직접 쓰지 않지만
 * {@code application → security-core → infrastructure:redis} 전이 의존으로 이 모듈을 갖고 있어
 * 여기 빈들이 함께 뜬다. 전환 전에도 {@code InfrastructureModuleConfig}의
 * {@code com.tastyhouse.infrastructure} 통째 스캔이 같은 빈을 올리고 있었으므로 빈 집합은 동일하다.
 *
 * <p><b>{@code before = RedisAutoConfiguration}</b> — {@link RedisConfig}의
 * {@code stringRedisTemplate}은 Boot {@code RedisAutoConfiguration}의 동명 빈과 이름이 겹친다.
 * 이 모듈이 먼저 정의돼야 Boot 쪽 {@code @ConditionalOnMissingBean(StringRedisTemplate.class)}가
 * 그 정의를 보고 물러나고, 키·값 serializer를 명시 지정한 우리 템플릿이 이긴다. 전환 전에는 앱의
 * {@code @Import}가 auto-configuration보다 먼저 처리돼 자연히 이겼으나, 이 설정도
 * auto-configuration이 된 지금은 순서를 명시해야 한다.
 *
 * <p>{@code after}로 두면 Boot 쪽이 먼저 등록되고, 그 뒤 <b>스캔된</b> {@link RedisConfig}(조건 없는
 * {@code @Configuration})가 같은 이름으로 다시 등록을 시도해
 * {@code allow-bean-definition-overriding=false}(기본값)에 걸려 <b>기동이 실패한다</b> — 실제로
 * 4개 앱 전부 이렇게 실패했다. 즉 실패 지점은 "Boot의 조건이 물러나지 않아서"가 아니라
 * "스캔 빈이 auto-config 빈을 덮으려다 override 금지에 걸려서"다. 어느 쪽이든 조용한 회귀가 아니라
 * 기동 실패라는 점이 이 이름 충돌의 안전판이다.
 *
 * <p><b>{@link RedisConfig}에 {@code @ConditionalOnMissingBean}을 붙이지 않는 것은 의도된 선택이다.</b>
 * 붙이면 순서와 무관하게 Boot 쪽이 이겨 serializer 명시 지정이 사라진다 — 우리가 이 빈을 소유하는
 * 이유 자체가 없어진다. 이름 충돌은 조건이 아니라 {@code before} 순서로 해소한다.
 *
 * <p>{@code RedisConnectionFactory}는 Boot가 만들지만 빈 <i>정의</i> 순서와 <i>생성</i> 순서는 다르다 —
 * 주입은 생성 시점에 해결되므로 {@code before}로 두어도 팩토리를 정상적으로 주입받는다.
 */
@AutoConfiguration(before = RedisAutoConfiguration.class)
@ComponentScan("com.tastyhouse.infrastructure.redis")
public class RedisModuleAutoConfiguration {
}
