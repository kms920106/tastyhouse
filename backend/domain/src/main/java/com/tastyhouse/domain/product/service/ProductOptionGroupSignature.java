package com.tastyhouse.domain.product.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;

public final class ProductOptionGroupSignature {
    private static final String FIELD_SEPARATOR = "|";
    private static final String OPTION_SEPARATOR = ",";
    private static final String OPTION_FIELD_SEPARATOR = ":";

    private ProductOptionGroupSignature() {
    }

    public static String of(ProductOptionGroup group, List<ProductOption> options) {
        return hash(payloadOf(group, options));
    }

    public static String hash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    public static String payloadOf(ProductOptionGroup group, List<ProductOption> options) {
        List<ProductOption> visible = options.stream()
            .filter(ProductOption::isVisible)
            .sorted(Comparator
                .comparing(ProductOption::getName, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(ProductOption::getAdditionalPrice,
                    Comparator.nullsFirst(Comparator.naturalOrder())))
            .toList();

        StringBuilder optionPayload = new StringBuilder();
        for (int index = 0; index < visible.size(); index++) {
            if (index > 0) {
                optionPayload.append(OPTION_SEPARATOR);
            }
            ProductOption option = visible.get(index);
            optionPayload
                .append(option.getName())
                .append(OPTION_FIELD_SEPARATOR)
                .append(option.getAdditionalPrice() != null ? option.getAdditionalPrice() : 0);
        }

        return String.join(
            FIELD_SEPARATOR,
            group.getName(),
            text(group.getMinSelect()),
            text(group.getMaxSelect()),
            String.valueOf(visible.size()),
            optionPayload.toString()
        );
    }

    private static String text(Integer value) {
        return value == null ? "" : String.valueOf(value);
    }
}
