"use client";

import * as React from "react";

import { ChevronDown, ChevronRight } from "lucide-react";
import { toast } from "sonner";

import { Checkbox } from "@/components/ui/checkbox";
import { Spinner } from "@/components/ui/spinner";
import { fetchAdminDongTreeAction } from "@/feature/shop/actions";
import type { AdminDongTreeNode } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import { cn } from "@/lib/utils";

/**
 * 시도 → 시군구 → 행정동 3단 lazy 트리.
 *
 * 전 계층을 한 번에 받으면 3,600행이 넘으므로 펼칠 때마다 그 단계만 조회한다.
 * `regionName` 을 공백으로 잘라 클라이언트가 계층을 조립하는 우회는 쓰지 않는다 —
 * 동명에 공백이 들어가면 깨지고, `region.dto.ts` 가 "프론트가 조립하지 않는다"고 못 박은 규칙의 역방향이다.
 *
 * 체크 = 추가, 해제 = 삭제다. "추가 모드 / 삭제 모드" 토글은 두지 않는다 — 자명한 조작에
 * 모드를 얹으면 인지 부하만 는다.
 */

/** 부모 노드의 체크 상태 — 하위 전부/일부/없음 */
type TriState = boolean | "indeterminate";

/**
 * 선택 목록에 쓸 전체 이름.
 *
 * 트리 노드는 동 이름 단독("역삼1동")이지만 검색·반경·서버 목록은 전체 이름("서울 강남구 역삼1동")을
 * 준다. 같은 목록에 섞여 표시되므로 트리 쪽을 맞춰 준다. 위 주석의 "조립하지 않는다"는
 * `regionName` 을 **쪼개** 계층을 만드는 역방향 금지이고, 트리가 이미 계층으로 들고 있는
 * 상위 이름을 앞에 붙이는 것은 그 규칙과 무관하다.
 */
function fullRegionName(sidoName: string, sigunguName: string, dongName: string): string {
  return `${sidoName} ${sigunguName} ${dongName}`;
}

interface DeliveryAreaDongTreeProps {
  /** 선택된 행정동 ID */
  selectedDongIds: Set<number>;
  /** 배달팁이 걸려 해제할 수 없는 행정동 ID */
  lockedDongIds: Set<number>;
  /** 트리가 아는 이름을 함께 올린다 — 저장 전에도 목록에 실제 이름이 뜨도록 */
  onToggleDong: (dongId: number, regionName: string) => void;
  /** 시군구 전체 토글 — 하위 행정동을 ID·이름 쌍으로 한 번에 넘긴다 */
  onToggleMany: (dongs: { adminDongId: number; regionName: string }[], selected: boolean) => void;
  disabled?: boolean;
}

interface SigunguState {
  /** 펼쳐진 시군구의 행정동 목록. 아직 안 받았으면 undefined */
  dongs?: AdminDongTreeNode[];
  isLoading: boolean;
}

export function DeliveryAreaDongTree({
  selectedDongIds,
  lockedDongIds,
  onToggleDong,
  onToggleMany,
  disabled = false,
}: DeliveryAreaDongTreeProps) {
  const [sidos, setSidos] = React.useState<AdminDongTreeNode[]>([]);
  const [isLoadingSidos, setIsLoadingSidos] = React.useState(true);

  const [openSido, setOpenSido] = React.useState<string | null>(null);
  const [sigungus, setSigungus] = React.useState<Map<string, AdminDongTreeNode[]>>(() => new Map());
  const [loadingSido, setLoadingSido] = React.useState<string | null>(null);

  const [openSigungu, setOpenSigungu] = React.useState<string | null>(null);
  const [dongState, setDongState] = React.useState<Map<string, SigunguState>>(() => new Map());

  // 시도 목록은 화면에 들어오면 한 번만 받는다.
  React.useEffect(() => {
    void fetchAdminDongTreeAction().then(({ success, message, data }) => {
      setIsLoadingSidos(false);
      if (!success || !data) {
        toast.error(message ?? SHOP_MESSAGE.DELIVERY_AREA_TREE_LOAD_FAILED);
        return;
      }
      setSidos(data.items);
    });
  }, []);

  const toggleSido = React.useCallback(
    (sidoName: string) => {
      if (openSido === sidoName) {
        setOpenSido(null);
        return;
      }

      setOpenSido(sidoName);
      setOpenSigungu(null);
      // 한 번 받은 시군구 목록은 다시 받지 않는다.
      if (sigungus.has(sidoName)) return;

      setLoadingSido(sidoName);
      void fetchAdminDongTreeAction(sidoName).then(({ success, message, data }) => {
        setLoadingSido(null);
        if (!success || !data) {
          toast.error(message ?? SHOP_MESSAGE.DELIVERY_AREA_TREE_LOAD_FAILED);
          return;
        }
        setSigungus((previous) => new Map(previous).set(sidoName, data.items));
      });
    },
    [openSido, sigungus],
  );

  const toggleSigungu = React.useCallback(
    (sidoName: string, sigunguName: string) => {
      const key = `${sidoName}/${sigunguName}`;
      if (openSigungu === key) {
        setOpenSigungu(null);
        return;
      }

      setOpenSigungu(key);
      if (dongState.get(key)?.dongs) return;

      setDongState((previous) => new Map(previous).set(key, { isLoading: true }));
      void fetchAdminDongTreeAction(sidoName, sigunguName).then(({ success, message, data }) => {
        if (!success || !data) {
          setDongState((previous) => new Map(previous).set(key, { isLoading: false }));
          toast.error(message ?? SHOP_MESSAGE.DELIVERY_AREA_TREE_LOAD_FAILED);
          return;
        }
        setDongState((previous) => new Map(previous).set(key, { dongs: data.items, isLoading: false }));
      });
    },
    [openSigungu, dongState],
  );

  /** 받아 둔 하위 행정동 기준으로만 판정한다 — 아직 안 받은 시군구는 판정하지 않는다 */
  function sigunguCheckState(key: string): TriState {
    const dongs = dongState.get(key)?.dongs;
    if (!dongs || dongs.length === 0) return false;

    const ids = dongs.map((dong) => dong.adminDongId).filter((id): id is number => id !== null);
    const selectedCount = ids.filter((id) => selectedDongIds.has(id)).length;
    if (selectedCount === 0) return false;
    return selectedCount === ids.length ? true : "indeterminate";
  }

  if (isLoadingSidos) {
    return (
      <div className="flex items-center justify-center gap-2 py-8 text-muted-foreground text-sm">
        <Spinner className="size-4" />
        지역을 불러오는 중입니다.
      </div>
    );
  }

  return (
    <ul className="flex flex-col" aria-label="행정동 선택">
      {sidos.map((sido) => {
        const isOpen = openSido === sido.name;
        const sigunguItems = sigungus.get(sido.name);

        return (
          <li key={sido.name} className="border-b last:border-b-0">
            <button
              type="button"
              className="flex w-full items-center gap-2 py-2 text-left text-sm hover:bg-accent/50"
              onClick={() => toggleSido(sido.name)}
              disabled={disabled}
              aria-expanded={isOpen}
            >
              {isOpen ? <ChevronDown className="size-4 shrink-0" /> : <ChevronRight className="size-4 shrink-0" />}
              <span className="flex-1 truncate">{sido.name}</span>
              <span className="shrink-0 text-muted-foreground text-xs">{sido.dongCount}</span>
            </button>

            {isOpen && (
              <ul className="pl-5">
                {loadingSido === sido.name && (
                  <li className="flex items-center gap-2 py-2 text-muted-foreground text-sm">
                    <Spinner className="size-4" />
                    불러오는 중
                  </li>
                )}

                {sigunguItems?.map((sigungu) => {
                  const key = `${sido.name}/${sigungu.name}`;
                  const isSigunguOpen = openSigungu === key;
                  const state = dongState.get(key);
                  const checkState = sigunguCheckState(key);

                  return (
                    <li key={key} className="border-t">
                      <div className="flex items-center gap-2 py-2">
                        <Checkbox
                          id={`sigungu-${key}`}
                          checked={checkState}
                          // 하위 목록을 아직 안 받았으면 전체 토글의 대상이 없다.
                          disabled={disabled ? true : !state?.dongs}
                          onCheckedChange={(checked) => {
                            const targets = (state?.dongs ?? [])
                              .flatMap((dong) =>
                                dong.adminDongId === null
                                  ? []
                                  : [
                                      {
                                        adminDongId: dong.adminDongId,
                                        regionName: fullRegionName(sido.name, sigungu.name, dong.name),
                                      },
                                    ],
                              )
                              // 배달팁이 걸린 동은 전체 해제에서 제외해 잠금을 우회하지 못하게 한다.
                              .filter((dong) => checked === true || !lockedDongIds.has(dong.adminDongId));
                            onToggleMany(targets, checked === true);
                          }}
                          aria-label={`${sigungu.name} 전체 선택`}
                        />
                        <button
                          type="button"
                          className="flex flex-1 items-center gap-2 text-left text-sm hover:underline"
                          onClick={() => toggleSigungu(sido.name, sigungu.name)}
                          disabled={disabled}
                          aria-expanded={isSigunguOpen}
                        >
                          {isSigunguOpen ? (
                            <ChevronDown className="size-4 shrink-0" />
                          ) : (
                            <ChevronRight className="size-4 shrink-0" />
                          )}
                          <span className="flex-1 truncate">{sigungu.name}</span>
                          <span className="shrink-0 text-muted-foreground text-xs">{sigungu.dongCount}</span>
                        </button>
                      </div>

                      {isSigunguOpen && (
                        <ul className="pl-6">
                          {state?.isLoading && (
                            <li className="flex items-center gap-2 py-2 text-muted-foreground text-sm">
                              <Spinner className="size-4" />
                              불러오는 중
                            </li>
                          )}

                          {state?.dongs?.map((dong) => {
                            if (dong.adminDongId === null) return null;
                            const dongId = dong.adminDongId;
                            const isLocked = lockedDongIds.has(dongId);
                            const isSelected = selectedDongIds.has(dongId);

                            return (
                              <li key={dongId} className="flex items-center gap-2 border-t py-2">
                                <Checkbox
                                  id={`dong-${dongId}`}
                                  checked={isSelected}
                                  // 배달팁이 걸린 동은 해제만 막는다. 선택은 이미 되어 있다.
                                  disabled={disabled ? true : isLocked && isSelected}
                                  onCheckedChange={() =>
                                    onToggleDong(dongId, fullRegionName(sido.name, sigungu.name, dong.name))
                                  }
                                />
                                <label
                                  htmlFor={`dong-${dongId}`}
                                  className={cn("flex-1 truncate text-sm", isLocked && "text-muted-foreground")}
                                >
                                  {dong.name}
                                </label>
                                {isLocked && (
                                  <span className="shrink-0 rounded border px-1.5 py-0.5 text-[10px] text-muted-foreground">
                                    배달팁
                                  </span>
                                )}
                              </li>
                            );
                          })}
                        </ul>
                      )}
                    </li>
                  );
                })}
              </ul>
            )}
          </li>
        );
      })}
    </ul>
  );
}
