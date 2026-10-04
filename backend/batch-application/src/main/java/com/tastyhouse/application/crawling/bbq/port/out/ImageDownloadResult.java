package com.tastyhouse.application.crawling.bbq.port.out;

public record ImageDownloadResult(
    DownloadedImage image,
    ImageDownloadFailure failure
) {

    public ImageDownloadResult {
        if ((image == null) == (failure == null)) {
            throw new IllegalArgumentException("이미지 다운로드 결과는 이미지와 실패 사유 중 정확히 하나를 가져야 합니다.");
        }
    }

    public static ImageDownloadResult downloaded(DownloadedImage image) {
        return new ImageDownloadResult(image, null);
    }

    public static ImageDownloadResult failed(ImageDownloadFailure failure) {
        return new ImageDownloadResult(null, failure);
    }

    public boolean success() {
        return failure == null;
    }
}
