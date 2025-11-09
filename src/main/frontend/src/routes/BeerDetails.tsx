import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getBeer, type BeerDTO } from '../services/beers.service'

export default function BeerDetails() {
  const { id } = useParams()
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
