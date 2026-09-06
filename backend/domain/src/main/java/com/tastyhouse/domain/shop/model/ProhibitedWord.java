package com.tastyhouse.domain.shop.model;

public class ProhibitedWord {
    private final Long id;
    private final String word;
    private final String reason;

    private ProhibitedWord(Long id, String word, String reason) {
        this.id = id;
        this.word = word;
        this.reason = reason;
    }

    public static ProhibitedWord reconstitute(Long id, String word, String reason) {
        return new ProhibitedWord(id, word, reason);
    }

    public Long getId() {
        return this.id;
    }

    public String getWord() {
        return this.word;
    }

    public String getReason() {
        return this.reason;
    }
}
