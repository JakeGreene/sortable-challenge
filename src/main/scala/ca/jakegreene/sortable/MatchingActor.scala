package ca.jakegreene.sortable

import akka.actor.ActorLogging
import akka.actor.Actor

object MatchingActor {
  sealed trait Input
  case class FindMatches(products: List[Product], listings: List[Listing]) extends Input
  sealed trait Output
  case class FoundMatches(results: List[Result]) extends Output
}

class MatchingActor extends Actor with ActorLogging with ExplicitMatchingStrategy {
	import MatchingActor._
	
	def receive = {
	  case FindMatches(products, listings) => {
	    val results = products.map(product => Result(product.product_name, findMatches(product, listings))).toList
	    sender ! FoundMatches(results)
	  }
	  case out: Output => log.warning(s"$self has received an Output message as input: $out")
	  case msg => log.warning(s"$self has received an unhandled message: $msg")
	}
}