"use client";

import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { ShopSummary } from "@/feature/shop/domain";

interface ShopSelectorProps {
  shops: ShopSummary[];
  shopId: number;
  disabled?: boolean;
  onChange: (shopId: number) => void;
}

export function ShopSelector({ shops, shopId, disabled, onChange }: ShopSelectorProps) {
  // 가게가 1개면 선택할 것이 없으므로 렌더하지 않는다.
  if (shops.length < 2) return null;

  return (
    <Select value={String(shopId)} onValueChange={(value) => onChange(Number(value))} disabled={disabled}>
      <SelectTrigger className="w-full md:w-64">
        <SelectValue />
      </SelectTrigger>
      <SelectContent position="popper" align="start">
        <SelectGroup>
          {shops.map((shop) => (
            <SelectItem key={shop.id} value={String(shop.id)}>
              {shop.name}
            </SelectItem>
          ))}
        </SelectGroup>
      </SelectContent>
    </Select>
  );
}
