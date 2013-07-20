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

object SortableChallenge extends App with ProductDataFromFile with SystemPreferences {
	import MatchingActor._

	val futureResults = for {
	  productGroup <- products.grouped(batchSize)
	  matcher = actorSystem.actorOf(Props(new MatchingActor with ExplicitMatchingStrategy))
	  result = ask(matcher, FindMatches(productGroup, listings)).mapTo[FoundMatches]
	} yield result
	
	Future.fold(futureResults)(List[Result]()) { (acc, foundMatches) =>
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