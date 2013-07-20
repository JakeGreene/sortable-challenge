package ca.jakegreene.sortable

import akka.actor.ActorLogging
import akka.actor.Actor

object MatchingActor {
  sealed trait Input
  case class FindMatches(products: List[Product], listings: List[Listing]) extends Input
  sealed trait Output
  case class FoundMatches(results: List[Result]) extends Output
}

class MatchingActor extends Actor with ActorLogging {
	import MatchingActor._
	
	def receive = {
	  case FindMatches(products, listings) => {
	    val results = products.map(product => Result(product.product_name, findMatchingListings(product, listings.toIterable))).toList
	    sender ! FoundMatches(results)
	  }
	  case out: Output => log.warning(s"$self has received an Output message as input: $out")
	  case msg => log.warning(s"$self has received an unhandled message: $msg")
	}
		
	private def findMatchingListings(product: Product, listings: Iterable[Listing]): List[Listing] = {
	  listings.filter(listing => {
	    listing.title.contains(product.model) && (listing.title.contains(product.manufacturer) || 
	    										listing.title.contains(product.family))
	  }).toList
	}
}