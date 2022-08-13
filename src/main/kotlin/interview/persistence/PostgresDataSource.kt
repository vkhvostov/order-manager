package interview.persistence

import com.zaxxer.hikari.HikariDataSource

object PostgresDataSource {

    // create a dataSource
    val dataSource = HikariDataSource()

    init {
        // set the jdbcUrl
        dataSource.jdbcUrl = "jdbc:postgresql://localhost:5432/order_manager_db"

        dataSource.driverClassName = "org.postgresql.Driver"

        // set the username
        dataSource.username = "postgres"

        // set the password
        dataSource.password = "postgres"

        println(dataSource.maximumPoolSize)
    }
}
