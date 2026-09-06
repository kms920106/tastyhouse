package com.tastyhouse.domain.search.model;

public class PopularKeyword {
    private final Long id;
    private final String keyword;
    private final int rank;
    private final boolean newKeyword;
    private final boolean visible;

    private PopularKeyword(Long id, String keyword, int rank, boolean newKeyword, boolean visible) {
        this.id = id;
        this.keyword = keyword;
        this.rank = rank;
        this.newKeyword = newKeyword;
        this.visible = visible;
    }

    public static PopularKeyword of(String keyword, int rank, boolean newKeyword) {
        return new PopularKeyword(null, keyword, rank, newKeyword, true);
    }

    public static PopularKeyword reconstitute(Long id, String keyword, int rank, boolean newKeyword, boolean visible) {
        return new PopularKeyword(id, keyword, rank, newKeyword, visible);
    }

    public Long getId() {
        return this.id;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public int getRank() {
        return this.rank;
    }

    public boolean isNewKeyword() {
        return this.newKeyword;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
