package ca.jakegreene.sortable

import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.util.Timeout

trait SystemPreferences {
	val config = ConfigFactory.load().getConfig("ca.jakegreene.sortable.system")
	
	val actorSystem = ActorSystem(config.getString("system-name"))
	implicit val executionContext = actorSystem.dispatcher
	
	val timeoutLength = config.getInt("future-timeout")
	implicit val timeout: Timeout = timeoutLength.second
	
	val batchSize = config.getInt("batch-size")
}