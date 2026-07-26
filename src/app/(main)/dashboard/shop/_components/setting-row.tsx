"use client";

import type * as React from "react";

import { Button } from "@/components/ui/button";
import { SHOP_BASIC_COPY } from "@/feature/shop/message";

interface SettingRowProps {
  title: string;
  description?: string;
  /** 현재 설정값 요약 — 문자열이면 그대로, 노드면 Badge 등 커스텀 표시 */
  summary?: React.ReactNode;
  actionLabel?: string;
  onAction: () => void;
}

export function SettingRow({ title, description, summary, actionLabel, onAction }: SettingRowProps) {
  return (
    <div className="flex items-start justify-between gap-4 border-b py-4 last:border-b-0">
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <span className="font-medium text-sm">{title}</span>
        {description && <span className="text-muted-foreground text-xs leading-snug">{description}</span>}
        <div className="mt-1 min-w-0 text-sm">
          {summary ?? <span className="text-muted-foreground">{SHOP_BASIC_COPY.NOT_REGISTERED}</span>}
        </div>
      </div>
      <Button type="button" size="sm" variant="outline" onClick={onAction}>
        {actionLabel ?? SHOP_BASIC_COPY.CHANGE}
      </Button>
    </div>
  );
}
