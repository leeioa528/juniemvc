import { useCallback, useEffect, useMemo, useState } from 'react'
import { listBeers, type BeerDTO, type ListBeersParams } from '../services/beers.service'
import type { NormalizedApiError } from '../lib/axios'

export type UseBeersResult = {
  data: { content: BeerDTO[]; totalPages: number } | null
  loading: boolean
  error: NormalizedApiError | null
  params: ListBeersParams
  setParams: (updater: (prev: ListBeersParams) => ListBeersParams) => void
  refetch: () => void
}

export function useBeers(initial: ListBeersParams): UseBeersResult {
  const [params, setParamsState] = useState<ListBeersParams>(initial)
  const [data, setData] = useState<{ content: BeerDTO[]; totalPages: number } | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<NormalizedApiError | null>(null)

  const fetchBeers = useCallback(async (p: ListBeersParams) => {
    setLoading(true)
    try {
      const res = await listBeers(p)
      setData({ content: res.content, totalPages: res.totalPages })
      setError(null)
    } catch (e: any) {
      setError(e)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchBeers(params)
  }, [params, fetchBeers])

  const setParams = useCallback((updater: (prev: ListBeersParams) => ListBeersParams) => {
    setParamsState((prev) => updater(prev))
  }, [])

  const refetch = useCallback(() => fetchBeers(params), [fetchBeers, params])

  return useMemo(
    () => ({ data, loading, error, params, setParams, refetch }),
    [data, loading, error, params, setParams, refetch],
  )
}
