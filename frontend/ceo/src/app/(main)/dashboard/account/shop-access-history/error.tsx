"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { CEO_ERROR_PAGE_COPY } from "@/feature/ceo/message";

export default function ShopAccessHistoryError({
  error,
  retry,
}: {
  error: Error & { digest?: string };
  retry: () => void;
}) {
  React.useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl leading-none">{CEO_ERROR_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription>{error.message}</CardDescription>
      </CardHeader>
      <CardContent>
        <Button onClick={() => retry()}>{CEO_ERROR_PAGE_COPY.RETRY}</Button>
      </CardContent>
    </Card>
  );
}
