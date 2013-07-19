package ca.jakegreene.sortable

import scala.io._
import com.github.nscala_time.time.Imports._
import spray.json._
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter

case class Product(product_name: String, manufacturer: String, family: Option[String], model: String, announced_date: DateTime)
case class Listing(title: String, manufacturer: String, currency: String, price: String)
case class Result(product_name: String, listings: List[Listing])

object MatchingJsonProtocol extends DefaultJsonProtocol {
  
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
  
	import MatchingJsonProtocol._
	
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
	
	private def findMatchingListings(product: Product, listings: Iterable[Listing]): List[Listing] = {
	  val productName = product.product_name.replace('_', ' ')
	  listings.filter(listing => listing.title.contains(productName)).toList
	}
	
	val results = products.map(product => Result(product.product_name, findMatchingListings(product, listings.toIterable))).toList
	
	val writer = new BufferedWriter(new FileWriter(new File("results.txt")))
	for (result <- results) {
	  val json = result.toJson
	  writer.write(json.prettyPrint + "\n")
	}
	writer.close()
	println("Matching Complete")
}