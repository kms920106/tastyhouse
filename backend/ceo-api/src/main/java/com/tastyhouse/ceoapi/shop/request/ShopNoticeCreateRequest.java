package com.tastyhouse.ceoapi.shop.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점주 공지 등록 요청")
public record ShopNoticeCreateRequest(
    @NotBlank(message = "공지 본문은 필수입니다.")
    @Size(max = 2000, message = "공지 본문은 최대 2000자까지 입력할 수 있습니다.")
    @Schema(description = "공지 본문 (최대 2000자)", example = "이번 주 신메뉴 출시했습니다. 많은 이용 부탁드립니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String content,

    @Schema(description = "첨부 이미지 (최대 3장, JPG/PNG, 10MB 이하, 최소 640x280 / 권장 1280x560)")
    List<MultipartFile> files,

    @Schema(description = "등록과 동시에 앱에 반영할지 여부 (기본 false)", example = "false")
    Boolean exposed
) {
}
