package com.tastyhouse.ceoapi.shop;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 매장 가격 인증에 첨부하는 <b>매장 가격표 이미지</b>의 규격(형식·용량·해상도)을 업로드 <b>전에</b>
 * 검증한다.
 *
 * <p>{@code ShopMenuCollectionImageSpecValidator}·{@code ShopImageSpecValidator}와 같은 형태로
 * presentation에 둔다 — 규격은 화면 계약이고 domain은 통과분의 {@code fileId}만 받는다. 검증을 업로드
 * 뒤로 미루면 규격 미달 파일이 스토리지에 남는다.
 *
 * <p><b>별도 검증기를 두는 이유는 던지는 에러코드가 다르기 때문이다</b> —
 * {@code SHOP_STORE_PRICE_LIST_IMAGE_SPEC_INVALID}로 내려가야 프론트가 어느 업로드가 거절됐는지
 * 분기해 안내 문구를 띄울 수 있다. 기존 검증기를 재사용하면 메뉴모음컷 코드가 나가 화면이 엉뚱한
 * 규격(1280x960)을 안내한다.
 *
 * <p>기준: JPG·PNG, 15MB 이하, 최소 750x350. 메뉴모음컷보다 최소 해상도가 낮은 것은 의도다 — 이
 * 이미지는 손님에게 노출되지 않고 <b>관리자가 실제 매장 가격과 대조하는 근거</b>일 뿐이라, 가격표
 * 글자를 읽을 수 있는 최소선만 요구하고 점주가 매장에서 찍은 사진이 반려되는 일을 줄인다. 비율은
 * 강제하지 않는다(가격표는 세로로 긴 것이 보통이다).
 */
@Component
public class StorePriceListImageSpecValidator {

    private static final long MAX_SIZE_BYTES = 15L * 1024 * 1024;
    private static final int MIN_WIDTH = 750;
    private static final int MIN_HEIGHT = 350;

    private static final String[] ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png"};

    public void validate(MultipartFile file) {
        validateNotEmpty(file);
        validateContentType(file);
        validateMaxSize(file);
        validateMinResolution(readImage(file));
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_LIST_IMAGE_SPEC_INVALID,
                "가격표 이미지 파일이 비어있습니다.");
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (allowed.equalsIgnoreCase(contentType)) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_LIST_IMAGE_SPEC_INVALID,
            "허용되지 않는 이미지 형식입니다: " + contentType);
    }

    private void validateMaxSize(MultipartFile file) {
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_LIST_IMAGE_SPEC_INVALID,
                "이미지 용량이 허용치를 초과했습니다: " + file.getSize());
        }
    }

    private void validateMinResolution(BufferedImage image) {
        if (image.getWidth() < MIN_WIDTH || image.getHeight() < MIN_HEIGHT) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_LIST_IMAGE_SPEC_INVALID,
                "이미지 해상도가 최소 기준(" + MIN_WIDTH + "x" + MIN_HEIGHT + ")보다 작습니다.");
        }
    }

    private BufferedImage readImage(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_LIST_IMAGE_SPEC_INVALID,
                    "이미지 파일을 읽을 수 없습니다.");
            }
            return image;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_LIST_IMAGE_SPEC_INVALID,
                "이미지 파일을 읽을 수 없습니다.");
        }
    }
}
