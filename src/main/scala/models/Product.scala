package models

import org.slf4j.LoggerFactory

import scala.xml.NodeSeq

case class Product(
                    id: Long,
                    title: String,
                    brand: String,
                    category: String,
                    url: String,
                    store: Int
                  ) {
  override def toString: String = s"($id , $title )"

  override def equals(o: Any) = o match {
    case that: Product => that.id == this.id
    case _ => false
  }

  override def hashCode = id.hashCode
}

case class PriceAtDate(
                        date: Option[Long],
                        price: Option[Float]
                      )

object Product {

  val logger = LoggerFactory.getLogger(getClass)

  /**
    * Helper used to get optional values from parsed xml elements
    * @param nodeSeq
    */
  implicit class ExtendedNodeSeq(nodeSeq: NodeSeq) {
    def textOption: Option[String] = {
      val text = nodeSeq.text
      if (text == null || text.length == 0) None else Some(text)
    }
  }

  /**
    * The method create a Map of prices at specific time of product
    * Here if one the the parsed values is empty we return a non empty map so we don't ignor such values
    * When exporting to Cassandra
    * @param id
    * @param priceSequence
    * @param dateSequence
    * @return
    */
  def createProductPriceAtDateMap(id: Long, priceSequence: NodeSeq, dateSequence: NodeSeq): List[PriceAtDate] = {
    logger.info(s"Creating price at date map of product with id =${id}")
    if (priceSequence.text.isEmpty && dateSequence.text.isEmpty)
      List()
    else if (priceSequence.text.isEmpty && dateSequence.text.nonEmpty)
      Option(dateSequence.textOption.get.toString.split(",").par.map(date => date.toLong).toList).get.par.map(productDates => PriceAtDate(Some(productDates), None)).toList
    else if (priceSequence.text.nonEmpty && dateSequence.text.isEmpty)
      Option(priceSequence.textOption.get.toString.split(",").par.map(price => price.toFloat).toList).get.par.map(productPrices => PriceAtDate(None, Some(productPrices))).toList
    else {
      val prices = priceSequence.textOption.get.toString.split(",").par.map(price => price.toFloat).toList
      val dates = dateSequence.textOption.get.toString.split(",").par.map(date => date.toLong).toList
      val datePriceMap = (dates zip prices).toMap
      datePriceMap.par.map(productPrices => PriceAtDate(Some(productPrices._1), Some(productPrices._2))).toList
    }
  }

  /**
    * Thıs method create Product object by parsing XML element
    * @param productAsXML
    * @param fileName
    * @return
    */
  def productFromXML(productAsXML: scala.xml.NodeSeq, fileName: String): (Product, List[PriceAtDate]) = {
    logger.info(s"Parsing Product Details From File ${fileName}")
    (Product(
      id = (productAsXML \ "id").text.toLong,
      title = (productAsXML \ "title").textOption.getOrElse("Not specified").replace("'", "''"),
      brand = (productAsXML \ "brand").textOption.getOrElse("Not specified").replace("'", "''"),
      category = (productAsXML \ "category").textOption.getOrElse("Not specified").replace("'", "''"),
      url = (productAsXML \ "url").textOption.getOrElse("Not specified").replace("'", "\'"),
      store = fileName.replaceAll("site", "").replace(".xml", "").toInt
    ), createProductPriceAtDateMap((productAsXML \ "id").text.toLong, productAsXML \ "prices", productAsXML \ "dates"))
  }
}
