package com.tastyhouse.application.shared.exception;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCatalogSnapshotTest {

    private static final Set<String> DELETED = Set.of(
        "ENTITY_NOT_FOUND",
        "ADMIN_NOT_FOUND",
        "FILE_DELETE_FAILED",
        "REVIEW_BLIND_ATTACHMENT_LIMIT_EXCEEDED",
        "MENU_REVIEW_NOT_FOUND",
        "SHOP_IMAGE_CATEGORY_NOT_FOUND",
        "SHOP_CLOSED_DAY_NOT_FOUND",
        "SHOP_AMENITY_NOT_FOUND",
        "SHOP_FOOD_TYPE_NOT_FOUND",
        "TAG_NOT_FOUND",
        "SHOP_ORDER_METHOD_NOT_FOUND",
        "SHOP_BANNER_IMAGE_NOT_FOUND",
        "SHOP_BUSINESS_HOUR_OVERLAP",
        "SHOP_PHONE_NUMBER_PRIMARY_REQUIRED",
        "SHOP_CONVENIENCE_INFO_NOT_FOUND",
        "SHOP_DELIVERY_TIP_NOT_FOUND",
        "SHOP_DELIVERY_TIP_REGION_NOT_FOUND",
        "SHOP_DELIVERY_TIP_SCHEDULE_NOT_FOUND",
        "SHOP_DELIVERY_TIP_HOLIDAY_NOT_FOUND",
        "SHOP_DELIVERY_AREA_POLYGON_NOT_FOUND",
        "PRODUCT_REPRESENTATIVE_REQUEST_ALREADY_PENDING",
        "FOLLOWER_REMOVE_ACCESS_DENIED",
        "SOCIAL_EMAIL_REQUIRED"
    );

    @Test
    @DisplayName("분할한 카탈로그의 (name, code, status, message) 집합은 분할 전 ErrorCode에서 삭제분만 뺀 집합과 같다")
    void splitCatalogsPreserveWireContract() throws IOException {
        Set<String> before;
        try (InputStream in = getClass().getResourceAsStream("/error-catalog-before.tsv")) {
            assertThat(in).isNotNull();
            before = Arrays.stream(new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n"))
                .filter(line -> !line.isBlank())
                .map(line -> line.split("\t", -1))
                .filter(columns -> !DELETED.contains(columns[0]))
                .map(columns -> columns[0] + "|" + columns[2] + "|" + columns[1] + "|" + columns[3])
                .collect(Collectors.toSet());
        }

        Set<String> after = ErrorCatalog.all().stream()
            .map(entry -> entry.name() + "|" + entry.code() + "|" + entry.status() + "|" + entry.message())
            .collect(Collectors.toSet());

        assertThat(before).hasSize(419);
        assertThat(after).isEqualTo(before);
    }
}
