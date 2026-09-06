package com.tastyhouse.domain.product.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;

/**
 * 옵션그룹 <b>동일성 서명</b>의 단일 진실원.
 *
 * <p>추천 합치기의 판정 기준(그룹명 + 최소/최대 선택 개수 + 옵션명·가격 집합)을 하나의 문자열로
 * 정규화한 뒤 SHA-256으로 해싱한다. 이 서명이 같은 그룹들이 "합칠 수 있는 중복 묶음"이며,
 * 점주가 [X]로 제외한 대상도 <b>그룹 id 쌍이 아니라 이 서명</b>으로 저장된다.
 *
 * <p><b>해싱은 Java에서만 한다 — SQL {@code SHA2}와 두 벌로 유지하지 않는다.</b> 미세한 인코딩·정렬
 * 차이만으로 제외 기능이 조용히 깨지는데, 그것이 이 기능의 가장 큰 correctness 리스크다. SQL은 원시
 * payload만 만들고 해시는 반드시 이 클래스가 계산한다.
 *
 * <p><b>정규화 규칙</b>
 * <ul>
 *   <li>옵션은 <b>이름·가격 오름차순으로 정렬</b>한다 — {@code sort}는 그룹마다 다르지만 동일성에는
 *       영향이 없다(같은 옵션 묶음을 다른 순서로 진열했을 뿐이다).</li>
 *   <li><b>숨은 옵션은 참여하지 않는다</b> — 눈에 똑같은 두 그룹이 유령 옵션 하나 때문에 다르다고
 *       판정되면 추천이 무의미해진다.</li>
 *   <li><b>옵션 개수를 payload에 포함한다</b> — MySQL {@code group_concat_max_len} 기본 1024바이트가
 *       옵션 30개쯤에서 목록을 잘라, 서로 다른 그룹이 같다고 판정되는 <b>조용한 오탐</b>이 생긴다.
 *       개수를 앞에 두면 잘린 목록끼리도 개수로 갈린다.</li>
 *   <li>{@code null}인 min/max는 빈 문자열로 표기해 "미지정"과 "0"을 구분한다.</li>
 * </ul>
 */
public final class ProductOptionGroupSignature {

    private static final String FIELD_SEPARATOR = "|";
    private static final String OPTION_SEPARATOR = ",";
    private static final String OPTION_FIELD_SEPARATOR = ":";

    private ProductOptionGroupSignature() {
    }

    /** 그룹과 그 옵션 목록으로부터 서명을 계산한다. */
    public static String of(ProductOptionGroup group, List<ProductOption> options) {
        return hash(payloadOf(group, options));
    }

    /**
     * SQL이 만든 원시 payload를 해싱한다.
     *
     * <p>추천 쿼리는 {@code GROUP_CONCAT}으로 payload를 조립해 내려주고, 해싱만 여기서 한다 —
     * 위 클래스 주석의 "해시는 Java에서만" 원칙을 지키는 지점이다.
     */
    public static String hash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256은 JDK 표준 스펙상 반드시 존재한다 — 도달 불가능한 분기다.
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    /**
     * 해싱 전의 원시 payload를 만든다. SQL의 {@code CONCAT_WS} 결과와 <b>글자 단위로 같아야 한다</b> —
     * 두 경로가 어긋나면 추천 목록의 서명과 제외 저장의 서명이 달라져 제외가 동작하지 않는다.
     */
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

    /** {@code null}은 빈 문자열로 — SQL의 {@code IFNULL(x, '')}과 같은 표기를 쓴다. */
    private static String text(Integer value) {
        return value == null ? "" : String.valueOf(value);
    }
}
