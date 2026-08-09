"use client";

import { Eraser, Hand, Minus, Paintbrush, Plus, Redo2, Undo2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import {
  BRUSH_SIZE_LABEL,
  BRUSH_SIZE_OPTIONS,
  type BrushSizeOption,
  DELIVERY_AREA_MODE_LABEL,
  DELIVERY_AREA_MODE_OPTIONS,
  type DeliveryAreaMode,
} from "@/feature/shop/constants";

const MODE_ICON: Record<DeliveryAreaMode, typeof Hand> = {
  PAN: Hand,
  PAINT: Paintbrush,
  ERASE: Eraser,
};

interface DeliveryAreaToolbarProps {
  mode: DeliveryAreaMode;
  onModeChange: (mode: DeliveryAreaMode) => void;
  brushSize: BrushSizeOption;
  onBrushSizeChange: (size: BrushSizeOption) => void;
  onUndo: () => void;
  onRedo: () => void;
  canUndo: boolean;
  canRedo: boolean;
  onZoomIn: () => void;
  onZoomOut: () => void;
  /** 지도를 못 쓰는 상태면 그리기 관련 조작을 모두 잠근다 */
  disabled?: boolean;
}

/**
 * 편집 툴바 — 모드 토글 · 브러시 크기 · 되돌리기 · 줌.
 *
 * 그리기 모드에서는 지도 휠 확대가 잠기므로 줌 버튼을 상시 제공한다. 이것이 없으면
 * 그리다가 확대하려면 매번 이동 모드로 돌아가야 한다.
 */
export function DeliveryAreaToolbar({
  mode,
  onModeChange,
  brushSize,
  onBrushSizeChange,
  onUndo,
  onRedo,
  canUndo,
  canRedo,
  onZoomIn,
  onZoomOut,
  disabled = false,
}: DeliveryAreaToolbarProps) {
  const isDrawingMode = mode !== "PAN";

  return (
    <div className="flex flex-wrap items-center gap-2 rounded-md border bg-background/95 p-2 shadow-sm">
      <ToggleGroup
        type="single"
        value={mode}
        onValueChange={(value) => {
          // ToggleGroup 은 같은 값을 다시 누르면 빈 문자열을 준다 — 모드를 비우지 않는다.
          if (value) onModeChange(value as DeliveryAreaMode);
        }}
        disabled={disabled}
        aria-label="편집 모드"
      >
        {DELIVERY_AREA_MODE_OPTIONS.map((option) => {
          const Icon = MODE_ICON[option];
          return (
            <ToggleGroupItem key={option} value={option} aria-label={DELIVERY_AREA_MODE_LABEL[option]}>
              <Icon className="size-4" />
              <span className="hidden sm:inline">{DELIVERY_AREA_MODE_LABEL[option]}</span>
            </ToggleGroupItem>
          );
        })}
      </ToggleGroup>

      <Separator orientation="vertical" className="h-6" />

      <ToggleGroup
        type="single"
        value={brushSize}
        onValueChange={(value) => {
          if (value) onBrushSizeChange(value as BrushSizeOption);
        }}
        // 이동 모드에서는 브러시 크기를 바꿀 이유가 없다.
        disabled={disabled ? true : !isDrawingMode}
        aria-label="브러시 크기"
      >
        {BRUSH_SIZE_OPTIONS.map((option) => (
          <ToggleGroupItem key={option} value={option} aria-label={`브러시 ${BRUSH_SIZE_LABEL[option]}`}>
            {BRUSH_SIZE_LABEL[option]}
          </ToggleGroupItem>
        ))}
      </ToggleGroup>

      <Separator orientation="vertical" className="h-6" />

      <div className="flex items-center gap-1">
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              type="button"
              size="icon"
              variant="ghost"
              onClick={onUndo}
              disabled={!canUndo}
              aria-label="되돌리기"
            >
              <Undo2 className="size-4" />
            </Button>
          </TooltipTrigger>
          <TooltipContent>되돌리기</TooltipContent>
        </Tooltip>

        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              type="button"
              size="icon"
              variant="ghost"
              onClick={onRedo}
              disabled={!canRedo}
              aria-label="다시 실행"
            >
              <Redo2 className="size-4" />
            </Button>
          </TooltipTrigger>
          <TooltipContent>다시 실행</TooltipContent>
        </Tooltip>
      </div>

      <Separator orientation="vertical" className="h-6" />

      <div className="flex items-center gap-1">
        <Button type="button" size="icon" variant="ghost" onClick={onZoomIn} disabled={disabled} aria-label="지도 확대">
          <Plus className="size-4" />
        </Button>
        <Button
          type="button"
          size="icon"
          variant="ghost"
          onClick={onZoomOut}
          disabled={disabled}
          aria-label="지도 축소"
        >
          <Minus className="size-4" />
        </Button>
      </div>
    </div>
  );
}
