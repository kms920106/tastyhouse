"use client";

import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import { HYGIENE_BADGE_TYPE_LABEL } from "@/feature/shop/constants";
import type { HygieneBadge } from "@/feature/shop/domain";
import { SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { formatDateTime } from "@/lib/date";

interface HygieneInfoCardProps {
  badges: HygieneBadge[];
}

export function HygieneInfoCard({ badges }: HygieneInfoCardProps) {
  return (
    <div className="flex flex-col gap-2 py-4">
      <span className="font-medium text-sm">{SHOP_OPERATION_COPY.HYGIENE_TITLE}</span>
      <span className="text-muted-foreground text-xs leading-snug">{SHOP_OPERATION_COPY.HYGIENE_DESCRIPTION}</span>

      {badges.length > 0 ? (
        <ul className="mt-1 space-y-1">
          {badges.map((item) => (
            <li key={item.id} className="flex items-center gap-2 text-sm">
              <Badge variant="secondary">{HYGIENE_BADGE_TYPE_LABEL[item.badgeType]}</Badge>
              <span className="text-muted-foreground text-xs">
                인증일 {formatDateTime(item.certifiedDate)}
                {item.lastInspectionMonth && ` · 최근 점검 ${item.lastInspectionMonth}`}
              </span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="mt-1 text-muted-foreground text-sm">{SHOP_OPERATION_COPY.NOT_REGISTERED}</p>
      )}

      <Accordion type="single" collapsible className="mt-2">
        <AccordionItem value="apply-guide">
          <AccordionTrigger className="text-sm">위생 인증 신청 안내</AccordionTrigger>
          <AccordionContent>
            <p className="text-muted-foreground text-xs leading-relaxed">{SHOP_OPERATION_COPY.HYGIENE_APPLY_GUIDE}</p>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  );
}
