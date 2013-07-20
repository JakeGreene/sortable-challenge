package ca.jakegreene.sortable

import scala.io._
import scala.concurrent.duration._
import com.github.nscala_time.time.Imports._
import spray.json._
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import akka.dispatch.Futures
import scala.actors.Future
import scala.actors.Future
import scala.parallel.Future
import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.Failure
import scala.util.Success

case class Product(product_name: String, manufacturer: String, family: Option[String], model: String, announced_date: DateTime)
case class Listing(title: String, manufacturer: String, currency: String, price: String)
case class Result(product_name: String, listings: List[Listing])

object SortableChallenge extends App {
  
	import MatchingJsonProtocol._
	import MatchingActor._
	
	// Some of the product names and listing titles use UTF-8 characters
	implicit val codec: Codec = Codec.UTF8
	
	private def cleanProductData(productLines: Iterable[String]): List[String] = {
	  /*
	   * Scala case classes cannot have dashes "-" in their names
	   * but spray-json requires the case class member names to be the
	   * same as the names in the JSON object.
	   */
	  productLines.map(line => line.replaceFirst("announced-date", "announced_date")).toList
	}
	  
	val productLines = cleanProductData(Source.fromFile("data/products.txt").getLines.toIterable)
	val products = productLines.map(_.asJson.convertTo[Product]).toList
	
	val listingLines = Source.fromFile("data/listings.txt").getLines
	val listings = listingLines.map(line => line.asJson.convertTo[Listing]).toList
	
	val actorSystem = ActorSystem("MatchingSystem")
	implicit val timeout = Timeout(DurationInt(5).second)
	val futures = for {
	  productGroup <- products.grouped(100)
	  matcher = actorSystem.actorOf(Props[MatchingActor])
	  resultFuture = ask(matcher, FindMatches(productGroup, listings)).mapTo[FoundMatches]
	} yield resultFuture
	
	import actorSystem.dispatcher // The dispatcher will be our execution context
	val resultFuture = Future.fold(futures)(List[Result]()) { (acc, foundMatches) =>
	  acc ++ foundMatches.results
	} onComplete {
	  case Success(results) => {
		val writer = new BufferedWriter(new FileWriter(new File("results.txt")))
	    results.foreach(result => writer.write(result.toJson + "\n"))
	    writer.close()
	    println("Matching Complete")
	    actorSystem.shutdown()
	  }
	  case Failure(failure) => {
	    println(s"Failure Getting Results: $failure")
	    actorSystem.shutdown()
	  }
	}
}