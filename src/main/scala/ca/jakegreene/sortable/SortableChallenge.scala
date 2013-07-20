package ca.jakegreene.sortable

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.io.Codec
import scala.io.Source
import scala.util.Failure
import scala.util.Success

import com.github.nscala_time.time.Imports.DateTime
import com.typesafe.config.ConfigFactory

import MatchingActor.FindMatches
import MatchingActor.FoundMatches
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import spray.json.pimpAny
import spray.json.pimpString

case class Product(product_name: String, manufacturer: String, family: Option[String], model: String, announced_date: DateTime)
case class Listing(title: String, manufacturer: String, currency: String, price: String)
case class Result(product_name: String, listings: List[Listing])

object SortableChallenge extends App with MatchingJsonProtocol {
	import MatchingActor._
	
	val config = ConfigFactory.load()	
	val matcherConfig = config.getConfig("ca.jakegreene.sortable").withFallback(config)
	
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
	
	val productFileName = matcherConfig.getString("data.product")
	val productLines = cleanProductData(Source.fromFile(productFileName).getLines.toIterable)
	val products = productLines.map(_.asJson.convertTo[Product]).toList
	
	val listingFileName = matcherConfig.getString("data.listing")
	val listingLines = Source.fromFile(listingFileName).getLines
	val listings = listingLines.map(line => line.asJson.convertTo[Listing]).toList
	
	val actorSystem = ActorSystem(matcherConfig.getString("system.system-name"))
	
	val timeoutLength = matcherConfig.getInt("system.future-timeout")
	implicit val timeout = Timeout(DurationInt(timeoutLength).second)
	
	val batchSize = matcherConfig.getInt("system.batch-size")
	val futures = for {
	  productGroup <- products.grouped(batchSize)
	  matcher = actorSystem.actorOf(Props(new MatchingActor with ExplicitMatchingStrategy))
	  resultFuture = ask(matcher, FindMatches(productGroup, listings)).mapTo[FoundMatches]
	} yield resultFuture
	
	import actorSystem.dispatcher // The dispatcher will be our execution context
	val resultFuture = Future.fold(futures)(List[Result]()) { (acc, foundMatches) =>
	  acc ++ foundMatches.results
	} onComplete {
	  case Success(results) => {
		val writer = new BufferedWriter(new FileWriter(new File("results.txt")))
	    results.foreach(result => 
	      writer.write(result.toJson + "\n")
	    )
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