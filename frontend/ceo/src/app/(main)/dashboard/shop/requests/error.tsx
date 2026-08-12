"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { SHOP_ERROR_PAGE_COPY } from "@/feature/shop/message";

export default function ShopRequestError({ error, retry }: { error: Error & { digest?: string }; retry: () => void }) {
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
        <Button onClick={() => retry()}>{SHOP_ERROR_PAGE_COPY.RETRY}</Button>
      </CardContent>
    </Card>
  );
}
