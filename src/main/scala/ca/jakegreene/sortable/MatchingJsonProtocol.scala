package ca.jakegreene.sortable

import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat
import spray.json.JsString
import spray.json.JsValue
import org.joda.time.DateTime
import spray.json.DeserializationException

trait DateTimeJsonProtocol extends DefaultJsonProtocol {
  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {
    def write(date: DateTime) = JsString(date.toString())
    def read(value: JsValue) = value match {
      case JsString(date) => new DateTime(date)
      case _ => throw new DeserializationException("DateTime expected")
    }
  }
}

trait MatchingJsonProtocol extends DefaultJsonProtocol with DateTimeJsonProtocol { 
  implicit val productFormat = jsonFormat5(Product)
  implicit val listingFormat = jsonFormat4(Listing)
  implicit val resultFormat = jsonFormat2(Result)
}