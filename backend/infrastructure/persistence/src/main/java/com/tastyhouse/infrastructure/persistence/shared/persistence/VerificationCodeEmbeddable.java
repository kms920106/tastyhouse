package com.tastyhouse.infrastructure.persistence.shared.persistence;

import jakarta.persistence.Embeddable;

@Embeddable
public record VerificationCodeEmbeddable(String value) {
}
