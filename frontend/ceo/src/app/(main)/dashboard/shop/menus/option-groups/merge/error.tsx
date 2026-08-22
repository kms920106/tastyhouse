"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { SHOP_ERROR_PAGE_COPY } from "@/feature/shop/message";

// Next.js 는 error 바운더리에 `reset` 이라는 이름으로 재시도 함수를 넘긴다 — 다른 이름으로 받으면
// `undefined` 가 되어 "다시 시도" 버튼이 아무 일도 하지 않는다(`option-groups/error.tsx` 와 동일).
export default function OptionGroupMergeError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  React.useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl leading-none">{SHOP_ERROR_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription>{error.message}</CardDescription>
      </CardHeader>
      <CardContent>
        <Button onClick={() => reset()}>{SHOP_ERROR_PAGE_COPY.RETRY}</Button>
      </CardContent>
    </Card>
  );
}
