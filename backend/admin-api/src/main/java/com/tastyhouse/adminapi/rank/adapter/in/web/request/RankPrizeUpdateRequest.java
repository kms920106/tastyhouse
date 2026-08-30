package com.tastyhouse.adminapi.rank.adapter.in.web.request;

import com.tastyhouse.adminapplication.rank.port.in.RankPrizeUpdateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "랭킹 경품 수정 요청")
public record RankPrizeUpdateRequest(
    @NotNull(message = "등수는 필수입니다.")
    @Schema(description = "등수", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer prizeRank,

    @NotBlank(message = "경품명은 필수입니다.")
    @Schema(description = "경품명", example = "아이패드 Pro 11인치", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotBlank(message = "브랜드는 필수입니다.")
    @Schema(description = "브랜드", example = "Apple", requiredMode = Schema.RequiredMode.REQUIRED)
    String brand,

    @Schema(description = "업로드된 이미지 파일 ID", example = "56")
    Long imageFileId
) {

    public RankPrizeUpdateCommand toCommand(Long rankPrizeId) {
        return new RankPrizeUpdateCommand(rankPrizeId, prizeRank(), name(), brand(), imageFileId());
    }
}
