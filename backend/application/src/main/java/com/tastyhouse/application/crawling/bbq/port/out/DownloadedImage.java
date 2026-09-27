package com.tastyhouse.application.crawling.bbq.port.out;

public record DownloadedImage(
    byte[] bytes,
    String contentType,
    String filename
) {
}
