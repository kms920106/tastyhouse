package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardUpdateCommand;

@Schema(description = "가게 콘텐츠보드 수정 요청 (IMAGE/GIF는 file, VIDEO는 youtubeUrl 사용)")
public record ShopContentBoardUpdateRequest(
    @NotBlank(message = "콘텐츠 주제는 필수입니다.")
    @Schema(description = "콘텐츠 주제", example = "EXTERIOR", allowableValues = {"EXTERIOR", "INTERIOR", "FOOD_STORY", "NEWS"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String topic,

    @Schema(description = "이미지/GIF 파일 (콘텐츠보드가 IMAGE 또는 GIF인 경우, 변경 시에만 전달)")
    MultipartFile file,

    @Schema(description = "유튜브 영상 URL (콘텐츠보드가 VIDEO인 경우)", example = "https://www.youtube.com/watch?v=abcdefg")
    String youtubeUrl,

    @Schema(description = "설명 (최대 50자)", example = "매장 외부 전경입니다.")
    String description
) {

    public ShopContentBoardUpdateCommand toCommand(Long ceoId, Long shopId, Long contentBoardId) {
        return new ShopContentBoardUpdateCommand(ceoId, shopId, contentBoardId, topic(), youtubeUrl(), description());
    }
}
