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

object SortableChallenge extends App with ProductDataFromFile {
	import MatchingActor._
	
	val config = ConfigFactory.load()	
	val matcherConfig = config.getConfig("ca.jakegreene.sortable").withFallback(config)
	
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