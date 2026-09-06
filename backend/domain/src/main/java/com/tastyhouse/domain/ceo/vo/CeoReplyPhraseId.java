package com.tastyhouse.domain.ceo.vo;

public record CeoReplyPhraseId(Long value) {

    public CeoReplyPhraseId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("CeoReplyPhraseId는 양수여야 합니다: " + value);
        }
    }

    public static CeoReplyPhraseId of(Long value) {
        return new CeoReplyPhraseId(value);
    }
}
