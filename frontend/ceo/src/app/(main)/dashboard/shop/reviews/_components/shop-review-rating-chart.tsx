"use client";

import { Bar, CartesianGrid, ComposedChart, Line, XAxis, YAxis } from "recharts";

import { type ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart";
import type { ShopReviewMonthlyStat } from "@/feature/shop-review/domain";
import { formatYearMonthLabel } from "@/feature/shop-review/format";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";

/** 평균 별점 축은 0~5 고정 — 데이터에 따라 축이 흔들리면 달 간 비교가 왜곡된다 */
const RATING_DOMAIN = [0, 5] as const;

/**
 * 색을 CSS 변수에 매핑해 라이트/다크 모두에서 축·툴팁이 읽히게 한다.
 * (`chart.tsx` 가 `--color-{key}` 변수를 만들어 준다)
 */
const CHART_CONFIG = {
  averageRating: {
    label: SHOP_REVIEW_COPY.CHART_AVERAGE_RATING,
    color: "var(--chart-1)",
  },
  reviewCount: {
    label: SHOP_REVIEW_COPY.CHART_REVIEW_COUNT,
    color: "var(--chart-2)",
  },
} satisfies ChartConfig;

interface ShopReviewRatingChartProps {
  /** 최근 6개월(오래된 달 → 최신 달). 서버가 정확히 6개를 채워 내려준다 */
  monthlyStats: ShopReviewMonthlyStat[];
}

/**
 * 월별 평균 별점(line) + 리뷰 수(bar) 복합 그래프.
 *
 * 두 계열의 단위가 달라(별점 0~5 · 건수 0~N) Y축을 좌우로 분리한다.
 * 리뷰가 0건인 달은 `averageRating` 이 `null` 이라 line 을 이어붙이면 실제로 없는 추세를
 * 그리게 되므로 `connectNulls={false}` 로 끊는다.
 */
export function ShopReviewRatingChart({ monthlyStats }: ShopReviewRatingChartProps) {
  if (monthlyStats.length === 0) return null;

  const data = monthlyStats.map((stat) => ({
    label: formatYearMonthLabel(stat.yearMonth),
    averageRating: stat.averageRating,
    reviewCount: stat.reviewCount,
  }));

  return (
    <div className="flex flex-col gap-2">
      <div className="flex flex-col gap-1">
        <h3 className="font-medium text-sm">{SHOP_REVIEW_COPY.MONTHLY_CHART_TITLE}</h3>
        <p className="text-muted-foreground text-xs">{SHOP_REVIEW_COPY.MONTHLY_CHART_DESCRIPTION}</p>
      </div>

      <ChartContainer config={CHART_CONFIG} className="h-64 w-full">
        <ComposedChart accessibilityLayer data={data} margin={{ top: 8, right: 8, bottom: 0, left: 0 }}>
          <CartesianGrid vertical={false} />
          <XAxis dataKey="label" tickLine={false} axisLine={false} tickMargin={8} />
          <YAxis
            yAxisId="rating"
            domain={[...RATING_DOMAIN]}
            tickLine={false}
            axisLine={false}
            tickMargin={8}
            width={32}
          />
          <YAxis
            yAxisId="count"
            orientation="right"
            allowDecimals={false}
            tickLine={false}
            axisLine={false}
            tickMargin={8}
            width={32}
          />
          <ChartTooltip content={<ChartTooltipContent />} />
          <Bar yAxisId="count" dataKey="reviewCount" fill="var(--color-reviewCount)" radius={4} />
          <Line
            yAxisId="rating"
            type="monotone"
            dataKey="averageRating"
            stroke="var(--color-averageRating)"
            strokeWidth={2}
            dot={{ r: 3 }}
            connectNulls={false}
          />
        </ComposedChart>
      </ChartContainer>
    </div>
  );
}
