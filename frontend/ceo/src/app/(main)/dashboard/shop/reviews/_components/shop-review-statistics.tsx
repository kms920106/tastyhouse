import { Progress } from "@/components/ui/progress";
import { REVIEW_RATING_ASPECTS } from "@/feature/shop-review/constants";
import type { ShopReviewStatistics } from "@/feature/shop-review/domain";
import { formatCount, formatPercentage, formatRating, toRatingBarPercentage } from "@/feature/shop-review/format";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";

import { ShopReviewRatingChart } from "./shop-review-rating-chart";

interface ShopReviewStatisticsPanelProps {
  /** 조회 실패 시 undefined */
  statistics?: ShopReviewStatistics;
  failed?: boolean;
}

function KpiTile({ label, value, hint }: { label: string; value: string; hint?: string }) {
  return (
    <div className="flex flex-col gap-1 rounded-lg border p-4">
      <span className="text-muted-foreground text-xs">{label}</span>
      <span className="font-semibold text-2xl tabular-nums">{value}</span>
      {hint && <span className="text-muted-foreground text-xs">{hint}</span>}
    </div>
  );
}

/**
 * 리뷰 통계 대시보드.
 *
 * **`hasData=false` 면 이 영역을 아예 렌더하지 않는다** — 원문 규격이 "당일 포함 최근 180일 간
 * 리뷰가 1개도 없다면 전체 대시보드는 노출되지 않습니다"이기 때문이다. 이때 목록 영역만
 * `Empty` 상태로 보이므로 화면이 비어 보이지 않는다.
 *
 * 통계만 실패한 경우도 목록을 죽이지 않고 안내 한 줄로 끝낸다(`page.tsx` 의 `allSettled` 참고).
 */
export function ShopReviewStatisticsPanel({ statistics, failed = false }: ShopReviewStatisticsPanelProps) {
  if (failed || !statistics) {
    return <p className="text-destructive text-sm">{SHOP_REVIEW_COPY.STATISTICS_LOAD_FAILED}</p>;
  }

  if (!statistics.hasData) return null;

  // 막대 채움 비율의 분모 — 별점 분포의 합이 6개월 리뷰 수와 항상 같지는 않으므로 분포 합을 쓴다.
  const distributionTotal = statistics.ratingCounts.reduce((sum, entry) => sum + entry.count, 0);

  const aspectRatings: Record<(typeof REVIEW_RATING_ASPECTS)[number]["key"], number | null> = {
    taste: statistics.averageTasteRating,
    amount: statistics.averageAmountRating,
    price: statistics.averagePriceRating,
    atmosphere: statistics.averageAtmosphereRating,
    kindness: statistics.averageKindnessRating,
    hygiene: statistics.averageHygieneRating,
  };

  return (
    <section className="flex flex-col gap-4">
      <h2 className="font-medium text-sm">{SHOP_REVIEW_COPY.STATISTICS_SECTION_TITLE}</h2>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <KpiTile label={SHOP_REVIEW_COPY.STAT_AVERAGE_RATING} value={formatRating(statistics.averageTotalRating)} />
        <KpiTile
          label={SHOP_REVIEW_COPY.STAT_TOTAL_REVIEW_COUNT}
          value={formatCount(statistics.totalReviewCount)}
          hint={SHOP_REVIEW_COPY.STAT_TOTAL_REVIEW_COUNT_HINT}
        />
        <KpiTile
          label={SHOP_REVIEW_COPY.STAT_RECENT_REVIEW_COUNT}
          value={formatCount(statistics.recentReviewCount)}
          hint={SHOP_REVIEW_COPY.STAT_RECENT_REVIEW_COUNT_HINT}
        />
        <KpiTile
          label={SHOP_REVIEW_COPY.STAT_WILL_REVISIT}
          value={formatPercentage(statistics.willRevisitPercentage)}
          hint={SHOP_REVIEW_COPY.STAT_WILL_REVISIT_HINT}
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* 별점 분포 — 키 1~5 가 항상 존재하므로 0건인 별점도 0 으로 보인다 */}
        <div className="flex flex-col gap-2">
          <h3 className="font-medium text-sm">{SHOP_REVIEW_COPY.RATING_DISTRIBUTION_TITLE}</h3>
          <ul className="flex flex-col gap-2">
            {statistics.ratingCounts.map((entry) => (
              <li key={entry.rating} className="flex items-center gap-3">
                <span className="w-10 shrink-0 text-muted-foreground text-xs tabular-nums">
                  {entry.rating}
                  {SHOP_REVIEW_COPY.RATING_SUFFIX}
                </span>
                <Progress value={toRatingBarPercentage(entry.count, distributionTotal)} className="h-2 flex-1" />
                <span className="w-12 shrink-0 text-right text-xs tabular-nums">{formatCount(entry.count)}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* 항목별 평균 */}
        <div className="flex flex-col gap-2">
          <h3 className="font-medium text-sm">{SHOP_REVIEW_COPY.ASPECT_RATING_TITLE}</h3>
          <dl className="grid grid-cols-2 gap-x-6 gap-y-2 sm:grid-cols-3">
            {REVIEW_RATING_ASPECTS.map((aspect) => (
              <div key={aspect.key} className="flex items-baseline justify-between gap-2">
                <dt className="text-muted-foreground text-xs">{aspect.label}</dt>
                <dd className="font-medium text-sm tabular-nums">{formatRating(aspectRatings[aspect.key])}</dd>
              </div>
            ))}
          </dl>
        </div>
      </div>

      <ShopReviewRatingChart monthlyStats={statistics.monthlyStats} />
    </section>
  );
}
