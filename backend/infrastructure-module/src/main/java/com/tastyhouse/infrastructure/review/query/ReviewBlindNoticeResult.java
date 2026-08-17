package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindReason;

/**
 * 고객에게 보여줄 게시중단 안내(동의·거부 화면의 조회 결과).
 *
 * <p><b>이 조회가 따로 필요한 이유</b>: 일반 리뷰 상세 조회({@code ReviewQueryDao})는
 * {@code hidden.isFalse()} 필터를 걸어 게시중단된 리뷰에 404를 낸다. 그 필터는 "게시중단은 정책 위반
 * 제재"라는 판단에 따른 것이라 완화하면 안 되므로, 작성자 본인에게만 열리는 이 경로를 별도로 둔다.
 *
 * <p>가게명·사유는 "왜 중단됐는지"를, {@code blindUntil}은 "동의하지 않으면 언제 다시 노출되는지"를
 * 답한다 — 고객이 동의 여부를 판단하는 데 필요한 최소 정보다.
 *
 * <p>{@code imageUrls}는 다건이라 본 쿼리에 join하지 않고 별도 조회 후 위더로 채운다.
 */
public record ReviewBlindNoticeResult(
    Long reviewId,
    String content,
    List<String> imageUrls,
    LocalDateTime createdAt,
    String shopName,
    ReviewBlindReason reason,
    String detailReason,
    LocalDateTime blindUntil,
    Long reviewMemberId
) {

    /**
     * 리뷰 사진 URL을 채워 넣는다.
     *
     * <p><b>필드 선언 순서와 인자 순서를 한 필드씩 대조한다</b> — {@code String}·{@code LocalDateTime}이
     * 각각 연속해 있어 자리를 바꿔도 컴파일되고 값만 조용히 뒤바뀐다(DTO 조립 규칙의 경고).
     */
    public ReviewBlindNoticeResult withImageUrls(List<String> imageUrls) {
        return new ReviewBlindNoticeResult(
            this.reviewId,
            this.content,
            imageUrls,
            this.createdAt,
            this.shopName,
            this.reason,
            this.detailReason,
            this.blindUntil,
            this.reviewMemberId
        );
    }
}
