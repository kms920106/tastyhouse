"use client";

import * as React from "react";

import { format } from "date-fns";
import type { DateRange } from "react-day-picker";
import { toast } from "sonner";

import { DateRangePicker } from "@/components/date-range-picker";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Field, FieldDescription, FieldLabel, FieldLegend, FieldSet } from "@/components/ui/field";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import { clearMenuExposureAction, loadMenuExposureAction, saveMenuExposureAction } from "@/feature/product/actions";
import {
  EXPOSURE_DAY_SELECTION_MODES,
  EXPOSURE_INDIVIDUAL_DAY_OPTIONS,
  EXPOSURE_PRESET_DAY_OPTIONS,
  EXPOSURE_PRESET_DAY_TYPES,
  EXPOSURE_TIME_OPTIONS,
} from "@/feature/product/constants";
import type {
  ExposureDaySelectionMode,
  MenuExposure,
  MenuExposureHour,
  ProductExposureDayType,
} from "@/feature/product/domain";
import {
  PRODUCT_DETAIL_COPY,
  PRODUCT_DETAIL_SCREEN_COPY,
  PRODUCT_MENU_MESSAGE,
  PRODUCT_MENU_VALIDATION_MESSAGE,
} from "@/feature/product/message";

const DATE_FORMAT = "yyyy-MM-dd";

/** 종일을 나타내는 Select 센티넬. Radix Select 는 빈 문자열을 항목 값으로 쓸 수 없다 */
const ALL_DAY_VALUE = "ALL_DAY";

interface MenuExposureSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  productId: number;
  shopId: number;
  /** 저장 후 상세 행의 요약을 갱신하기 위해 부모에 알린다 */
  onSaved: (exposure: MenuExposure | null) => void;
}

/** 요일별 시간대 편집 상태. 체크 해제한 요일의 시간까지 지우지 않으려 Map 으로 들고 있는다 */
type HourDraft = { startTime: string | null; endTime: string | null };

function parseLocalDate(value: string | null): Date | undefined {
  if (!value) return undefined;
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

export function MenuExposureSheet({ open, onOpenChange, productId, shopId, onSaved }: MenuExposureSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [isLoading, setIsLoading] = React.useState(false);
  const [exposure, setExposure] = React.useState<MenuExposure | null>(null);

  const [alwaysExposed, setAlwaysExposed] = React.useState(true);
  const [dateRange, setDateRange] = React.useState<DateRange>({ from: undefined, to: undefined });
  const [dayMode, setDayMode] = React.useState<ExposureDaySelectionMode>(EXPOSURE_DAY_SELECTION_MODES.PRESET);
  const [selectedDays, setSelectedDays] = React.useState<ProductExposureDayType[]>([]);
  const [hourDrafts, setHourDrafts] = React.useState<Record<string, HourDraft>>({});

  // 열릴 때마다 서버에서 현재 설정을 읽는다 — 상세 페이지가 미리 받아오면 Sheet 마다 페이로드가
  // 커지고, 다른 탭에서 바꾼 스케줄을 덮어쓸 위험도 생긴다.
  React.useEffect(() => {
    if (!open) return;

    let cancelled = false;
    setIsLoading(true);

    void loadMenuExposureAction(productId, shopId).then(({ success, message, data }) => {
      if (cancelled) return;
      setIsLoading(false);

      if (!success || !data) {
        toast.error(message ?? PRODUCT_MENU_MESSAGE.EXPOSURE_LOAD_FAILED);
        return;
      }

      setExposure(data);

      // 스케줄 행이 0건이고 기간도 없으면 "제약 없음" = 상시 노출이다(`backend.md` §6-1).
      const hasSchedule = data.hours.length > 0 || data.startDate !== null || data.endDate !== null;
      setAlwaysExposed(!hasSchedule);
      setDateRange({ from: parseLocalDate(data.startDate), to: parseLocalDate(data.endDate) });

      const hasPreset = data.hours.some((hour) => EXPOSURE_PRESET_DAY_TYPES.includes(hour.dayType));
      setDayMode(hasPreset ? EXPOSURE_DAY_SELECTION_MODES.PRESET : EXPOSURE_DAY_SELECTION_MODES.INDIVIDUAL);
      setSelectedDays(data.hours.map((hour) => hour.dayType));
      setHourDrafts(
        Object.fromEntries(
          data.hours.map((hour) => [hour.dayType, { startTime: hour.startTime, endTime: hour.endTime }]),
        ),
      );
    });

    return () => {
      cancelled = true;
    };
  }, [open, productId, shopId]);

  const dayOptions =
    dayMode === EXPOSURE_DAY_SELECTION_MODES.PRESET ? EXPOSURE_PRESET_DAY_OPTIONS : EXPOSURE_INDIVIDUAL_DAY_OPTIONS;

  // 방식을 바꾸면 선택을 비운다. 남겨 두면 묶음+개별이 함께 전송돼 서버가
  // `PRODUCT_EXPOSURE_DAY_TYPE_MIXED` 로 거절한다 — 애초에 섞일 수 없게 만드는 것이 목적이다.
  function handleDayModeChange(next: string) {
    setDayMode(next as ExposureDaySelectionMode);
    setSelectedDays([]);
  }

  function handleDayToggle(dayType: ProductExposureDayType, checked: boolean) {
    setSelectedDays((previous) => (checked ? [...previous, dayType] : previous.filter((value) => value !== dayType)));
  }

  function handleTimeChange(dayType: ProductExposureDayType, key: keyof HourDraft, value: string) {
    setHourDrafts((previous) => {
      // 기존 draft 를 먼저 펼치고 바꾸려는 한쪽만 덮어쓴다.
      // 리터럴 키(startTime/endTime)와 계산된 키(`[key]`)를 한 객체 리터럴에 함께 두면
      // 계산된 키가 리터럴을 덮어쓰는 형태가 되어 TS2783 이 난다 — 지금은 순서 덕에 맞게
      // 동작하지만 두 줄의 순서에 정확성이 걸려 있어 위험하다.
      const current: HourDraft = previous[dayType] ?? { startTime: null, endTime: null };

      return {
        ...previous,
        [dayType]: { ...current, [key]: value === ALL_DAY_VALUE ? null : value },
      };
    });
  }

  function buildHours(): MenuExposureHour[] {
    return selectedDays.map((dayType) => {
      const draft = hourDrafts[dayType];
      // 한쪽만 고른 상태는 서버가 해석할 수 없다(종일은 둘 다 null). 저장 전에 종일로 정규화한다.
      const complete = draft?.startTime != null && draft?.endTime != null;
      return {
        dayType,
        startTime: complete ? draft.startTime : null,
        endTime: complete ? draft.endTime : null,
      };
    });
  }

  function handleSave() {
    // 상시 노출은 스케줄 삭제와 같다 — 빈 배열을 PUT 하는 대신 전용 DELETE 를 쓴다.
    if (alwaysExposed) {
      startTransition(async () => {
        const { success, message } = await clearMenuExposureAction(productId, shopId);
        if (!success) {
          toast.error(message ?? PRODUCT_MENU_MESSAGE.EXPOSURE_CLEAR_FAILED);
          return;
        }
        toast.success(PRODUCT_MENU_MESSAGE.EXPOSURE_CLEAR_SUCCESS);
        onSaved(null);
        onOpenChange(false);
      });
      return;
    }

    // 기간만 지정하고 요일을 비우는 것도 유효하지만, 요일 체크박스를 하나도 안 고른 채
    // "요일 지정" 화면을 떠나면 의도와 결과가 어긋나므로 기간도 없을 때만 막는다.
    const hours = buildHours();
    const startDate = dateRange.from ? format(dateRange.from, DATE_FORMAT) : null;
    const endDate = dateRange.to ? format(dateRange.to, DATE_FORMAT) : null;

    if (hours.length === 0 && startDate === null && endDate === null) {
      toast.error(PRODUCT_MENU_VALIDATION_MESSAGE.EXPOSURE_DAY_REQUIRED);
      return;
    }

    startTransition(async () => {
      const { success, message } = await saveMenuExposureAction(productId, shopId, { startDate, endDate, hours });
      if (!success) {
        toast.error(message ?? PRODUCT_MENU_MESSAGE.EXPOSURE_SAVE_FAILED);
        return;
      }
      toast.success(PRODUCT_MENU_MESSAGE.EXPOSURE_SAVE_SUCCESS);
      onSaved({ startDate, endDate, hours, exposedNow: exposure?.exposedNow ?? true, hiddenReason: null });
      onOpenChange(false);
    });
  }

  const statusBadge = (() => {
    if (exposure === null) return null;
    if (exposure.exposedNow) {
      return <Badge variant="secondary">{PRODUCT_DETAIL_COPY.EXPOSURE_BADGE_EXPOSED}</Badge>;
    }
    if (exposure.hiddenReason === null) return null;
    return (
      <Badge variant="outline">{PRODUCT_DETAIL_SCREEN_COPY.EXPOSURE_HIDDEN_REASON_LABEL[exposure.hiddenReason]}</Badge>
    );
  })();

  const disabled = isPending || isLoading;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle className="flex items-center gap-2">
            {PRODUCT_DETAIL_COPY.SHEET_EXPOSURE_TITLE}
            {statusBadge}
          </SheetTitle>
          <SheetDescription>{PRODUCT_DETAIL_COPY.EXPOSURE_ALWAYS_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 overflow-y-auto px-4">
          {isLoading ? (
            <div className="flex flex-col gap-3">
              <Skeleton className="h-9 w-full" />
              <Skeleton className="h-9 w-full" />
              <Skeleton className="h-24 w-full" />
            </div>
          ) : (
            <div className="flex flex-col gap-6">
              <Field orientation="horizontal" className="gap-3">
                <div className="flex min-w-0 flex-1 flex-col gap-1">
                  <FieldLabel htmlFor="menu-exposure-always">{PRODUCT_DETAIL_COPY.EXPOSURE_ALWAYS_LABEL}</FieldLabel>
                  <FieldDescription>{PRODUCT_DETAIL_COPY.EXPOSURE_ALWAYS_DESCRIPTION}</FieldDescription>
                </div>
                <Switch
                  id="menu-exposure-always"
                  checked={alwaysExposed}
                  onCheckedChange={setAlwaysExposed}
                  disabled={disabled}
                />
              </Field>

              {/* 상시 노출이 켜져 있으면 이하 입력은 의미가 없다 — 숨기지 않고 비활성으로 남겨
                  어떤 값이 설정돼 있었는지 확인할 수 있게 한다. */}
              {!alwaysExposed && (
                <>
                  <Separator />

                  <Field className="gap-1.5">
                    <FieldLabel htmlFor="menu-exposure-period">{PRODUCT_DETAIL_COPY.EXPOSURE_PERIOD_LABEL}</FieldLabel>
                    <div className="flex flex-wrap items-center gap-2">
                      <DateRangePicker
                        value={dateRange}
                        onChange={(next) => setDateRange(next ?? { from: undefined, to: undefined })}
                        disabled={disabled}
                        placeholder={PRODUCT_DETAIL_SCREEN_COPY.EXPOSURE_PERIOD_PLACEHOLDER}
                      />
                      <Button
                        type="button"
                        size="sm"
                        variant="ghost"
                        disabled={disabled}
                        onClick={() => setDateRange({ from: undefined, to: undefined })}
                      >
                        {PRODUCT_DETAIL_SCREEN_COPY.EXPOSURE_PERIOD_CLEAR}
                      </Button>
                    </div>
                    <FieldDescription>{PRODUCT_DETAIL_COPY.EXPOSURE_PERIOD_HELP}</FieldDescription>
                  </Field>

                  <FieldSet className="gap-3">
                    <FieldLegend variant="label">{PRODUCT_DETAIL_COPY.EXPOSURE_DAY_MODE_LABEL}</FieldLegend>
                    {/* 묶음과 개별 요일은 함께 보낼 수 없다(`PRODUCT_EXPOSURE_DAY_TYPE_MIXED`).
                        라디오로 방식을 먼저 고르게 해 **혼용 자체를 구조적으로 불가능하게** 만든다 —
                        서버 에러 문구에 기대면 사용자는 무엇이 잘못됐는지 알 수 없다. */}
                    <RadioGroup
                      value={dayMode}
                      onValueChange={handleDayModeChange}
                      disabled={disabled}
                      className="flex flex-row gap-4"
                    >
                      <div className="flex items-center gap-2">
                        <RadioGroupItem
                          id="menu-exposure-day-mode-preset"
                          value={EXPOSURE_DAY_SELECTION_MODES.PRESET}
                        />
                        <FieldLabel htmlFor="menu-exposure-day-mode-preset" className="font-normal">
                          {PRODUCT_DETAIL_COPY.EXPOSURE_DAY_MODE_PRESET}
                        </FieldLabel>
                      </div>
                      <div className="flex items-center gap-2">
                        <RadioGroupItem
                          id="menu-exposure-day-mode-individual"
                          value={EXPOSURE_DAY_SELECTION_MODES.INDIVIDUAL}
                        />
                        <FieldLabel htmlFor="menu-exposure-day-mode-individual" className="font-normal">
                          {PRODUCT_DETAIL_COPY.EXPOSURE_DAY_MODE_INDIVIDUAL}
                        </FieldLabel>
                      </div>
                    </RadioGroup>
                    <FieldDescription>{PRODUCT_DETAIL_COPY.EXPOSURE_DAY_MODE_HELP}</FieldDescription>

                    <div className="flex flex-wrap gap-3">
                      {dayOptions.map((option) => (
                        <div key={option.value} className="flex items-center gap-2">
                          <Checkbox
                            id={`menu-exposure-day-${option.value}`}
                            checked={selectedDays.includes(option.value)}
                            onCheckedChange={(checked) => handleDayToggle(option.value, checked === true)}
                            disabled={disabled}
                          />
                          <FieldLabel htmlFor={`menu-exposure-day-${option.value}`} className="font-normal">
                            {option.label}
                          </FieldLabel>
                        </div>
                      ))}
                    </div>
                  </FieldSet>

                  {selectedDays.length > 0 && (
                    <FieldSet className="gap-3">
                      <FieldLegend variant="label">{PRODUCT_DETAIL_COPY.EXPOSURE_TIME_LABEL}</FieldLegend>
                      <FieldDescription>{PRODUCT_DETAIL_COPY.EXPOSURE_TIME_HELP}</FieldDescription>

                      {selectedDays.map((dayType) => {
                        const draft = hourDrafts[dayType];
                        const label =
                          [...EXPOSURE_PRESET_DAY_OPTIONS, ...EXPOSURE_INDIVIDUAL_DAY_OPTIONS].find(
                            (option) => option.value === dayType,
                          )?.label ?? dayType;
                        // 종료가 시작보다 빠른 것은 오류가 아니라 **자정 넘김**(22:00~02:00 야식)이다.
                        // 막으면 야식 메뉴를 설정할 수 없으므로 안내만 띄운다.
                        const overnight =
                          draft?.startTime != null && draft?.endTime != null && draft.endTime < draft.startTime;

                        return (
                          <div key={dayType} className="flex flex-col gap-1.5 border-b pb-3 last:border-b-0">
                            <div className="flex flex-wrap items-center gap-2">
                              <span className="w-12 shrink-0 text-sm">{label}</span>
                              <Select
                                value={draft?.startTime ?? ALL_DAY_VALUE}
                                onValueChange={(next) => handleTimeChange(dayType, "startTime", next)}
                                disabled={disabled}
                              >
                                <SelectTrigger
                                  id={`menu-exposure-start-${dayType}`}
                                  size="sm"
                                  className="w-[112px]"
                                  aria-label={PRODUCT_DETAIL_COPY.EXPOSURE_TIME_START}
                                >
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent position="popper">
                                  <SelectGroup>
                                    <SelectItem value={ALL_DAY_VALUE}>
                                      {PRODUCT_DETAIL_COPY.EXPOSURE_TIME_ALL_DAY}
                                    </SelectItem>
                                    {EXPOSURE_TIME_OPTIONS.map((time) => (
                                      <SelectItem key={time} value={time}>
                                        {time}
                                      </SelectItem>
                                    ))}
                                  </SelectGroup>
                                </SelectContent>
                              </Select>
                              <span className="text-muted-foreground text-sm">~</span>
                              <Select
                                value={draft?.endTime ?? ALL_DAY_VALUE}
                                onValueChange={(next) => handleTimeChange(dayType, "endTime", next)}
                                disabled={disabled}
                              >
                                <SelectTrigger
                                  id={`menu-exposure-end-${dayType}`}
                                  size="sm"
                                  className="w-[112px]"
                                  aria-label={PRODUCT_DETAIL_COPY.EXPOSURE_TIME_END}
                                >
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent position="popper">
                                  <SelectGroup>
                                    <SelectItem value={ALL_DAY_VALUE}>
                                      {PRODUCT_DETAIL_COPY.EXPOSURE_TIME_ALL_DAY}
                                    </SelectItem>
                                    {EXPOSURE_TIME_OPTIONS.map((time) => (
                                      <SelectItem key={time} value={time}>
                                        {time}
                                      </SelectItem>
                                    ))}
                                  </SelectGroup>
                                </SelectContent>
                              </Select>
                            </div>
                            {overnight && (
                              <span className="text-muted-foreground text-xs leading-snug">
                                {PRODUCT_DETAIL_COPY.EXPOSURE_OVERNIGHT_NOTICE}
                              </span>
                            )}
                          </div>
                        );
                      })}
                    </FieldSet>
                  )}
                </>
              )}
            </div>
          )}
        </div>

        <SheetFooter>
          <Button type="button" onClick={handleSave} disabled={disabled}>
            {isPending ? PRODUCT_DETAIL_SCREEN_COPY.BUTTON_SAVING : PRODUCT_DETAIL_SCREEN_COPY.BUTTON_SAVE}
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
