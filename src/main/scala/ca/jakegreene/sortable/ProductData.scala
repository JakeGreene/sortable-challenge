package ca.jakegreene.sortable

import org.joda.time.DateTime
import com.typesafe.config.ConfigFactory
import scala.io.Source
import spray.json.pimpAny
import spray.json.pimpString
import scala.io.Codec

case class Product(product_name: String, manufacturer: String, family: Option[String], model: String, announced_date: DateTime)
case class Listing(title: String, manufacturer: String, currency: String, price: String)
case class Result(product_name: String, listings: List[Listing])

trait ProductData {
	def products: List[Product]
	def listings: List[Listing]
}

trait ProductDataFromFile extends ProductData with MatchingJsonProtocol { 
  
	// Some of the product names and listing titles use UTF-8 characters
	implicit val codec: Codec = Codec.UTF8
  
	private val dataConfig = ConfigFactory.load().getConfig("ca.jakegreene.sortable.data")
	val productFileName = dataConfig.getString("product")
	val productLines = cleanProductData(Source.fromFile(productFileName).getLines.toIterable)
	val productList = productLines.map(_.asJson.convertTo[Product]).toList
	override def products: List[Product] = {
	  productList
	}
	
	val listingFileName = dataConfig.getString("listing")
	val listingLines = Source.fromFile(listingFileName).getLines.toIterable
	val listingList = listingLines.map(_.asJson.convertTo[Listing]).toList
	override def listings: List[Listing] = {
	  listingList
	}
	
	private def cleanProductData(productLines: Iterable[String]): List[String] = {
	  /*
	   * Scala case classes cannot have dashes "-" in their names
	   * but spray-json requires the case class member names to be the
	   * same as the names in the JSON object.
	   */
	  productLines.map(line => line.replaceFirst("announced-date", "announced_date")).toList
	}
}