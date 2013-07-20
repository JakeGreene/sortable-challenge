package ca.jakegreene.sortable

/**
 * A MatchingStrategy will search through a collection of
 * Listings and select any which may involve a particular
 * Product
 */
trait MatchingStrategy {
	protected def findMatches(product: Product, listings: List[Listing]): List[Listing]
}

/**
 * A quick and dirty heuristic-based matching strategy. 
 */
trait ExplicitMatchingStrategy extends MatchingStrategy {
    override def findMatches(product: Product, listings: List[Listing]): List[Listing] = {
      listings.filter( listing => 
	    listing.title.contains(product.model) && (listing.title.contains(product.manufacturer) || 
	    										  listing.title.contains(product.family))
	  ).toList
    }
}