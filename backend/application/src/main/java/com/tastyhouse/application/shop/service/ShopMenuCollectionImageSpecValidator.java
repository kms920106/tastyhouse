package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Component
@CeoApp
public class ShopMenuCollectionImageSpecValidator {

    private static final long MAX_SIZE_BYTES = 15L * 1024 * 1024;
    private static final int MIN_WIDTH = 1280;
    private static final int MIN_HEIGHT = 960;

    private static final String[] ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png"};

    public void validate(MultipartFile file) {
        validateNotEmpty(file);
        validateContentType(file);
        validateMaxSize(file);
        validateMinResolution(readImage(file));
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_SPEC_INVALID,
                "이미지 파일이 비어있습니다.");
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (allowed.equalsIgnoreCase(contentType)) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_SPEC_INVALID,
            "허용되지 않는 이미지 형식입니다: " + contentType);
    }

    private void validateMaxSize(MultipartFile file) {
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_SPEC_INVALID,
                "이미지 용량이 허용치를 초과했습니다: " + file.getSize());
        }
    }

    private void validateMinResolution(BufferedImage image) {
        if (image.getWidth() < MIN_WIDTH || image.getHeight() < MIN_HEIGHT) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_SPEC_INVALID,
                "이미지 해상도가 최소 기준(" + MIN_WIDTH + "x" + MIN_HEIGHT + ")보다 작습니다.");
        }
    }

    private BufferedImage readImage(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_SPEC_INVALID,
                    "이미지 파일을 읽을 수 없습니다.");
            }
            return image;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_SPEC_INVALID,
                "이미지 파일을 읽을 수 없습니다.");
        }
    }
}
