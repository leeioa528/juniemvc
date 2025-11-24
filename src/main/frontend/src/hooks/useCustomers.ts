import { useCallback, useEffect, useMemo, useState } from 'react'
import { listCustomers, type CustomerDTO, type ListCustomersParams } from '../services/customers.service'
import type { NormalizedApiError } from '../lib/axios'

export type UseCustomersResult = {
  data: { content: CustomerDTO[]; totalPages: number } | null
  loading: boolean
  error: NormalizedApiError | null
  params: ListCustomersParams
  setParams: (updater: (prev: ListCustomersParams) => ListCustomersParams) => void
  refetch: () => void
}

export function useCustomers(initial: ListCustomersParams): UseCustomersResult {
  const [params, setParamsState] = useState<ListCustomersParams>(initial)
  const [data, setData] = useState<{ content: CustomerDTO[]; totalPages: number } | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<NormalizedApiError | null>(null)

  const fetchCustomers = useCallback(async (p: ListCustomersParams) => {
    setLoading(true)
    try {
      const res = await listCustomers(p)
      setData({ content: res.content, totalPages: res.totalPages })
      setError(null)
    } catch (e: any) {
      setError(e)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchCustomers(params)
  }, [params, fetchCustomers])

  const setParams = useCallback((updater: (prev: ListCustomersParams) => ListCustomersParams) => {
    setParamsState((prev) => updater(prev))
  }, [])

  const refetch = useCallback(() => fetchCustomers(params), [fetchCustomers, params])

  return useMemo(
    () => ({ data, loading, error, params, setParams, refetch }),
    [data, loading, error, params, setParams, refetch],
  )
}
