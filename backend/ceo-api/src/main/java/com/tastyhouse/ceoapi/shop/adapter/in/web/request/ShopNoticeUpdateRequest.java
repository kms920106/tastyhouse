package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.shop.port.in.ShopNoticeUpdateCommand;

@Schema(description = "점주 공지 수정 요청 (이미지는 replace-all로 교체)")
public record ShopNoticeUpdateRequest(
    @NotBlank(message = "공지 본문은 필수입니다.")
    @Size(max = 2000, message = "공지 본문은 최대 2000자까지 입력할 수 있습니다.")
    @Schema(description = "공지 본문 (최대 2000자)", example = "이번 주 신메뉴 출시했습니다. 많은 이용 부탁드립니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String content,

    @Schema(description = "교체할 첨부 이미지 (최대 3장). keepExistingImages가 false일 때만 사용하며, 비어 있으면 기존 이미지를 전부 삭제합니다.")
    List<MultipartFile> files,

    @Schema(description = "기존 이미지를 그대로 둘지 여부 (기본 false). true면 files를 무시하고 본문만 수정합니다.", example = "false")
    Boolean keepExistingImages
) {

    public ShopNoticeUpdateCommand toCommand(Long ceoId, Long shopId, Long noticeId) {
        return new ShopNoticeUpdateCommand(ceoId, shopId, noticeId, content(), keepExistingImages());
    }
}
