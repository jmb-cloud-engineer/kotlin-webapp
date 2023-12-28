package server.config

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import server.mappers.DbMapper
import javax.sql.DataSource

private const val MIGRATION_PATH = "db/migration"
private const val FLYWAY_HISTORY_TABLE = "flyway_schema_history"

/**
 * Code to bootstrap a provide a connection to an underlying Datasource.
 * This implements a Singleton-Tied Factory Pattern, to avoid having more than one Datasource per
 * server.
 *
 * This can change if in the future the Server needs other Datasources, for which
 * an interface could be used and DI could be leveraged (i.e: SqlDatasourceConfigurer
 * and NoSQLDatasourceConfigurer)
 */
class DatasourceConfigurer private constructor(webAppConfigs: WebAppConfigs){

    //Created when instance is created
    val dataSource: DataSource = createAndMigrateDatasource(webAppConfigs)

    /**
     * The 'companion object' has access to the private constructor of its enclosing class.
     * So, it can instantiate the DatasourceConfigurer class using the private constructor.
     */
    companion object {
        private var INSTANCE: DatasourceConfigurer? = null

        fun getInstance(webAppConfigs: WebAppConfigs?): DatasourceConfigurer {
            webAppConfigs ?:
                throw IllegalArgumentException("Configurations cannot be null for creating a Datasource")

            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatasourceConfigurer(webAppConfigs).also {
                    INSTANCE = it
                }
            }
        }
    }

    private fun createAndMigrateDatasource(configs: WebAppConfigs): DataSource {
        val dataSource = createDatasource(configs).also {
            migrateDataSource(it)
        }
        return dataSource
    }

    private fun createDatasource(configs: WebAppConfigs) =
        HikariDataSource().apply {
            jdbcUrl = configs.dbUrl
            username = configs.dbUserName
            password = configs.dbPassword
        }

    private fun migrateDataSource(dataSource: DataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations(MIGRATION_PATH)
            .table(FLYWAY_HISTORY_TABLE)
            .load()
            .migrate()
    }
}