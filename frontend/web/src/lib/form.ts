import { z } from 'zod'

export function extractZodFieldErrors<T extends z.ZodType>(
  error: z.ZodError<z.infer<T>>,
): Partial<Record<string, string>> {
  const fieldErrors = z.flattenError(error).fieldErrors
  const result: Partial<Record<string, string>> = {}
  for (const key in fieldErrors) {
    result[key] = fieldErrors[key]?.[0]
  }
  return result
}
