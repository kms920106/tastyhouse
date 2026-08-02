import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatPhoneNumber(phone: string): string {
  const digits = phone.replace(/\D/g, '')

  if (digits.length === 11) {
    return digits.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3')
  }

  if (digits.length === 10) {
    if (digits.startsWith('02')) {
      return digits.replace(/(\d{2})(\d{4})(\d{4})/, '$1-$2-$3')
    }
    return digits.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3')
  }

  if (digits.length === 9 && digits.startsWith('02')) {
    return digits.replace(/(\d{2})(\d{3})(\d{4})/, '$1-$2-$3')
  }

  return phone
}
