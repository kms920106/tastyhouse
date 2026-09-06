package com.tastyhouse.domain.review.service;

import java.util.List;

import com.tastyhouse.domain.review.model.Review;

public record ReviewRegistration(
    Review review,
    List<Long> uploadedFileIds,
    List<String> tags
) {
}
