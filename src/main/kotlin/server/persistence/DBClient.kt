package server.persistence

import kotliquery.queryOf
import kotliquery.sessionOf
import server.mappers.DbMapper
import javax.sql.DataSource

class DBClient(private val dataSource: DataSource) {

    fun executeSingleRowResultQuery(
        dataSource: DataSource,
        query: String
    ): Map<String, Any?>? {
        return sessionOf(dataSource).use { dbSession ->
            dbSession.single(queryOf(query), DbMapper::mapFromRow)
        }
    }

    // Returns a list of maps representing a row each.
    // forEach can be used instead of list(), as it loads row by row and
    // can help with huge numbers of rows, or rows containing blobs, in terms of memory
    fun executeQuery(
        dataSource: DataSource,
        query: String
    ): List<Map<String, Any?>> {
        return sessionOf(dataSource).use { dbSession ->
            dbSession.list(
                queryOf(
                    query
                ),
                DbMapper::mapFromRow
            )
        }
    }
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
