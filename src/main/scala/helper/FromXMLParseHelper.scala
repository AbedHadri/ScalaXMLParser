package helper

import models.{Product, PriceAtDate}
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.xml.XML

trait FromXMLParseHelper {
  val logger = LoggerFactory.getLogger(getClass)

  /**
    * Gets the list of each product with its list of prices at specific dates
    * Of all the xml files in resources directory
    * @return
    */
  def getProductsWithPricesList(): List[(Product, List[PriceAtDate])] = {
    getResourceNames
      .par
      .flatMap(resourceName => parseXmlFileData(resourceName))
      .toList
  }

  /**
    * Returns a list containing a names of data sources (XML Files)
    * @return
    */
  def getResourceNames(): List[String] = {
    logger.info("Construction of resources file list")
    Source.fromURL(
      getClass.getResource("/resources.csv"))
      .getLines()
      .flatMap(line => line.split(", ")
      ).toList
  }

  /**
    * This method parses an xml file and extract the data from it and return it
    * as list of each product with its list of prices at specific dates
    * @param file
    * @return
    */
  def parseXmlFileData(file: String): List[(Product, List[PriceAtDate])] = {
    logger.info(s"Start parsing data of file ${file}")
    val xmlString = Source.fromURL(getClass.getResource(s"/$file")).mkString
    val xml = XML.loadString(xmlString)
    val productsXML = xml \ "row"
    productsXML.par
      .map(product => Product.productFromXML(product, file))
      .toList
  }
}
