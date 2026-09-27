package com.tastyhouse.infrastructure.shared.persistence;

import jakarta.persistence.Embeddable;

@Embeddable
public record VerificationCodeEmbeddable(String value) {
}
