package interview.service

import interview.models.Customer
import interview.service.CustomerDao.email
import interview.service.CustomerDao.firstName
import interview.service.CustomerDao.id
import interview.service.CustomerDao.lastName
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object CustomerService {

    class CustomerD(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<CustomerD>(CustomerDao)
        var firstName by CustomerDao.firstName
        var lastName     by CustomerDao.lastName
        var email by CustomerDao.email
    }

    fun store(customer: Customer) {
        connect()

        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

//            SchemaUtils.create (Cities)

            val storedCustomer = CustomerD.new {
                firstName = customer.firstName
                lastName = customer.lastName
                email = customer.email
            }
//            val customerId = CustomerDao.insert {
//                it[firstName] = customer.firstName
//                it[lastName] = customer.lastName
//                it[email] = customer.email
//            } get CustomerDao.id

            println("Customer = $storedCustomer")

            // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
            println("Customers: ${CustomerDao.selectAll()}")
        }
    }

    fun findAll(): List<Customer> {
        connect()

        return transaction {
            addLogger(StdOutSqlLogger)

            CustomerDao.selectAll().map { Customer(it[CustomerDao.id].toString(), it[firstName], it[lastName], it[email]) }
        }
    }

    fun remove(customerId: Int): Boolean {
        connect()

        return transaction {
            addLogger(StdOutSqlLogger)

            CustomerDao.deleteWhere { CustomerDao.id eq customerId } != 0
        }
    }

    private fun connect() {
        Database.connect(
            "jdbc:postgresql://localhost:5432/order_manager_db",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )
    }
}

object CustomerDao: IntIdTable() {
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val email = varchar("email", 100)
}
