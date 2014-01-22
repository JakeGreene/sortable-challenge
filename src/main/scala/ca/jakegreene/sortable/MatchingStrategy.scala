package ca.jakegreene.sortable

import com.google.common.base.Splitter
import com.google.common.base.CharMatcher
import scala.collection.JavaConversions._

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
    
    val splitter = Splitter.on(CharMatcher.WHITESPACE.or(CharMatcher.is('/'))).omitEmptyStrings().trimResults() 
  
    override def findMatches(product: Product, listings: List[Listing]): List[Listing] = {
      listings.filter( listing => {
        val title = splitter.split(listing.title).toSet
	    title.contains(product.model) && (title.contains(product.manufacturer) || 
	                                      title.contains(product.family))
      })
    }
}