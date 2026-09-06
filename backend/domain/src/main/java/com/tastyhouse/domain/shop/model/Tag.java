package com.tastyhouse.domain.shop.model;

public class Tag {
    private final Long id;
    private final String tagName;

    private Tag(Long id, String tagName) {
        this.id = id;
        this.tagName = tagName;
    }

    public static Tag of(String tagName) {
        return new Tag(null, tagName);
    }

    public static Tag reconstitute(Long id, String tagName) {
        return new Tag(id, tagName);
    }

    public Long getId() {
        return this.id;
    }

    public String getTagName() {
        return this.tagName;
    }
}
