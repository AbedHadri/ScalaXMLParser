package dao

import database.{CassandraDatabaseManagerImpl, MySQLDatabaseMangerImpl}
import models.{PriceAtDate, Product}
import org.slf4j.LoggerFactory

/**
  * This trait has one method which insert data into database
  */
trait ProductsDAO {
  val logger = LoggerFactory.getLogger(getClass)

  abstract def insertAllProducts(products: List[(Product, List[PriceAtDate])]): Unit
}

//Note: here i had to use try catch.
object ProductMysqlDAOImpl extends ProductsDAO {

  /**
    * Here we insert products into Mysql database in parallel
    */
  override def insertAllProducts(products: List[(Product, List[PriceAtDate])]): Unit = {
    logger.info("Inserting products information into mysql database")
    try {
      products.par.foreach(
        productWithPriceList => {

          val productInfo = productWithPriceList._1
          logger.info(s"Inserting product information into mysql database of product with id = ${productInfo.id}")
          val insertionPreparedStatement = MySQLDatabaseMangerImpl.connection.prepareStatement("Insert IGNORE into products(storeID,productID,title,brand,category,url) values (? , ? , ? , ? , ? , ?);")
          insertionPreparedStatement.setInt(1, productInfo.store)
          insertionPreparedStatement.setLong(2, productInfo.id)
          insertionPreparedStatement.setString(3, productInfo.title)
          insertionPreparedStatement.setString(4, productInfo.brand)
          insertionPreparedStatement.setString(5, productInfo.category)
          insertionPreparedStatement.setString(6, productInfo.url)
          insertionPreparedStatement.executeUpdate()
        }
      )
    } catch {
      case e: Exception => logger.error(e.getMessage)
    } finally {
      MySQLDatabaseMangerImpl.connection.close()
    }
    logger.info("Insertion of data into mysql was made successfully")
  }
}

object ProductCassandraDAOImpl extends ProductsDAO {

  override def insertAllProducts(products: List[(Product, List[PriceAtDate])]): Unit = {
    /**
      * Here we insert products into Mysql database in parallel
      */
    logger.info("Inserting products information into cassandra database")
    try {
      products.par.foreach(
        productWithPriceList => {
          val productInfo = productWithPriceList._1
          val productPrices = productWithPriceList._2
          if (productPrices.nonEmpty) {
            logger.info(s"Inserting product's price list into cassandra database of product with id = ${productInfo.id}")
            CassandraDatabaseManagerImpl
              .session
              .execute(s"Insert into products(id, productid , datepriceList) values ( now() , ${productInfo.id} , {${productPrices.map(list => s"${list.date.getOrElse(-1)}:${list.price.getOrElse(-1)}").mkString(",")}})")
          }
        }
      )
    } catch {
      case e: Exception => logger.error(e.getMessage)
    } finally {
      CassandraDatabaseManagerImpl.session.close()
    }
    logger.info("Insertion of data into cassandra was made successfully")
  }


}


