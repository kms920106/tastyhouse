package com.tastyhouse.adminapi.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.RequestParam;

public record PageRequest(
    @RequestParam(defaultValue = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    @Schema(description = "페이지 번호(0부터 시작)", example = "0")
    int page,

    @RequestParam(defaultValue = "10")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    @Schema(description = "페이지 크기", example = "10")
    int size
) {
}
