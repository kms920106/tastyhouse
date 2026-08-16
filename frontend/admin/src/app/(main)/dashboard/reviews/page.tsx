import { reviewService } from "@/api/review/review.service";
import { REVIEW_MESSAGE } from "@/feature/review/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { Reviews } from "./_components/reviews";

const MAX_PAGE_SIZE = 100;

function parsePositiveInt(value: string | string[] | undefined): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

/** "0"~"5" 범위의 평점(Double) 문자열을 파싱한다. 범위를 벗어나거나 숫자가 아니면 undefined. */
function parseOptionalRating(value: string | string[] | undefined): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  if (raw === undefined || raw === "") return undefined;
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed >= 0 && parsed <= 5 ? parsed : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/reviews">) {
  const {
    page: pageParam,
    size: sizeParam,
    shopId: shopIdParam,
    productId: productIdParam,
    memberId: memberIdParam,
    hidden: hiddenParam,
    ownerOnly: ownerOnlyParam,
    content: contentParam,
    minRating: minRatingParam,
    maxRating: maxRatingParam,
  } = await searchParams;

  const shopId = parsePositiveInt(shopIdParam);
  const productId = parsePositiveInt(productIdParam);
  const memberId = parsePositiveInt(memberIdParam);
  const hidden = parseOptionalBoolean(hiddenParam);
  const ownerOnly = parseOptionalBoolean(ownerOnlyParam);
  const content = parseSearchString(contentParam);
  const minRating = parseOptionalRating(minRatingParam);
  const maxRating = parseOptionalRating(maxRatingParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await reviewService.getReviews(
    { shopId, productId, memberId, hidden, ownerOnly, content, minRating, maxRating },
    { page, size },
  );

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "리뷰 목록 조회 실패");
    throw new Error(REVIEW_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <Reviews
      reviews={data}
      pagination={pagination}
      initialShopId={shopId}
      initialProductId={productId}
      initialMemberId={memberId}
      initialHidden={hidden}
      initialOwnerOnly={ownerOnly}
      initialContent={content}
      initialMinRating={minRating}
      initialMaxRating={maxRating}
    />
  );
}
