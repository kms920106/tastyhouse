package com.tastyhouse.ceoapi.region.adapter.in.web.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.region.port.out.AdminDongBoundaryViewResult;

@Schema(description = "행정동 경계 한 건")
public record AdminDongBoundaryItemResponse(
    @Schema(description = "행정동 ID", example = "1101053")
    long adminDongId,

    @Schema(description = "행정동 전체 이름", example = "서울특별시 강남구 역삼1동")
    String regionName,

    @Schema(description = "대표점 위도", example = "37.500123")
    BigDecimal centerLatitude,

    @Schema(description = "대표점 경도", example = "127.036456")
    BigDecimal centerLongitude,

    @Schema(description = "경계 폴리곤(링 배열). 경계 미보유 시 null")
    List<List<AdminDongPointResponse>> rings
) {
    public static AdminDongBoundaryItemResponse from(AdminDongBoundaryViewResult result) {
        List<List<AdminDongBoundaryViewResult.Point>> rings = result.rings();
        return new AdminDongBoundaryItemResponse(
            result.adminDongId(),
            result.regionName(),
            result.centerLatitude(),
            result.centerLongitude(),
            rings == null ? null : rings.stream()
                .map(ring -> ring.stream().map(AdminDongPointResponse::from).toList())
                .toList()
        );
    }
}
