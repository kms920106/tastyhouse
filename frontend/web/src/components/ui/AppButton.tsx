'use client'

import { Button, buttonVariants } from '@/components/ui/shadcn/button'
import { cn } from '@/lib/utils'
import { type VariantProps } from 'class-variance-authority'
import * as React from 'react'

export interface Props
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

export default function AppButton({
  className,
  variant = 'outline',
  size,
  asChild,
  ...props
}: Props) {
  // m-0 CSS 적용 금지

  return (
    <Button
      className={cn('p-0 rounded-none shadow-none cursor-pointer disabled:opacity-100', className)}
      variant={variant}
      size={size}
      asChild={asChild}
      {...props}
    />
  )
}
