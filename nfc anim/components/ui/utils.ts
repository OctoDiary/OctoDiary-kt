import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

// Simple LRU cache for frequently used class combinations
const CLASS_CACHE_SIZE = 100;
const classCache = new Map<string, string>();

/**
 * Utility function to merge Tailwind CSS classes with proper conflict resolution.
 * Combines clsx for conditional classes and tailwind-merge for deduplication.
 * Includes LRU caching for performance optimization.
 * 
 * @param inputs - Class values to merge (strings, objects, arrays)
 * @returns Merged and deduplicated class string
 */
export function cn(...inputs: ClassValue[]): string {
  // Create cache key from inputs
  const cacheKey = JSON.stringify(inputs);
  
  // Check cache first
  if (classCache.has(cacheKey)) {
    return classCache.get(cacheKey)!;
  }
  
  // Process classes
  const result = twMerge(clsx(inputs));
  
  // Implement simple LRU cache
  if (classCache.size >= CLASS_CACHE_SIZE) {
    const firstKey = classCache.keys().next().value;
    classCache.delete(firstKey);
  }
  classCache.set(cacheKey, result);
  
  return result;
}

/**
 * Alternative function name for better readability
 * @deprecated Use cn() instead - kept for backward compatibility
 */
export function mergeClassNames(...inputs: ClassValue[]): string {
  return cn(...inputs);
}
