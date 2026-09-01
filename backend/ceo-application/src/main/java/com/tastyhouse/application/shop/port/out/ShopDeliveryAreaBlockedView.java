package com.tastyhouse.application.shop.port.out;

/**
 * 도형 저장 시 닫히지 못하는 행정동 한 건과 그 사유.
 *
 * <p><b>챕터 09</b>에서 신설. 사유 판정(지역별 배달팁이 걸려 있어 닫을 수 없다)은 application이 수행한다.
 */
public record ShopDeliveryAreaBlockedView(
    long adminDongId,
    String regionName,
    String reason
) {
}
