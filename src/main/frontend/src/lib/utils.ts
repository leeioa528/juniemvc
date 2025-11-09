import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

/**
 * Merge conditional classNames with Tailwind conflict resolution.
 */
export function cn(...inputs: ClassValue[]) {
  // @ts-expect-error clsx types are compatible with tailwind-merge
  return twMerge(clsx(inputs))
}
