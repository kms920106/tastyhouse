package com.tastyhouse.infrastructure.shop.persistence;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.tastyhouse.domain.shop.domain.model.ProhibitedWord;
import com.tastyhouse.domain.shop.domain.repository.ProhibitedWordRepository;

/**
 * 금칙어 전량 로드를 TTL 동안 재사용하는 캐싱 데코레이터(write 포트 어댑터 감싸기).
 *
 * <p>{@code ProhibitedWordValidator}는 텍스트 검증 때마다 {@link ProhibitedWordRepository#findAll()}을
 * 호출하는데, 점주 입력(가게소개·찾아오는길 등) 저장 경로마다 금칙어 테이블을 통째로 다시 읽는 것이
 * 낭비다. 검증기는 domain-module의 순수 POJO라 스프링 {@code @Cacheable}을 붙일 수 없으므로, 캐싱을
 * 도메인이 아니라 <b>어댑터 쪽</b>에 둔다 — 검증기·도메인 서비스 코드는 그대로 두고 빈 등록 지점
 * ({@code DomainServiceConfig})에서 이 데코레이터를 주입하는 것으로 끝난다.
 *
 * <p>금칙어는 SQL 시드로 관리되는 read-only 데이터(Java 계층에 생성·수정 경로가 없다)라 정합성 리스크가
 * 낮다. 그래도 무기한 캐싱은 시드 갱신이 재기동 전까지 반영되지 않으므로 TTL을 둬서 자연히 만료시킨다.
 *
 * <p>{@link AtomicReference}에 (적재 시각, 목록) 스냅샷을 통째로 담아 교체하므로 락이 필요 없다. 만료
 * 직후 동시 호출이 겹치면 적재가 중복될 수 있으나, 결과가 같은 read-only 조회라 무해하다(중복 적재를
 * 막는 락이 주는 이득보다 락 경합 비용이 크다).
 */
public class CachingProhibitedWordRepository implements ProhibitedWordRepository {

    /**
     * 시드 갱신이 이 시간 안에는 반영된다. 금칙어는 자주 바뀌지 않으므로 넉넉히 잡는다.
     */
    private static final Duration TTL = Duration.ofMinutes(10);

    private final ProhibitedWordRepository delegate;
    private final AtomicReference<Snapshot> cache = new AtomicReference<>();

    public CachingProhibitedWordRepository(ProhibitedWordRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<ProhibitedWord> findAll() {
        Snapshot snapshot = cache.get();
        long now = System.nanoTime();

        if (snapshot != null && now - snapshot.loadedAtNanos() < TTL.toNanos()) {
            return snapshot.words();
        }

        List<ProhibitedWord> words = List.copyOf(delegate.findAll());
        cache.set(new Snapshot(now, words));
        return words;
    }

    /**
     * 적재 시각과 목록을 함께 교체하기 위한 불변 스냅샷.
     */
    private record Snapshot(long loadedAtNanos, List<ProhibitedWord> words) {
    }
}
