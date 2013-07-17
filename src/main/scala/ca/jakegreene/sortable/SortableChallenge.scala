package ca.jakegreene.sortable

import scala.io._
import com.github.nscala_time.time.Imports._
import spray.json._
import java.io.File
import java.io.FileWriter

case class Product(product_name: String, manufacturer: String, family: Option[String], model: String, announced_date: DateTime)
case class Listing(title: String, manufacturer: String, currency: String, price: String)
case class Result(product_name: String, listings: Seq[Listing])

object MyJsonProtocol extends DefaultJsonProtocol {
  
  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {
    def write(date: DateTime) = JsString(date.toString())
    def read(value: JsValue) = value match {
      case JsString(date) => new DateTime(date)
      case _ => throw new DeserializationException("DateTime expected")
    }
  }
  
  implicit val productFormat = jsonFormat5(Product)
  implicit val listingFormat = jsonFormat4(Listing)
  implicit val resultFormat = jsonFormat2(Result)
}

object SortableChallenge extends App {
  
	import MyJsonProtocol._
	
	// Some of the product names and listing titles are in different languages
	implicit val codec = Codec.UTF8
	
	val prodBuilder = Seq.newBuilder[Product]
	for (line <- Source.fromFile("data/products.txt").getLines) {
	  /*
	   * Scala case classes cannot have dashes "-" in their names
	   * but spray-json requires the case class member names to be the
	   * same as the names in the JSON object.
	   */ 
	  val properLine = line.replaceAll("announced-date", "announced_date")
	  val jsonAst = properLine.asJson
	  prodBuilder += jsonAst.convertTo[Product]
	}
	val products = prodBuilder.result

	val listBuilder = Seq.newBuilder[Listing]
	for (line <- Source.fromFile("data/listings.txt").getLines) {
	  val jsonAst = line.asJson
	  listBuilder += jsonAst.convertTo[Listing]
	}
	val listings = listBuilder.result
	
	val resultBuilder = Seq.newBuilder[Result]
	for (product <- products) {
	  val matchBuilder = Seq.newBuilder[Listing]
	  for (listing <- listings) {
	    if (listing.title.contains(product.product_name.replace("_", " "))) {
	      matchBuilder += listing
	    }
	  }
	  resultBuilder += Result(product.product_name, matchBuilder.result)
	}
	val results = resultBuilder.result
	
	val writer = new FileWriter(new File("results.txt"))
	results.map(r => r.toJson).foreach(json => writer.write(json.prettyPrint + '\n'))
	writer.close()
}