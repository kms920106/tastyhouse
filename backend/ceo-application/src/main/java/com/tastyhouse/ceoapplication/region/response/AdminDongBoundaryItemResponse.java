package com.tastyhouse.ceoapplication.region.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 행정동 경계 한 건.
 *
 * <p><b>경계 미보유는 404가 아니라 {@code rings: null}인 200이다.</b> 시드가 코드·좌표 먼저, 경계는 나중에
 * 들어오므로 "좌표는 있고 경계는 없는" 상태가 정상이다. 그런 동을 목록에서 빼면 화면이 "이 지역에 행정동이
 * 없다"로 오해하게 된다.
 */
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

    public static AdminDongBoundaryItemResponse from(
        long adminDongId,
        String regionName,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        List<List<AdminDongPointResponse>> rings
    ) {
        return new AdminDongBoundaryItemResponse(
            adminDongId,
            regionName,
            centerLatitude,
            centerLongitude,
            rings
        );
    }
}
