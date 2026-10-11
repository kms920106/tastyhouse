package com.tastyhouse.application.point.port.out;

public record PointSearchCondition(
    Long memberId,
    String pointType
) {

    public static PointSearchCondition of(Long memberId, String pointType) {
        return new PointSearchCondition(memberId, pointType);
    }
}
