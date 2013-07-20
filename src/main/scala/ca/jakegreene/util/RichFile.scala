package ca.jakegreene.util

import java.io._
import scala.io._

/**
 * Rich file allows for easy one-line writes and reads
 * taken from:
 * http://stackoverflow.com/a/6881792/382110
 */
class RichFile(file: File) {

  def text = Source.fromFile(file)(Codec.UTF8).mkString

  def text_=(s: String) {
    val out = new PrintWriter(file , "UTF-8")
    try{ out.print(s) }
    finally{ out.close }
  }
}

object RichFile {
  implicit def enrichFile(file: File) = new RichFile(file)
}