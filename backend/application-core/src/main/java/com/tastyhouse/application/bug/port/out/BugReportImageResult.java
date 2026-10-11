package com.tastyhouse.application.bug.port.out;

public record BugReportImageResult(
    Long fileId,
    String fileName,
    String imageUrl
) {
}
