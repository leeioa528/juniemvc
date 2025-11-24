import { useCallback, useEffect, useMemo, useState } from 'react'
import { listOrders, type BeerOrderDTO, type ListOrdersParams } from '../services/orders.service'
import type { NormalizedApiError } from '../lib/axios'

export type UseOrdersResult = {
  data: { content: BeerOrderDTO[]; totalPages: number } | null
  loading: boolean
  error: NormalizedApiError | null
  params: ListOrdersParams
  setParams: (updater: (prev: ListOrdersParams) => ListOrdersParams) => void
  refetch: () => void
}

export function useOrders(initial: ListOrdersParams): UseOrdersResult {
  const [params, setParamsState] = useState<ListOrdersParams>(initial)
  const [data, setData] = useState<{ content: BeerOrderDTO[]; totalPages: number } | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<NormalizedApiError | null>(null)

  const fetchOrders = useCallback(async (p: ListOrdersParams) => {
    setLoading(true)
    try {
      const res = await listOrders(p)
      setData({ content: res.content, totalPages: res.totalPages })
      setError(null)
    } catch (e: any) {
      setError(e)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchOrders(params)
  }, [params, fetchOrders])

  const setParams = useCallback((updater: (prev: ListOrdersParams) => ListOrdersParams) => {
    setParamsState((prev) => updater(prev))
  }, [])

  const refetch = useCallback(() => fetchOrders(params), [fetchOrders, params])

  return useMemo(
    () => ({ data, loading, error, params, setParams, refetch }),
    [data, loading, error, params, setParams, refetch],
  )
}
