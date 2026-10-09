package com.tastyhouse.infrastructure.jpa.shared.persistence;

import jakarta.persistence.Embeddable;

@Embeddable
public record PhoneNumberEmbeddable(String value) {
}
