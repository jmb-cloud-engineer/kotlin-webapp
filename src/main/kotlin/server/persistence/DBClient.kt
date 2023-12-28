package server.persistence

import server.mappers.DbMapper
import javax.sql.DataSource

class DBClient(private val dataSource: DataSource) {
    fun dbMapperHealthCheck(): String {
        val queryResult = DbMapper.run {
            executeSingleRowResultQuery(dataSource, "SELECT 1")
        }

        val statusFailed = queryResult?.isEmpty() ?: true

        return if (statusFailed) {
            "DATABASE STATUS: FAILED"
        } else {
            "DATABASE STATUS: OK"
        }
    }
}