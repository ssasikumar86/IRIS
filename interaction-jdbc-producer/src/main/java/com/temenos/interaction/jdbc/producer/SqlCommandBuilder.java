package com.temenos.interaction.jdbc.producer;

/*
 * Utility class for building SQL commands.
 * 
 * If given a key constructs a command for a single row. 
 * 
 * If given a null key constructs a command to add all rows.
 * 
 * TODO maybe need variants for different databases.
 */

/*
 * #%L
 * interaction-jdbc-producer
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.authorization.command.data.AccessProfile;
import com.temenos.interaction.authorization.command.data.FieldName;
import com.temenos.interaction.authorization.command.data.OrderBy;
import com.temenos.interaction.authorization.command.data.RowFilter;

class SqlCommandBuilder {

    // Somewhere to store arguments
    private String tableName;
    private String keyValue;
    private AccessProfile accessProfile;
    private ColumnTypesMap colTypesMap;
    private String top;
    private String skip;
    private List<OrderBy> orderBy;

    // Server mode.Or compatibility mode under H2.
    public enum ServerMode {
        // Real server modes
        MSSQL, ORACLE,
        // H2 server compatibility modes. Used for testing.
        H2_MSSQL, H2_ORACLE
    };

    // Server compatibility mode.
    private ServerMode serverMode;

    // Flag indicating that the server is really H2. i.e. an emulated server for
    // testing.
    private boolean serverIsEmulated;

    // Name of rownum exported form inner select.
    private final static String INNER_RN_NAME = "rn";

    private final static Logger logger = LoggerFactory.getLogger(SqlCommandBuilder.class);

    /*
     * Constructor when there is not a key.
     */
    public SqlCommandBuilder(String tableName, AccessProfile accessProfile, ColumnTypesMap colTypesMap, String top,
            String skip, List<OrderBy> orderBy, ServerMode serverMode) {
        this.tableName = tableName;
        this.accessProfile = accessProfile;
        this.colTypesMap = colTypesMap;
        this.serverMode = serverMode;
        this.top = top;
        this.skip = skip;
        this.orderBy = orderBy;

        // Check if reserved row number column name is present.
        boolean exists = true;
        try {
            colTypesMap.getType(INNER_RN_NAME);
        } catch (SecurityException e) {
            // Not found. This is what we want.
            exists = false;
        }
        if (exists) {
            // Possibly should throw or maybe dynamically work out an unique
            // column name. For now just warn the user.
            logger.warn("Table contains a column with the reserved name \"" + INNER_RN_NAME
                    + "\" pagination may not perform as expected");
        }

        setCompatibilityMode(serverMode);
    }

    /*
     * Set up server compatibility modes.
     */
    private void setCompatibilityMode(ServerMode serverMode) {
        // Default to emulated MSSQL mode
        if (null == serverMode) {
            serverMode = ServerMode.MSSQL;
        }

        switch (serverMode) {
        case MSSQL:
            this.serverMode = serverMode;
            this.serverIsEmulated = false;
            break;

        case H2_MSSQL:
            this.serverMode = ServerMode.MSSQL;
            this.serverIsEmulated = true;
            break;

        case H2_ORACLE:
            this.serverMode = ServerMode.ORACLE;
            this.serverIsEmulated = true;
            break;

        case ORACLE:
        default:
            this.serverMode = serverMode;
            this.serverIsEmulated = false;
            break;
        }

    }

    /*
     * Constructor when there is a key.
     */
    public SqlCommandBuilder(String tableName, String keyValue, AccessProfile accessProfile,
            ColumnTypesMap colTypesMap, String top, String skip, List<OrderBy> orderBy, ServerMode serverMode) {
        this(tableName, accessProfile, colTypesMap, top, skip, orderBy, serverMode);
        this.keyValue = keyValue;
    }

    /*
     * Method to build Sql command
     */
    public String getCommand() {

        // Build inner SQL command
        StringBuilder builder = new StringBuilder("SELECT");
        addRowNumSelect(builder);
        addSelects(builder);
        addFrom(builder);
        addWhereTerms(builder);

        switch (serverMode) {
        case ORACLE:
            // Always need an order by term
            addOrderByTerms(builder);
            break;
        case MSSQL:
        default:
            // If we have not already included it, for $top or $skip, need an
            // order by.
            if ((null == top) && (null == skip)) {
                addOrderByTerms(builder);
            }
            break;
        }

        // Package the inner SQL command in an outer SQL command.
        addTopAndSkip(builder);

        return builder.toString();
    }

    /*
     * To select row number ranges ($top and $skip) in outer select we need the
     * INNER_RN_NAME alias in the inner select.
     */
    private void addRowNumSelect(StringBuilder builder) {
        if ((null != top) || (null != skip)) {
            switch (serverMode) {
            case MSSQL:
                builder.append(" ROW_NUMBER() OVER (");

                // H2 in MSSQL mode does not yet support the inner "ORDER BY"
                // syntax but MSSQL requires it.
                if (!serverIsEmulated) {
                    addOrderByTerms(builder);
                }

                builder.append(") AS \"" + INNER_RN_NAME + "\",");
                break;

            case ORACLE:
            default:
                builder.append(" ROWNUM \"" + INNER_RN_NAME + "\",");
                break;
            }
        }
    }

    private void addSelects(StringBuilder builder) {

        // Add columns to select
        Set<FieldName> names = accessProfile.getFieldNames();
        if (null == names) {
            throw (new SecurityException("Cannot generate Sql command for null field set."));
        }
        if (names.isEmpty()) {
            // Empty select list means "return all columns".
            builder.append(" \"" + tableName + "\".*");
        } else {
            // Add comma separated list of select terms. Need to detect the last
            // operation so use old style iterator.
            Iterator<FieldName> iterator = names.iterator();
            while (iterator.hasNext()) {
                FieldName name = iterator.next();
                addSelect(builder, name);

                // If not the last entry
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
        }
    }

    private void addSelect(StringBuilder builder, FieldName name) {
        builder.append(" \"" + name.getName() + "\"");
    }

    private void addFrom(StringBuilder builder) {
        builder.append(" FROM \"" + tableName + "\"");
    }

    /*
     * add the "WHERE x AND y" etc clause. Adds filters and/or key.
     */
    private void addWhereTerms(StringBuilder builder) {

        // If there are no filters or key return;
        if (accessProfile.getRowFilters().isEmpty() && (null == keyValue)) {
            return;
        }

        addWhere(builder);

        if (null != keyValue) {
            if (null == colTypesMap.getPrimaryKeyName()) {
                throw (new SecurityException("No primary key column defined for \"" + tableName
                        + "\". Cannot look up key."));
            }

            // Add key as a filter
            RowFilter keyFilter = new RowFilter(colTypesMap.getPrimaryKeyName(), RowFilter.Relation.EQ, keyValue);
            addFilter(builder, keyFilter);
        }

        if (!accessProfile.getRowFilters().isEmpty()) {
            // If we already had a key need to link with an 'AND'.
            if (null != keyValue) {
                addAnd(builder);
            }
            addFilters(builder);
        }
    }

    private void addWhere(StringBuilder builder) {
        builder.append(" WHERE");
    }

    private void addAnd(StringBuilder builder) {
        builder.append(" AND");
    }

    private void addFilters(StringBuilder builder) {

        // Add row filters
        List<RowFilter> filters = accessProfile.getRowFilters();
        if (null == filters) {
            throw (new SecurityException("Cannot generate Sql command for null row filters list."));
        }

        // Add AND separated list of filter terms. Need to detect the last
        // operation so use old style iterator.
        Iterator<RowFilter> iterator = filters.iterator();
        while (iterator.hasNext()) {
            RowFilter filter = iterator.next();

            addFilter(builder, filter);

            // If not the last entry
            if (iterator.hasNext()) {
                addAnd(builder);
            }
        }
    }

    private void addFilter(StringBuilder builder, RowFilter filter) {
        builder.append(" \"" + filter.getFieldName().getName() + "\"");
        builder.append(filter.getRelation().getSqlSymbol());

        // Extract the column type from the metadata. Text
        // must be quoted but not numerics.
        boolean numeric = colTypesMap.isNumeric(filter.getFieldName().getName());
        if (numeric) {
            String value = filter.getValue();
            if (!isJdbcNumeric(value)) {
                throw (new SecurityException("Jdbc column \"" + filter.getFieldName().getName()
                        + "\" is numeric. Filter value \"" + value + "\" is non numeric."));
            }
            builder.append(value);
        } else {
            builder.append("'" + filter.getValue() + "'");
        }
    }

    /*
     * Add $top and $skip components for this server type.
     * 
     * This is messy. To support pagination an inner select is wraped by an
     * outer select. For more information search online for "oracle pagination".
     */
    private void addTopAndSkip(StringBuilder builder) {
        if ((null == top) && (null == skip)) {
            // Nothing to do
            return;
        }

        // Add start of outer command
        builder.insert(0, "SELECT * FROM (");

        // Add inner command end bracket
        builder.append(")");

        if (ServerMode.MSSQL == serverMode) {
            // Add extra MSSQL syntax.
            builder.append(" AS tbl");
        }

        // Add where clauses
        addSkip(builder);
        addTop(builder);
    }

    private void addTop(StringBuilder builder) {
        if (null != top) {
            // Work out max row
            int maxRow = Integer.parseInt(top);
            if (null != skip) {
                maxRow += Integer.parseInt(skip);
            }

            if (null != skip) {
                // If we already have a skip add "AND" to existing "WHERE"
                addAnd(builder);
            } else {
                addWhere(builder);
            }
            builder.append(" \"" + INNER_RN_NAME + "\" <= " + maxRow);
        }
    }

    private void addSkip(StringBuilder builder) {
        if (null != skip) {
            addWhere(builder);
            builder.append(" \"" + INNER_RN_NAME + "\" > " + skip);
        }
    }

    /*
     * Add order by term. If this is not present rows will be returned in a
     * random order.
     */
    private void addOrderByTerms(StringBuilder builder) {
        if (null != orderBy) {
            addOrderBy(builder);
            boolean first = true;
            for (OrderBy order : orderBy) {
                if (!first) {
                    builder.append(",");
                } else {
                    first = false;
                }
                addOrderByTerm(builder, order.getFieldName().getName(), order.isAcsending());
            }
        } else {
            // By default order by the primary key.
            if (null == colTypesMap.getPrimaryKeyName()) {
                logger.warn("Primary key name not known. Cannot add \"ORDER BY\" clause.");
                return;
            }
            addOrderBy(builder);
            addOrderByTerm(builder, colTypesMap.getPrimaryKeyName(), true);
        }
    }

    private void addOrderBy(StringBuilder builder) {
        builder.append(" ORDER BY");
    }

    private void addOrderByTerm(StringBuilder builder, String columnName, boolean ascending) {
        builder.append(" \"" + columnName + "\"");
        if (!ascending) {
            builder.append(" DESC");
        }
    }

    /*
     * Utility to check if a string is representable as a Jdbc numeric.
     */
    private boolean isJdbcNumeric(String value) {
        try {
            // Java "BigDecimal" appears to be the closest data type to Jdbc
            // "numeric".
            new BigDecimal(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /*
     * Utility to obtain the reserved 'row number' column name. Used mainly in
     * testing but could be useful to the end user.
     */
    public static String getRnName() {
        return INNER_RN_NAME;
    }
}
