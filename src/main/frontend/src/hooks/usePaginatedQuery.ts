import { useEffect, useState } from 'react'

export type Page<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export function usePaginatedQuery<TParams extends { page: number; size: number }, TItem>(
  params: TParams,
  fetcher: (params: TParams) => Promise<Page<TItem>>,
) {
  const [data, setData] = useState<Page<TItem>>({ content: [], number: params.page, size: params.size, totalElements: 0, totalPages: 0 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    fetcher(params)
      .then((res) => {
        if (cancelled) return
        setData(res)
        setError(null)
      })
      .catch((e) => {
        if (cancelled) return
        setError(e?.message ?? 'Request failed')
      })
      .finally(() => {
        if (cancelled) return
        setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [params.page, params.size, JSON.stringify(params)])

  return { data, loading, error }
}
