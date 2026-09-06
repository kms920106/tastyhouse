package com.tastyhouse.infrastructure.shop.persistence;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;

public class CachingProhibitedWordRepository implements ProhibitedWordRepository {
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

    private record Snapshot(long loadedAtNanos, List<ProhibitedWord> words) {
    }
}
