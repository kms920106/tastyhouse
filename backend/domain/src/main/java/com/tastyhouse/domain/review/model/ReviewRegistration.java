package com.tastyhouse.domain.review.model;

import java.util.List;

public record ReviewRegistration(
    Review review,
    List<Long> uploadedFileIds,
    List<String> tags
) {
}
