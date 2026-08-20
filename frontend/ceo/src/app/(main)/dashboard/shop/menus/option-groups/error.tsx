"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { SHOP_ERROR_PAGE_COPY } from "@/feature/shop/message";

// Next.js 는 error 바운더리에 `reset` 이라는 이름으로 재시도 함수를 넘긴다.
// 다른 이름으로 받으면 `undefined` 가 되어 "다시 시도" 버튼이 눌러도 아무 일도 하지 않는다
// (이 라우트군에 `retry` 로 잘못 받은 전례가 있어 `availability/error.tsx` 와 같은 판단을 따른다).
export default function ProductOptionGroupError({
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
