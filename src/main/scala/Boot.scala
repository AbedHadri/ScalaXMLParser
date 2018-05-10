import dao.{ProductCassandraDAOImpl, ProductMysqlDAOImpl}
import helper.FromXMLParseHelper

/**
  * Main Entry Of The Applicaiton
  * author: Ahmad
  */
object Boot extends App with FromXMLParseHelper {
  // parsing the xml

  //Getting a list of both products and it's prices
  val productsWithPrices = getProductsWithPricesList()

  //Export the products into mysql
  ProductMysqlDAOImpl.insertAllProducts(productsWithPrices)

  //Export the products prices into cassandra
  ProductCassandraDAOImpl.insertAllProducts(productsWithPrices)

  //Close the application
  System.exit(1)
}