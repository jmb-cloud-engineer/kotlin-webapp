package server.mappers

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import javax.sql.DataSource

//Object makes it a singleton, static, type of class.
object DbMapper {

    /**
     * Function does the following:
     * - Gets the metadata from a row and for a range of the columns count
     * - It maps it to a List of ColumnNames
     * - Then it maps again, this time retrieving the values based on the name
     * - This generates a List<Pair<String!, Any?>> which is put into a Map for all
     * the entries.
     */
    fun mapFromRow(row: Row): Map<String, Any?> {
        return row.underlying.metaData
            .let { (1..it.columnCount).map(it::getColumnName) }
            .map { it to row.anyOrNull(it) }
            .toMap()
    }

    //::mapFromRow is a function reference
    //'use' will invoke .close() on the session through kotlin query library
    //as sessionOf implements java 'closeable' interface wich needs manual closing
    fun executeSingleRowResultQuery(
        dataSource: DataSource,
        query: String): Map<String, Any?>? {
        return sessionOf(dataSource).use {dbSession ->
            dbSession.single(queryOf(query), ::mapFromRow)
        }
    }

    //Returns a list of maps representing a row each.
    //forEach can be used instead of list(), as it loads row by row and
    //can help with huge numbers of rows, or rows containing blobs, in terms of memory
    fun executeQuery(
        dataSource: DataSource,
        query: String
    ): List<Map<String, Any?>> {
        return sessionOf(dataSource).use {dbSession ->
            dbSession.list(queryOf(
                query), ::mapFromRow)
        }
    }

    
}