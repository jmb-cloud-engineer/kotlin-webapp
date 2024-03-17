package server.mappers

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import javax.sql.DataSource

// Object makes it a singleton, static, type of class.
object DbMapper {

    /**
     * Function does the following:
     * - Gets the metadata from a row and for a range of the columns count
     * - It maps it to a List of ColumnNames
     * - Then it maps again, this time retrieving the values based on the name
     * - This generates a List<Pair<String!, Any?>> which is put into a Map for all
     * the entries.
     * ::mapFromRow is a function reference
     * 'use' will invoke .close() on the session through kotlin query library
     * as sessionOf implements java 'closeable' interface which needs manual closing
     *
     */
    fun mapFromRow(row: Row): Map<String, Any?> {
        return row.underlying.metaData
            .let { (1..it.columnCount).map(it::getColumnName) }
            .map { it to row.anyOrNull(it) }
            .toMap()
    }
}
