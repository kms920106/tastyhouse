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
 * 점주가 업로드하는 메뉴모음컷의 규격(형식·용량·해상도)을 업로드 <b>전에</b> 검증한다.
 *
 * <p>{@code ShopImageSpecValidator}·{@code ProductImageSpecValidator}와 같은 형태로 presentation에
 * 둔다 — 규격은 화면 계약이고 domain은 통과분의 {@code fileId}만 받는다. 검증을 업로드 뒤로 미루면
 * 규격 미달 파일이 스토리지에 남는다.
 *
 * <p><b>기준이 메뉴 이미지와 같은데도 별도 검증기를 두는 이유</b>는 던지는 에러코드가 다르기 때문이다 —
 * 메뉴모음컷 규격 위반은 {@code SHOP_MENU_COLLECTION_IMAGE_SPEC_INVALID}로 내려가야 프론트가 어느
 * 업로드가 거절됐는지 분기할 수 있다. 기존 검증기를 재사용하면 메뉴 이미지 코드가 나가 화면이
 * 엉뚱한 안내를 띄운다.
 *
 * <p>메뉴모음컷 기준: JPG·PNG, 15MB 이하, 최소 1280x960. 비율은 강제하지 않는다 — 여러 메뉴를 한 데
 * 모은 컷이라 가로·세로 구도가 모두 정당하고, 앱이 배너 영역에서 크롭한다.
 */
@Component
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
