package server.config

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

class DataSourceManager(private val configs: WebAppConfigs) {

    private lateinit var dataSource: DataSource

    fun initializeDataSource(): DataSource {
        dataSource = createDatasource().also {
            migrateDataSource(it)
        }
        return dataSource
    }

    private fun createDatasource() =
        HikariDataSource().apply {
            jdbcUrl = configs.dbUrl
            username = configs.dbUserName
            password = configs.dbPassword
        }

    private fun migrateDataSource(dataSource: DataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("db/migration")
            .table("flyway_schema_history")
            .load()
            .migrate()
    }

    fun healthCheck(): String {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SELECT 1")
            }
        }
        return "DATABASE STATUS: OK"
    }
}
