"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export default function PartnershipRequestsError({
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
        <CardTitle className="text-xl leading-none">문제가 발생했습니다</CardTitle>
        <CardDescription>{error.message}</CardDescription>
      </CardHeader>
      <CardContent>
        <Button onClick={() => retry()}>다시 시도</Button>
      </CardContent>
    </Card>
  );
}
