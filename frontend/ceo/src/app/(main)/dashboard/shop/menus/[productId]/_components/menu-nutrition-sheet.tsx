"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { ChevronRight } from "lucide-react";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import {
  deleteProductNutritionAction,
  loadAllergensAction,
  loadProductNutritionAction,
  updateProductNutritionAction,
} from "@/feature/product/actions";
import {
  NUTRITION_OPTIONAL_NUMERIC_KEYS,
  NUTRITION_REQUIRED_KEYS,
  NUTRITION_UNIT,
  type NutritionNumericKey,
} from "@/feature/product/constants";
import type { AllergenOption, MenuNutrition } from "@/feature/product/domain";
import {
  PRODUCT_DETAIL_SCREEN_COPY,
  PRODUCT_NUTRITION_COPY,
  PRODUCT_NUTRITION_MESSAGE,
} from "@/feature/product/message";
import { type NutritionFormValues, nutritionSchema } from "@/feature/product/schema";

const FORM_ID = "menu-nutrition-form";

/** 수치 입력란의 라벨. 키 순서가 아니라 이 표가 화면 문구를 정한다 */
const NUMERIC_LABEL: Record<NutritionNumericKey, string> = {
  calorie: PRODUCT_NUTRITION_COPY.LABEL_CALORIE,
  sugars: PRODUCT_NUTRITION_COPY.LABEL_SUGARS,
  protein: PRODUCT_NUTRITION_COPY.LABEL_PROTEIN,
  saturatedFat: PRODUCT_NUTRITION_COPY.LABEL_SATURATED_FAT,
  natrium: PRODUCT_NUTRITION_COPY.LABEL_NATRIUM,
  carbohydrate: PRODUCT_NUTRITION_COPY.LABEL_CARBOHYDRATE,
  cholesterol: PRODUCT_NUTRITION_COPY.LABEL_CHOLESTEROL,
  fat: PRODUCT_NUTRITION_COPY.LABEL_FAT,
  transFat: PRODUCT_NUTRITION_COPY.LABEL_TRANS_FAT,
  caffeine: PRODUCT_NUTRITION_COPY.LABEL_CAFFEINE,
};

/** 선택 텍스트 4종. 수치와 입력 형태가 달라 따로 돈다 */
const TEXT_FIELDS = [
  { key: "flavor", label: PRODUCT_NUTRITION_COPY.LABEL_FLAVOR },
  { key: "size", label: PRODUCT_NUTRITION_COPY.LABEL_SIZE },
  { key: "totalAmount", label: PRODUCT_NUTRITION_COPY.LABEL_TOTAL_AMOUNT },
  { key: "servingSize", label: PRODUCT_NUTRITION_COPY.LABEL_SERVING_SIZE },
] as const;

const EMPTY_VALUES: NutritionFormValues = {
  servingSize: "",
  totalAmount: "",
  flavor: "",
  size: "",
  calorie: "",
  sugars: "",
  protein: "",
  saturatedFat: "",
  natrium: "",
  carbohydrate: "",
  cholesterol: "",
  fat: "",
  transFat: "",
  caffeine: "",
  setMenu: false,
  allergens: [],
};

/** 수치는 null 이 곧 미입력이므로 빈 문자열로 편다 — 0 과 구분되어야 한다 */
function toFormValues(nutrition: MenuNutrition | null): NutritionFormValues {
  if (nutrition === null) return EMPTY_VALUES;

  const numeric = (value: number | null) => (value === null ? "" : String(value));
  return {
    servingSize: nutrition.servingSize ?? "",
    totalAmount: nutrition.totalAmount ?? "",
    flavor: nutrition.flavor ?? "",
    size: nutrition.size ?? "",
    calorie: numeric(nutrition.calorie),
    sugars: numeric(nutrition.sugars),
    protein: numeric(nutrition.protein),
    saturatedFat: numeric(nutrition.saturatedFat),
    natrium: numeric(nutrition.natrium),
    carbohydrate: numeric(nutrition.carbohydrate),
    cholesterol: numeric(nutrition.cholesterol),
    fat: numeric(nutrition.fat),
    transFat: numeric(nutrition.transFat),
    caffeine: numeric(nutrition.caffeine),
    setMenu: nutrition.setMenu,
    allergens: nutrition.allergens,
  };
}

interface MenuNutritionSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  productId: number;
  shopId: number;
  /** 상세 행 요약을 갱신한다. 미입력이면 null */
  onChanged: (nutrition: MenuNutrition | null) => void;
}

/**
 * 영양성분·알레르기 설정.
 *
 * 둘을 한 Sheet 에 두는 이유는 저장 엔드포인트가 하나이고(`PUT .../nutrition`), 점주 입장에서
 * "성분 정보 입력"이라는 한 가지 일이기 때문이다. 검수 대상이 아니라 저장 즉시 반영된다.
 */
export function MenuNutritionSheet({ open, onOpenChange, productId, shopId, onChanged }: MenuNutritionSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [allergenOptions, setAllergenOptions] = React.useState<AllergenOption[]>([]);
  /** 이미 저장된 정보가 있을 때만 삭제 버튼을 노출한다 */
  const [hasSaved, setHasSaved] = React.useState(false);

  const form = useForm<NutritionFormValues>({
    resolver: zodResolver(nutritionSchema),
    defaultValues: EMPTY_VALUES,
  });

  /**
   * 시트가 **열리는 순간에만** 서버 값으로 폼을 되돌린다.
   *
   * 조회 실패 시 폼을 비우지 않는다 — 전체 교체(PUT)라 빈 폼으로 저장하면 기존 값이 지워진다
   * (`frontend.md` §예외 처리 "빈 폼으로 위장하지 않는다").
   */
  const wasOpen = React.useRef(false);
  React.useEffect(() => {
    if (open && !wasOpen.current) {
      startTransition(async () => {
        const [allergenResult, nutritionResult] = await Promise.all([
          loadAllergensAction(),
          loadProductNutritionAction(productId, shopId),
        ]);

        // 목록 조회 실패는 저장을 막지 않는다 — 알레르기는 선택 항목이라 수치만 고칠 수 있어야 한다.
        if (!allergenResult.success || !allergenResult.data) {
          toast.error(allergenResult.message ?? PRODUCT_NUTRITION_MESSAGE.ALLERGEN_LOAD_FAILED);
        } else {
          setAllergenOptions(allergenResult.data);
        }

        if (!nutritionResult.success) {
          toast.error(nutritionResult.message ?? PRODUCT_NUTRITION_MESSAGE.LOAD_FAILED);
          return;
        }

        const nutrition = nutritionResult.data ?? null;
        setHasSaved(nutrition !== null);
        form.reset(toFormValues(nutrition));
      });
    }
    wasOpen.current = open;
  }, [open, productId, shopId, form]);

  const onSubmit = (values: NutritionFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateProductNutritionAction(productId, shopId, values);
      if (!success) {
        toast.error(message ?? PRODUCT_NUTRITION_MESSAGE.SAVE_FAILED);
        return;
      }

      toast.success(PRODUCT_NUTRITION_MESSAGE.SAVE_SUCCESS);
      setHasSaved(true);
      // 저장한 값을 그대로 요약에 반영한다 — 재조회 없이 화면과 서버가 일치한다.
      const { data } = await loadProductNutritionAction(productId, shopId);
      onChanged(data ?? null);
    });
  };

  const handleDelete = () => {
    startTransition(async () => {
      const { success, message } = await deleteProductNutritionAction(productId, shopId);
      if (!success) {
        toast.error(message ?? PRODUCT_NUTRITION_MESSAGE.DELETE_FAILED);
        return;
      }

      toast.success(PRODUCT_NUTRITION_MESSAGE.DELETE_SUCCESS);
      setHasSaved(false);
      form.reset(EMPTY_VALUES);
      onChanged(null);
    });
  };

  const renderNumericField = (key: NutritionNumericKey) => (
    <Controller
      key={key}
      control={form.control}
      name={key}
      render={({ field, fieldState }) => (
        <Field className="gap-1.5" data-invalid={fieldState.invalid}>
          <FieldLabel htmlFor={`menu-nutrition-${key}`}>{NUMERIC_LABEL[key]}</FieldLabel>
          <div className="flex items-center gap-2">
            <Input
              {...field}
              id={`menu-nutrition-${key}`}
              inputMode="numeric"
              disabled={isPending}
              aria-invalid={fieldState.invalid}
            />
            {/* 단위를 입력란 옆에 붙인다 — 숫자만 두면 무엇을 적는지 알 수 없다 */}
            <span className="w-10 shrink-0 text-muted-foreground text-sm">{NUTRITION_UNIT[key]}</span>
          </div>
          {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
        </Field>
      )}
    />
  );

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{PRODUCT_NUTRITION_COPY.SHEET_TITLE}</SheetTitle>
          <SheetDescription>{PRODUCT_NUTRITION_COPY.SHEET_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-4 overflow-y-auto px-4">
          {/* 의무표시 대상 판정을 시스템이 하지 않는다 — 대상 여부를 점주가 알 수 있게 문구로 알린다 */}
          <Alert>
            <AlertDescription className="text-xs leading-snug">{PRODUCT_NUTRITION_COPY.TARGET_NOTICE}</AlertDescription>
          </Alert>

          <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-4">
              <div className="flex flex-col gap-1">
                <span className="font-medium text-sm">{PRODUCT_NUTRITION_COPY.REQUIRED_SECTION_TITLE}</span>
                <span className="text-muted-foreground text-xs leading-snug">
                  {PRODUCT_NUTRITION_COPY.REQUIRED_SECTION_HELP}
                </span>
              </div>
              {NUTRITION_REQUIRED_KEYS.map(renderNumericField)}

              <Separator />

              <Collapsible className="group/optional rounded-md border px-3 py-2">
                <CollapsibleTrigger className="flex w-full items-center justify-between gap-2 text-left font-medium text-sm">
                  {PRODUCT_NUTRITION_COPY.OPTIONAL_SECTION_TITLE}
                  <ChevronRight className="size-4 transition-transform duration-200 group-data-[state=open]/optional:rotate-90" />
                </CollapsibleTrigger>
                <CollapsibleContent>
                  <FieldGroup className="mt-3 gap-4">
                    {TEXT_FIELDS.map(({ key, label }) => (
                      <Controller
                        key={key}
                        control={form.control}
                        name={key}
                        render={({ field, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={`menu-nutrition-${key}`}>{label}</FieldLabel>
                            <Input
                              {...field}
                              id={`menu-nutrition-${key}`}
                              disabled={isPending}
                              aria-invalid={fieldState.invalid}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />
                    ))}
                    {NUTRITION_OPTIONAL_NUMERIC_KEYS.map(renderNumericField)}
                  </FieldGroup>
                </CollapsibleContent>
              </Collapsible>

              <Separator />

              <Controller
                control={form.control}
                name="setMenu"
                render={({ field }) => (
                  <Field className="gap-1.5">
                    <FieldLabel htmlFor="menu-nutrition-set-menu">
                      {PRODUCT_NUTRITION_COPY.SET_MENU_SECTION_TITLE}
                    </FieldLabel>
                    <div className="flex items-center gap-2">
                      <Checkbox
                        id="menu-nutrition-set-menu"
                        checked={field.value}
                        onCheckedChange={(checked) => field.onChange(checked === true)}
                        disabled={isPending}
                      />
                      <FieldLabel htmlFor="menu-nutrition-set-menu" className="font-normal">
                        {PRODUCT_NUTRITION_COPY.SET_MENU_LABEL}
                      </FieldLabel>
                    </div>
                    <FieldDescription>{PRODUCT_NUTRITION_COPY.SET_MENU_HELP}</FieldDescription>
                  </Field>
                )}
              />

              <Separator />

              <Controller
                control={form.control}
                name="allergens"
                render={({ field }) => (
                  <Field className="gap-1.5">
                    <FieldLabel htmlFor="menu-nutrition-allergens">
                      {PRODUCT_NUTRITION_COPY.ALLERGEN_SECTION_TITLE}
                    </FieldLabel>
                    <FieldDescription>{PRODUCT_NUTRITION_COPY.ALLERGEN_SECTION_HELP}</FieldDescription>
                    {allergenOptions.length === 0 ? (
                      <span className="text-muted-foreground text-xs">{PRODUCT_NUTRITION_COPY.ALLERGEN_EMPTY}</span>
                    ) : (
                      <div id="menu-nutrition-allergens" className="grid grid-cols-2 gap-2">
                        {allergenOptions.map((option) => {
                          const checked = field.value.includes(option.code);
                          return (
                            <div key={option.code} className="flex items-center gap-2">
                              <Checkbox
                                id={`menu-nutrition-allergen-${option.code}`}
                                checked={checked}
                                disabled={isPending}
                                onCheckedChange={(next) => {
                                  field.onChange(
                                    next === true
                                      ? [...field.value, option.code]
                                      : field.value.filter((code) => code !== option.code),
                                  );
                                }}
                              />
                              <FieldLabel
                                htmlFor={`menu-nutrition-allergen-${option.code}`}
                                className="font-normal text-sm"
                              >
                                {option.label}
                              </FieldLabel>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </Field>
                )}
              />
            </FieldGroup>
          </form>

          {/* 이미 입력된 경우에만 노출한다 — 없는 정보를 지우는 버튼은 의미가 없다 */}
          {hasSaved && (
            <>
              <Separator />
              <div className="flex flex-col gap-2">
                <span className="font-medium text-sm">{PRODUCT_NUTRITION_COPY.DELETE_TITLE}</span>
                <span className="text-muted-foreground text-xs leading-snug">{PRODUCT_NUTRITION_COPY.DELETE_HELP}</span>
                <Button type="button" variant="destructive" disabled={isPending} onClick={handleDelete}>
                  {isPending ? PRODUCT_NUTRITION_COPY.ACTION_DELETING : PRODUCT_NUTRITION_COPY.ACTION_DELETE}
                </Button>
              </div>
            </>
          )}
        </div>

        <SheetFooter>
          <Button type="submit" form={FORM_ID} disabled={isPending}>
            {isPending ? PRODUCT_NUTRITION_COPY.ACTION_PENDING : PRODUCT_NUTRITION_COPY.ACTION_SUBMIT}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              {PRODUCT_DETAIL_SCREEN_COPY.BUTTON_CANCEL}
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
