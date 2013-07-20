package ca.jakegreene.sortable

trait MatchingStrategy {
	protected def findMatches(product: Product, listings: List[Listing]): List[Listing]
}

trait ExplicitMatchingStrategy extends MatchingStrategy {
    override def findMatches(product: Product, listings: List[Listing]): List[Listing] = {
      listings.filter({ listing => 
	    listing.title.contains(product.model) && (listing.title.contains(product.manufacturer) || 
	    										  listing.title.contains(product.family))
	  }).toList
    }
}