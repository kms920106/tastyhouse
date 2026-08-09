"use client";

import * as React from "react";

import { toast } from "sonner";

import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { searchAdminDongsAction } from "@/feature/shop/actions";
import { SEARCH_DEBOUNCE_MS } from "@/feature/shop/constants";
import type { AdminDong } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { cn } from "@/lib/utils";

interface DeliveryAreaDongSearchProps {
  selectedDongIds: Set<number>;
  lockedDongIds: Set<number>;
  /** 검색 결과의 이름을 함께 올린다 — 저장 전에도 목록에 실제 이름이 뜨도록 */
  onToggleDong: (dongId: number, regionName: string) => void;
  /** 검색 결과를 고르면 지도를 그쪽으로 옮기기 위해 알린다 */
  onFocusDong?: (dongId: number) => void;
  disabled?: boolean;
}

/**
 * 행정동 키워드 검색.
 *
 * 기존 `delivery-area-sheet.tsx` 의 300ms 디바운스 로직을 그대로 이식했다. 검색어가 비면
 * 후보를 비우고 요청도 보내지 않는다 — 빈 검색으로 전국 목록을 받아 오지 않기 위해서다.
 *
 * 지도를 못 쓰는 환경(키 미설정·SDK 실패·키보드 사용자)에서 이 검색과 트리가 완전한
 * 대체 경로가 되어야 하므로, 지도 상태와 무관하게 항상 렌더된다.
 */
export function DeliveryAreaDongSearch({
  selectedDongIds,
  lockedDongIds,
  onToggleDong,
  onFocusDong,
  disabled = false,
}: DeliveryAreaDongSearchProps) {
  const [keyword, setKeyword] = React.useState("");
  const [candidates, setCandidates] = React.useState<AdminDong[]>([]);
  const [isSearching, setIsSearching] = React.useState(false);

  React.useEffect(() => {
    const trimmed = keyword.trim();
    if (!trimmed) {
      setCandidates([]);
      setIsSearching(false);
      return;
    }

    let active = true;
    setIsSearching(true);
    const timer = setTimeout(() => {
      void searchAdminDongsAction(trimmed).then(({ success, message, data }) => {
        // 입력이 이어져 이 요청이 낡았으면 결과를 버린다.
        if (!active) return;
        if (success) {
          setCandidates(data ?? []);
        } else {
          setCandidates([]);
          toast.error(message ?? SHOP_MESSAGE.ADMIN_DONG_SEARCH_FAILED);
        }
        setIsSearching(false);
      });
    }, SEARCH_DEBOUNCE_MS);

    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [keyword]);

  const hasKeyword = keyword.trim().length > 0;

  return (
    <div className="flex flex-col gap-3">
      <Input
        value={keyword}
        onChange={(event) => setKeyword(event.target.value)}
        placeholder={SHOP_OPERATION_COPY.DELIVERY_AREA_SEARCH_PLACEHOLDER}
        disabled={disabled}
        aria-label={SHOP_OPERATION_COPY.DELIVERY_AREA_SEARCH_LABEL}
      />

      {hasKeyword &&
        (isSearching ? (
          <div className="flex items-center gap-2 py-4 text-muted-foreground text-sm">
            <Spinner className="size-4" />
            검색 중입니다.
          </div>
        ) : candidates.length > 0 ? (
          <ul className="flex flex-col">
            {candidates.map((candidate) => {
              const isSelected = selectedDongIds.has(candidate.id);
              const isLocked = lockedDongIds.has(candidate.id);

              return (
                <li key={candidate.id} className="flex items-center gap-2 border-b py-2 last:border-b-0">
                  <Checkbox
                    id={`search-dong-${candidate.id}`}
                    checked={isSelected}
                    // 배달팁이 걸린 동은 해제만 막는다.
                    disabled={disabled ? true : isLocked && isSelected}
                    onCheckedChange={() => onToggleDong(candidate.id, candidate.regionName)}
                  />
                  <label
                    htmlFor={`search-dong-${candidate.id}`}
                    className={cn("min-w-0 flex-1 truncate text-sm", isLocked && "text-muted-foreground")}
                  >
                    {candidate.regionName}
                  </label>
                  {onFocusDong && (
                    <button
                      type="button"
                      className="shrink-0 text-muted-foreground text-xs underline-offset-2 hover:underline"
                      onClick={() => onFocusDong(candidate.id)}
                      disabled={disabled}
                    >
                      지도에서 보기
                    </button>
                  )}
                </li>
              );
            })}
          </ul>
        ) : (
          <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
            {SHOP_OPERATION_COPY.DELIVERY_AREA_SEARCH_EMPTY}
          </p>
        ))}
    </div>
  );
}
