package database

import com.typesafe.config.ConfigFactory

/**
  * This trait inludes the config file's values which will be used my the
  * object that extend this trait
  */
trait DatabaseManager {
  val conf = ConfigFactory.load
}

/**
  * Implementation of DatabaseManager which includes the setup of the connection and opening a connection
  * To be able to interact with MySQL Database
  */
object MySQLDatabaseMangerImpl extends DatabaseManager {

  import java.sql.{Connection, DriverManager}

  val mysqlConf = conf.getConfig("mysql")
  val url = mysqlConf.getString("url")
  val driver = mysqlConf.getString("driver")
  val username = mysqlConf.getString("username")
  val password = mysqlConf.getString("password")
  Class.forName(driver)
  val connection: Connection = DriverManager.getConnection(url, username, password)


}

/**
  * Implementation of DatabaseManager which includes the setup of the connection and oppening a session
  * To be able to interact with Cassandra
  */
object CassandraDatabaseManagerImpl extends DatabaseManager {

  import com.datastax.driver.core._

  val cassandraConf = conf.getConfig("cassandra")
  val sessionConf = cassandraConf.getConfig("session")

  val serverIp = sessionConf.getString("contactPoint")
  val keyspace = cassandraConf.getString("keyspace")
  val cluster = Cluster.builder()
    .addContactPoints(serverIp)
    .withPort(sessionConf.getInt("withPort"))
    .withoutMetrics()
    .withMaxSchemaAgreementWaitSeconds(sessionConf.getInt("maxSchemaAgreementWaitSeconds"))
    .withCredentials(sessionConf.getString("credentials.username"), sessionConf.getString("credentials.password"))
    .build()
  val session = cluster.connect(keyspace)
}

