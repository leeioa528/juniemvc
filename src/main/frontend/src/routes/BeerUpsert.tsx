import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import BeerForm from '../components/BeerForm'
import { createBeer, getBeer, updateBeer, type BeerDTO } from '../services/beers.service'

export default function BeerUpsert() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [initial, setInitial] = useState<Partial<BeerDTO> | undefined>(undefined)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!isEdit) return
    const beerId = Number(id)
    if (!Number.isFinite(beerId)) return
    setLoading(true)
    getBeer(beerId)
      .then((b) => setInitial(b))
      .catch((e) => setError(e.message ?? 'Failed to load beer'))
      .finally(() => setLoading(false))
  }, [id, isEdit])

  async function handleSubmit(values: BeerDTO) {
    try {
      setSaving(true)
      if (isEdit) {
        await updateBeer(Number(id), values)
        ;(window as any).toast?.('Beer updated')
        navigate(`/beers/${id}`)
      } else {
        const created = await createBeer(values)
        ;(window as any).toast?.('Beer created')
        navigate(`/beers/${created.id}`)
      }
    } catch (e: any) {
      setError(e.message ?? 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div>Loading…</div>
  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{isEdit ? 'Edit Beer' : 'New Beer'}</h1>
      {error && <div className="text-red-600">{error}</div>}
      <BeerForm initial={initial} submitting={saving} onSubmit={handleSubmit} />
    </div>
  )
}
