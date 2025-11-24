import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { deleteBeer, getBeer, type BeerDTO } from '../services/beers.service'

export default function BeerDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [beer, setBeer] = useState<BeerDTO | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const beerId = Number(id)
    if (!Number.isFinite(beerId)) return
    getBeer(beerId)
      .then(setBeer)
      .catch((e) => setError(e.message ?? 'Failed to load beer'))
  }, [id])

  if (error) return <div className="text-red-600">{error}</div>
  if (!beer) return <div>Loading…</div>

  return (
    <div className="space-y-2">
      <h1 className="text-2xl font-bold">{beer.beerName}</h1>
      <div className="flex gap-2 mb-2">
        <Link className="px-3 py-1 rounded border" to={`/beers/${beer.id}/edit`}>Edit</Link>
        <button
          className="px-3 py-1 rounded border border-red-600 text-red-600"
          onClick={async () => {
            if (!confirm('Delete this beer?')) return
            try {
              await deleteBeer(beer.id!)
              ;(window as any).toast?.('Beer deleted')
              navigate('/beers')
            } catch (e: any) {
              ;(window as any).toast?.(e.message ?? 'Delete failed')
            }
          }}
        >
          Delete
        </button>
      </div>
      <dl className="grid grid-cols-2 gap-2 max-w-lg">
        <dt className="text-muted-foreground">Style</dt><dd>{beer.beerStyle}</dd>
        <dt className="text-muted-foreground">Price</dt><dd>{beer.price}</dd>
        <dt className="text-muted-foreground">UPC</dt><dd>{beer.upc}</dd>
        <dt className="text-muted-foreground">Quantity</dt><dd>{beer.quantityOnHand}</dd>
      </dl>
      <Link to="/beers" className="underline">Back to list</Link>
    </div>
  )
}
