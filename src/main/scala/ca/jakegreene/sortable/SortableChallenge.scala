package ca.jakegreene.sortable

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success
import com.typesafe.config.ConfigFactory
import MatchingActor.FindMatches
import MatchingActor.FoundMatches
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import spray.json.pimpAny
import ca.jakegreene.util.RichFile.enrichFile
import akka.routing.FromConfig
import scala.collection.immutable.Queue

/**
 * Sortable Challenge solution. Reads and parses a list of products and a list of
 * product listings. Attempts to determine which products are contained within
 * each listing.
 * 
 * General Idea:
 * - Read and Parse the products/listings json into case classes
 * - Scatter and Gather: Divide the product list into groups, work 
 *   on the groups independently, then collect and append the results
 *
 * Extensions I never got around to:
 * - Data preprocessing trait to organize the data before the matcher sees it
 * - Scatter further by dividing the listings into groups. No visible
 *   improvement on my rinky-dink dual-core laptop so I decided to
 *   leave it out
 */
object SortableChallenge extends App with ProductDataFromFile with SystemPreferences {
	import MatchingActor._
	
	val matchRouter = actorSystem.actorOf(Props(new MatchingActor with ExplicitMatchingStrategy).withRouter(FromConfig()), "matching-router")

	val futureResults = for {
	  productGroup <- products.grouped(batchSize)
	  result = ask(matchRouter, FindMatches(productGroup, listings)).mapTo[FoundMatches]
	} yield result
	
	Future.fold(futureResults)(Queue[Result]()) { (acc, foundMatches) =>
	  acc ++ foundMatches.results
	} andThen {
	  case Success(results) => {
	    val output = new File("output.txt")
	    output.text = results.map(_.toJson).mkString("\n")
	  }
	  case Failure(failure) => println(s"Failure Getting Results: $failure")
	} andThen {
	  case _ => actorSystem.shutdown()
	}
}