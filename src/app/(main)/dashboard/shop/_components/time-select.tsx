"use client";

import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { HOUR_OPTIONS, MINUTE_OPTIONS } from "@/feature/shop/time";

interface TimeSelectProps {
  id: string;
  /** "HH:mm:ss" */
  value: string;
  disabled?: boolean;
  onChange: (value: string) => void;
}

/** 5분 단위 시/분 Select 조합. 옵션 자체가 5분 간격이라 단위 위반 값을 고를 수 없다. */
export function TimeSelect({ id, value, disabled, onChange }: TimeSelectProps) {
  const hour = value.slice(0, 2);
  const minute = value.slice(3, 5);

  return (
    <div className="flex items-center gap-1">
      <Select
        value={HOUR_OPTIONS.includes(hour) ? hour : ""}
        onValueChange={(next) => onChange(`${next}:${minute}:00`)}
        disabled={disabled}
      >
        <SelectTrigger id={`${id}-hour`} size="sm" className="w-[72px]">
          <SelectValue />
        </SelectTrigger>
        <SelectContent position="popper">
          <SelectGroup>
            {HOUR_OPTIONS.map((option) => (
              <SelectItem key={option} value={option}>
                {option}
              </SelectItem>
            ))}
          </SelectGroup>
        </SelectContent>
      </Select>
      <span className="text-muted-foreground text-sm">:</span>
      <Select
        value={MINUTE_OPTIONS.includes(minute) ? minute : ""}
        onValueChange={(next) => onChange(`${hour}:${next}:00`)}
        disabled={disabled}
      >
        <SelectTrigger id={`${id}-minute`} size="sm" className="w-[72px]">
          <SelectValue />
        </SelectTrigger>
        <SelectContent position="popper">
          <SelectGroup>
            {MINUTE_OPTIONS.map((option) => (
              <SelectItem key={option} value={option}>
                {option}
              </SelectItem>
            ))}
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>
  );
}
