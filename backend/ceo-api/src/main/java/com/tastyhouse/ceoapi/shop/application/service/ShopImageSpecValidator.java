package com.tastyhouse.ceoapi.shop.application.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주가 업로드하는 가게 이미지(상표/콘텐츠)의 규격(형식·용량·해상도·비율)을 검증한다.
 *
 * <p>상표 이미지는 JPG·900KB 이하·최소 560x560·1:1 비율만 허용하고, 콘텐츠보드 이미지는
 * IMAGE(JPG/PNG)·GIF 각각 다른 용량·해상도 기준을 적용한다. 위반 시 규격 불일치를 단일
 * {@link ErrorCode#SHOP_IMAGE_SPEC_INVALID}로 통일해 알린다.
 */
@Component
public class ShopImageSpecValidator {

    private static final long TRADEMARK_MAX_SIZE_BYTES = 900L * 1024;
    private static final int TRADEMARK_MIN_WIDTH = 560;
    private static final int TRADEMARK_MIN_HEIGHT = 560;

    private static final long CONTENT_IMAGE_MAX_SIZE_BYTES = 10L * 1024 * 1024;
    private static final int CONTENT_IMAGE_MIN_WIDTH = 700;
    private static final int CONTENT_IMAGE_MIN_HEIGHT = 700;
    private static final int CONTENT_GIF_MIN_WIDTH = 250;
    private static final int CONTENT_GIF_MIN_HEIGHT = 250;

    private static final long NOTICE_IMAGE_MAX_SIZE_BYTES = 10L * 1024 * 1024;
    private static final int NOTICE_IMAGE_MIN_WIDTH = 640;
    private static final int NOTICE_IMAGE_MIN_HEIGHT = 280;

    public void validateTrademark(MultipartFile file) {
        validateNotEmpty(file);
        validateContentType(file, "image/jpeg");
        validateMaxSize(file, TRADEMARK_MAX_SIZE_BYTES);

        BufferedImage image = readImage(file);
        validateMinResolution(image, TRADEMARK_MIN_WIDTH, TRADEMARK_MIN_HEIGHT);
        validateSquareRatio(image);
    }

    public void validateContentImage(MultipartFile file, boolean gif) {
        validateNotEmpty(file);
        validateMaxSize(file, CONTENT_IMAGE_MAX_SIZE_BYTES);

        if (gif) {
            validateContentType(file, "image/gif");
            BufferedImage image = readImage(file);
            validateMinResolution(image, CONTENT_GIF_MIN_WIDTH, CONTENT_GIF_MIN_HEIGHT);
        } else {
            validateContentType(file, "image/jpeg", "image/png");
            BufferedImage image = readImage(file);
            validateMinResolution(image, CONTENT_IMAGE_MIN_WIDTH, CONTENT_IMAGE_MIN_HEIGHT);
        }
    }

    /**
     * 점주 공지 첨부 이미지 규격을 검증한다.
     *
     * <p>권장 사이즈(1280x560)를 그대로 강제하지 않고 그 절반(640x280)을 최소 해상도로 잡는다 — 정확히
     * 일치하는 이미지만 허용하면 등록 실패가 잦아지므로 지나치게 작은 이미지만 거르고 권장 사이즈는 프론트
     * 안내 문구로 처리한다. 공지 이미지는 가로형이라 비율({@code validateSquareRatio})은 적용하지 않는다.
     */
    public void validateNoticeImage(MultipartFile file) {
        validateNotEmpty(file);
        validateContentType(file, "image/jpeg", "image/png");
        validateMaxSize(file, NOTICE_IMAGE_MAX_SIZE_BYTES);
        validateMinResolution(readImage(file), NOTICE_IMAGE_MIN_WIDTH, NOTICE_IMAGE_MIN_HEIGHT);
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_SPEC_INVALID, "이미지 파일이 비어있습니다.");
        }
    }

    private void validateContentType(MultipartFile file, String... allowedContentTypes) {
        String contentType = file.getContentType();
        for (String allowed : allowedContentTypes) {
            if (allowed.equalsIgnoreCase(contentType)) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.SHOP_IMAGE_SPEC_INVALID, "허용되지 않는 이미지 형식입니다: " + contentType);
    }

    private void validateMaxSize(MultipartFile file, long maxSizeBytes) {
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_SPEC_INVALID, "이미지 용량이 허용치를 초과했습니다: " + file.getSize());
        }
    }

    private void validateMinResolution(BufferedImage image, int minWidth, int minHeight) {
        if (image.getWidth() < minWidth || image.getHeight() < minHeight) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_SPEC_INVALID,
                "이미지 해상도가 최소 기준(" + minWidth + "x" + minHeight + ")보다 작습니다.");
        }
    }

    private void validateSquareRatio(BufferedImage image) {
        if (image.getWidth() != image.getHeight()) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_SPEC_INVALID, "이미지 비율은 1:1이어야 합니다.");
        }
    }

    private BufferedImage readImage(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new BusinessException(ErrorCode.SHOP_IMAGE_SPEC_INVALID, "이미지 파일을 읽을 수 없습니다.");
            }
            return image;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_SPEC_INVALID, "이미지 파일을 읽을 수 없습니다.");
        }
    }
}
