"use client";

import * as React from "react";

import { useRouter } from "next/navigation";

import { ChevronLeft } from "lucide-react";
import { toast } from "sonner";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Switch } from "@/components/ui/switch";
import { loadProductNutritionAction, updateMenuAction } from "@/feature/product/actions";
import { SPICINESS_OPTIONS, VEGETARIAN_TYPE_OPTIONS } from "@/feature/product/constants";
import type {
  LinkedProductSummary,
  MenuCategory,
  MenuDetail,
  MenuExposure,
  MenuNutrition,
  MenuOptionGroup,
  MenuPrice,
  MenuVegetarian,
} from "@/feature/product/domain";
import { formatPrice } from "@/feature/product/format";
import {
  PRODUCT_DETAIL_COPY,
  PRODUCT_DETAIL_SCREEN_COPY,
  PRODUCT_MENU_COPY,
  PRODUCT_MENU_MESSAGE,
  PRODUCT_NUTRITION_COPY,
} from "@/feature/product/message";
import type { MenuFormValues } from "@/feature/product/schema";

import { SettingRow } from "../../../_components/setting-row";
import { CATEGORY_NONE_VALUE, type MenuBasicSection, MenuBasicSheet } from "./menu-basic-sheets";
import { MenuExposureSheet } from "./menu-exposure-sheet";
import { MenuImageSheet } from "./menu-image-sheet";
import { MenuNutritionSheet } from "./menu-nutrition-sheet";
import { MenuOptionGroupSheet } from "./menu-option-group-sheet";
import { MenuPriceSheet } from "./menu-price-sheet";
import { MenuVegetarianSheet } from "./menu-vegetarian-sheet";

interface MenuDetailManageProps {
  productId: number;
  shopId: number;
  /** 상세 조회 실패 시 undefined 로 넘어온다 — 이때는 Sheet 를 열지 않고 안내만 보인다(§7) */
  detail?: MenuDetail;
  categories?: MenuCategory[];
  optionGroups?: MenuOptionGroup[];
  linkedProductsByGroupId?: Record<number, LinkedProductSummary[]>;
  errorMessage?: string;
}

/** 상세 응답 → 폼 값. Select 는 문자열만 다루고 미분류는 `NONE` 센티넬로 표현한다 */
function toFormValues(detail: MenuDetail): MenuFormValues {
  return {
    name: detail.name,
    productCategoryId: detail.productCategoryId === null ? CATEGORY_NONE_VALUE : String(detail.productCategoryId),
    composition: detail.composition ?? "",
    description: detail.description ?? "",
    weightText: detail.weightText ?? "",
    originalPrice: String(detail.originalPrice),
    discountPrice: detail.discountPrice === null ? "" : String(detail.discountPrice),
    singleServing: detail.singleServing,
    representative: detail.representative,
    spiciness: detail.spiciness === null ? "" : String(detail.spiciness),
    ratingExcluded: detail.ratingExcluded,
  };
}

export function MenuDetailManage({
  productId,
  shopId,
  detail,
  categories = [],
  optionGroups = [],
  linkedProductsByGroupId = {},
  errorMessage,
}: MenuDetailManageProps) {
  const router = useRouter();
  const [isPending, startTransition] = React.useTransition();

  const [basicSection, setBasicSection] = React.useState<MenuBasicSection | null>(null);
  const [exposureOpen, setExposureOpen] = React.useState(false);
  const [imageOpen, setImageOpen] = React.useState(false);
  const [vegetarianOpen, setVegetarianOpen] = React.useState(false);
  const [optionGroupOpen, setOptionGroupOpen] = React.useState(false);
  const [nutritionOpen, setNutritionOpen] = React.useState(false);
  const [priceOpen, setPriceOpen] = React.useState(false);

  // Sheet 안에서만 조회하는 값들의 요약. 상세 응답에 없어서 Sheet 가 알려줄 때까지 비어 있다.
  const [exposureSummary, setExposureSummary] = React.useState<MenuExposure | null>(null);
  const [imageSummary, setImageSummary] = React.useState<{ count: number; pending: boolean } | null>(null);
  const [vegetarianSummary, setVegetarianSummary] = React.useState<MenuVegetarian | null>(null);
  /** 영양성분은 상세 응답에 없어 Sheet 를 열어 조회할 때까지 모른다 — `undefined` 가 "아직 모름"이다 */
  const [nutritionSummary, setNutritionSummary] = React.useState<MenuNutrition | null | undefined>(undefined);
  /** 가격 행 목록도 상세 응답에 없다. Sheet 가 알려줄 때까지 기존 단일 가격 요약을 보인다 */
  const [priceSummaryRows, setPriceSummaryRows] = React.useState<MenuPrice[] | undefined>(undefined);

  const handleExposureSaved = React.useCallback((exposure: MenuExposure | null) => {
    setExposureSummary(exposure);
  }, []);

  const handleImageChanged = React.useCallback((count: number, pending: boolean) => {
    setImageSummary({ count, pending });
  }, []);

  const handleVegetarianChanged = React.useCallback((vegetarian: MenuVegetarian | null) => {
    setVegetarianSummary(vegetarian);
  }, []);

  const handleNutritionChanged = React.useCallback((nutrition: MenuNutrition | null) => {
    setNutritionSummary(nutrition);
  }, []);

  // 상세 응답에 영양성분 요약이 없어, 목록 행 요약을 채우려면 마운트 시 한 번 별도 조회해야 한다.
  React.useEffect(() => {
    if (!detail) return;
    let cancelled = false;
    loadProductNutritionAction(productId, shopId).then((result) => {
      if (!cancelled && result.success) {
        setNutritionSummary(result.data);
      }
    });
    return () => {
      cancelled = true;
    };
  }, [detail, productId, shopId]);

  const backToBoard = () => router.push(`/dashboard/shop/menus?shopId=${shopId}`);

  // 상세 조회 실패는 화면을 무너뜨리지 않는다 — Sheet 를 열지 않고 인라인 안내만 남긴다(§7).
  // 서버가 내려준 한국어 문구(403 `SHOP_ACCESS_DENIED` / 404 `PRODUCT_NOT_FOUND`)를 그대로 쓴다.
  if (!detail) {
    return (
      <Card>
        <CardHeader className="border-b">
          <CardTitle className="text-xl leading-none">{PRODUCT_DETAIL_COPY.PAGE_TITLE}</CardTitle>
          <CardDescription>{PRODUCT_DETAIL_COPY.PAGE_DESCRIPTION}</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col items-start gap-4">
          <Alert variant="destructive">
            <AlertTitle>{PRODUCT_DETAIL_SCREEN_COPY.DETAIL_UNAVAILABLE_TITLE}</AlertTitle>
            <AlertDescription>
              {errorMessage ?? PRODUCT_DETAIL_SCREEN_COPY.DETAIL_UNAVAILABLE_DESCRIPTION}
            </AlertDescription>
          </Alert>
          <Button type="button" variant="outline" onClick={backToBoard}>
            <ChevronLeft />
            {PRODUCT_DETAIL_COPY.BUTTON_BACK}
          </Button>
        </CardContent>
      </Card>
    );
  }

  const formValues = toFormValues(detail);

  /**
   * 저장은 항상 **전체 필드를 담은 PUT**이다(§2-2 — 부분 갱신 경로가 없다).
   *
   * 그래서 어떤 Sheet 든, 어떤 토글이든 현재 값 전량에 바뀐 것만 얹어 보낸다. 바뀐 필드만 보내면
   * 서버가 나머지를 null 로 치환해 **건드리지 않은 값이 지워진다.**
   */
  function submitUpdate(values: MenuFormValues, onSuccess?: () => void) {
    startTransition(async () => {
      const categoryId = values.productCategoryId === CATEGORY_NONE_VALUE ? null : Number(values.productCategoryId);
      const { success, message } = await updateMenuAction(productId, {
        shopId,
        productCategoryId: Number.isFinite(categoryId) ? categoryId : null,
        name: values.name,
        composition: values.composition,
        description: values.description,
        weightText: values.weightText,
        originalPrice: Number(values.originalPrice),
        discountPrice: values.discountPrice.trim() === "" ? null : Number(values.discountPrice),
        singleServing: values.singleServing,
        spiciness: values.spiciness.trim() === "" ? null : Number(values.spiciness),
        representative: values.representative,
        ratingExcluded: values.ratingExcluded,
      });

      if (!success) {
        // 중복·금칙어·특수문자(400)는 서버가 한국어 문구를 내려주므로 그대로 보여준다.
        toast.error(message ?? PRODUCT_MENU_MESSAGE.MENU_UPDATE_FAILED);
        return;
      }

      toast.success(PRODUCT_MENU_MESSAGE.MENU_UPDATE_SUCCESS);
      onSuccess?.();
      // 낙관적 업데이트를 하지 않는다 — 서버 응답 후 다시 읽어 화면과 손님 화면을 일치시킨다.
      router.refresh();
    });
  }

  /** 판매 옵션 3종은 행에서 바로 토글한다. 다른 필드는 현재 값 그대로 함께 실어 보낸다 */
  function toggleFlag(key: "singleServing" | "representative" | "ratingExcluded", checked: boolean) {
    submitUpdate({ ...formValues, [key]: checked });
  }

  const categorySummary = detail.productCategoryName ?? PRODUCT_MENU_COPY.PLACEHOLDER_CATEGORY_NONE;

  // 할인가와 맵기를 한 Sheet 에서 편집하므로 둘을 함께 요약한다. 미설정이면 표시하지 않는다.
  const discountRowSummary =
    [
      detail.discountPrice === null ? undefined : formatPrice(detail.discountPrice),
      SPICINESS_OPTIONS.find((option) => option.value === detail.spiciness)?.label,
    ]
      .filter((value) => value !== undefined)
      .join(" · ") || undefined;

  // 중량도 이 행에서 편집하므로 요약에 함께 보인다 — 미입력이면 무엇을 아직 안 적었는지 알 수 없다.
  const textRowSummary =
    [detail.composition ?? detail.description, detail.weightText]
      .filter((value) => value != null && value !== "")
      .join(" · ") || undefined;

  /**
   * 가격 요약.
   *
   * 가격 행 목록은 상세 응답에 없어 Sheet 를 열기 전에는 모른다. 그때까지는 기존 단일 가격
   * (`originalPrice`·`discountPrice`)으로 요약한다 — 행이 1개인 메뉴는 두 값이 일치한다.
   * 행이 2개 이상이면 가격명을 함께 보여야 어느 가격인지 알 수 있다.
   */
  const priceSummary =
    priceSummaryRows === undefined || priceSummaryRows.length === 0
      ? detail.discountPrice === null
        ? formatPrice(detail.originalPrice)
        : `${formatPrice(detail.discountPrice)} (${formatPrice(detail.originalPrice)})`
      : priceSummaryRows
          .map((row) =>
            row.priceName === null
              ? formatPrice(row.deliveryPrice)
              : `${row.priceName} ${formatPrice(row.deliveryPrice)}`,
          )
          .join(" · ");

  /**
   * 영양성분 요약.
   *
   * Sheet 를 아직 열지 않은 동안(`undefined`)은 값을 모르므로 아무것도 단정하지 않는다 —
   * "미입력"으로 표시하면 실제로는 입력돼 있는데 지워진 것처럼 보인다.
   */
  const nutritionSummaryNode = (() => {
    if (nutritionSummary === undefined) return undefined;
    if (nutritionSummary === null) return PRODUCT_NUTRITION_COPY.SUMMARY_EMPTY;

    const parts: string[] = [];
    if (nutritionSummary.calorie !== null) parts.push(`${nutritionSummary.calorie}kcal`);
    if (nutritionSummary.allergens.length > 0) {
      parts.push(`${nutritionSummary.allergens.length}${PRODUCT_NUTRITION_COPY.SUMMARY_ALLERGEN_SUFFIX}`);
    }
    if (nutritionSummary.setMenu) parts.push(PRODUCT_NUTRITION_COPY.SUMMARY_SET_MENU);

    return parts.length === 0 ? PRODUCT_NUTRITION_COPY.SUMMARY_EMPTY : parts.join(" · ");
  })();

  const exposureSummaryNode = (() => {
    // Sheet 를 아직 열지 않아 상세를 모르는 동안은 상세 응답의 exposureScheduled 플래그로 대신한다.
    const scheduled =
      exposureSummary === null
        ? detail.exposureScheduled
        : exposureSummary.hours.length > 0 || exposureSummary.startDate !== null || exposureSummary.endDate !== null;
    return scheduled
      ? PRODUCT_DETAIL_SCREEN_COPY.EXPOSURE_SUMMARY_SCHEDULED
      : PRODUCT_DETAIL_COPY.EXPOSURE_SUMMARY_ALWAYS;
  })();

  const imageSummaryNode =
    imageSummary === null ? (
      // Sheet 를 아직 열지 않아 목록을 모르는 동안은 상세의 대표 이미지 유무만 알 수 있다.
      detail.imageUrl === null ? (
        PRODUCT_DETAIL_COPY.IMAGE_EMPTY
      ) : (
        `1${PRODUCT_DETAIL_SCREEN_COPY.IMAGE_COUNT_SUFFIX}`
      )
    ) : (
      <span className="flex items-center gap-2">
        {`${imageSummary.count}${PRODUCT_DETAIL_SCREEN_COPY.IMAGE_COUNT_SUFFIX}`}
        {imageSummary.pending && <Badge variant="secondary">{PRODUCT_DETAIL_COPY.BADGE_PENDING}</Badge>}
      </span>
    );

  const vegetarianSummaryNode = (() => {
    const type = vegetarianSummary?.vegetarianType ?? detail.vegetarianType;
    const label = VEGETARIAN_TYPE_OPTIONS.find((option) => option.value === type)?.label ?? type;
    const status = vegetarianSummary?.pendingRequest?.status ?? null;
    return (
      <span className="flex items-center gap-2">
        {label ?? PRODUCT_DETAIL_COPY.NOT_SET}
        {status === "PENDING" && <Badge variant="secondary">{PRODUCT_DETAIL_COPY.BADGE_PENDING}</Badge>}
        {status === "REJECTED" && <Badge variant="destructive">{PRODUCT_DETAIL_COPY.BADGE_REJECTED}</Badge>}
      </span>
    );
  })();

  const linkedGroupCount = optionGroups.filter((group) =>
    (linkedProductsByGroupId[group.id] ?? []).some((product) => product.id === productId),
  ).length;

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{detail.name}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{PRODUCT_DETAIL_COPY.PAGE_DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:justify-end md:justify-self-end">
          <Button type="button" variant="outline" onClick={backToBoard}>
            <ChevronLeft />
            {PRODUCT_DETAIL_COPY.BUTTON_BACK}
          </Button>
        </CardAction>
      </CardHeader>

      <CardContent className="flex flex-col">
        <SettingRow
          title={PRODUCT_DETAIL_COPY.ROW_NAME}
          summary={detail.name}
          onAction={() => setBasicSection("name")}
        />
        <SettingRow
          title={PRODUCT_DETAIL_COPY.ROW_TEXT}
          summary={textRowSummary}
          onAction={() => setBasicSection("text")}
        />
        <SettingRow title={PRODUCT_DETAIL_COPY.ROW_PRICE} summary={priceSummary} onAction={() => setPriceOpen(true)} />
        <SettingRow
          title={PRODUCT_DETAIL_COPY.ROW_DISCOUNT}
          summary={discountRowSummary}
          onAction={() => setBasicSection("discount")}
        />
        <SettingRow
          title={PRODUCT_DETAIL_COPY.ROW_CATEGORY}
          summary={categorySummary}
          onAction={() => setBasicSection("category")}
        />

        {/* 판매 옵션은 Sheet 를 열 만한 입력이 아니라 행에서 바로 뒤집는다. 낙관적 업데이트 없이
            `useTransition` + `router.refresh()` 로 서버 응답을 기다린다. */}
        <div className="flex flex-col gap-3 border-b py-4">
          <span className="font-medium text-sm">{PRODUCT_DETAIL_COPY.ROW_FLAGS}</span>
          <div className="flex flex-col gap-3">
            <div className="flex items-center justify-between gap-4">
              <div className="flex min-w-0 flex-1 flex-col">
                <span className="text-sm">{PRODUCT_MENU_COPY.FIELD_SINGLE_SERVING}</span>
                <span className="text-muted-foreground text-xs leading-snug">
                  {PRODUCT_MENU_COPY.HELP_SINGLE_SERVING}
                </span>
              </div>
              <Switch
                aria-label={PRODUCT_MENU_COPY.FIELD_SINGLE_SERVING}
                checked={detail.singleServing}
                onCheckedChange={(checked) => toggleFlag("singleServing", checked)}
                disabled={isPending}
              />
            </div>
            <div className="flex items-center justify-between gap-4">
              <div className="flex min-w-0 flex-1 flex-col">
                <span className="text-sm">{PRODUCT_MENU_COPY.FIELD_REPRESENTATIVE}</span>
                <span className="text-muted-foreground text-xs leading-snug">
                  {PRODUCT_MENU_COPY.HELP_REPRESENTATIVE}
                </span>
              </div>
              <Switch
                aria-label={PRODUCT_MENU_COPY.FIELD_REPRESENTATIVE}
                checked={detail.representative}
                onCheckedChange={(checked) => toggleFlag("representative", checked)}
                disabled={isPending}
              />
            </div>
            <div className="flex items-center justify-between gap-4">
              <div className="flex min-w-0 flex-1 flex-col">
                <span className="text-sm">{PRODUCT_MENU_COPY.FIELD_RATING_EXCLUDED}</span>
                <span className="text-muted-foreground text-xs leading-snug">
                  {PRODUCT_MENU_COPY.HELP_RATING_EXCLUDED}
                </span>
              </div>
              <Switch
                aria-label={PRODUCT_MENU_COPY.FIELD_RATING_EXCLUDED}
                checked={detail.ratingExcluded}
                onCheckedChange={(checked) => toggleFlag("ratingExcluded", checked)}
                disabled={isPending}
              />
            </div>
          </div>
        </div>

        <SettingRow
          title={PRODUCT_DETAIL_COPY.ROW_EXPOSURE}
          description={PRODUCT_DETAIL_COPY.EXPOSURE_ALWAYS_DESCRIPTION}
          summary={exposureSummaryNode}
          onAction={() => setExposureOpen(true)}
        />
        <SettingRow
          title={PRODUCT_DETAIL_COPY.ROW_IMAGE}
          description={PRODUCT_DETAIL_COPY.IMAGE_APPROVAL_NOTICE}
          summary={imageSummaryNode}
          onAction={() => setImageOpen(true)}
        />
        <SettingRow
          title={PRODUCT_DETAIL_COPY.ROW_VEGETARIAN}
          description={PRODUCT_DETAIL_COPY.VEGETARIAN_APPROVAL_NOTICE}
          summary={vegetarianSummaryNode}
          onAction={() => setVegetarianOpen(true)}
        />
        <SettingRow
          title={PRODUCT_NUTRITION_COPY.ROW_TITLE}
          summary={nutritionSummaryNode}
          onAction={() => setNutritionOpen(true)}
        />
        <SettingRow
          title={PRODUCT_DETAIL_COPY.ROW_OPTION_GROUPS}
          summary={
            linkedGroupCount === 0
              ? PRODUCT_DETAIL_COPY.OPTION_GROUP_EMPTY
              : `${linkedGroupCount}${PRODUCT_DETAIL_SCREEN_COPY.OPTION_GROUP_COUNT_SUFFIX}`
          }
          onAction={() => setOptionGroupOpen(true)}
        />
      </CardContent>

      <MenuBasicSheet
        section={basicSection}
        onOpenChange={(open) => {
          if (!open) setBasicSection(null);
        }}
        pending={isPending}
        defaultValues={formValues}
        categories={categories}
        onSubmit={(values) => submitUpdate(values, () => setBasicSection(null))}
      />

      <MenuPriceSheet
        open={priceOpen}
        onOpenChange={setPriceOpen}
        productId={productId}
        shopId={shopId}
        // 상세 응답에 할인 기간이 없어 "대기·진행 중"을 정확히 판정할 수 없다. 할인가가 설정돼
        // 있으면 진행 중으로 보수적으로 본다 — 서버가 `PRODUCT_PRICE_DISCOUNT_IN_PROGRESS` 로
        // 한 번 더 막으므로, 잘못 열어 저장 실패를 겪는 쪽보다 미리 잠그는 쪽이 낫다.
        discountInProgress={detail.discountPrice !== null}
        onChanged={setPriceSummaryRows}
        onNavigateVerification={backToBoard}
      />

      <MenuExposureSheet
        open={exposureOpen}
        onOpenChange={setExposureOpen}
        productId={productId}
        shopId={shopId}
        onSaved={handleExposureSaved}
      />

      <MenuImageSheet
        open={imageOpen}
        onOpenChange={setImageOpen}
        productId={productId}
        shopId={shopId}
        onChanged={handleImageChanged}
      />

      <MenuVegetarianSheet
        open={vegetarianOpen}
        onOpenChange={setVegetarianOpen}
        productId={productId}
        shopId={shopId}
        onChanged={handleVegetarianChanged}
      />

      <MenuNutritionSheet
        open={nutritionOpen}
        onOpenChange={setNutritionOpen}
        productId={productId}
        shopId={shopId}
        onChanged={handleNutritionChanged}
      />

      <MenuOptionGroupSheet
        open={optionGroupOpen}
        onOpenChange={setOptionGroupOpen}
        productId={productId}
        shopId={shopId}
        optionGroups={optionGroups}
        linkedProductsByGroupId={linkedProductsByGroupId}
      />
    </Card>
  );
}
