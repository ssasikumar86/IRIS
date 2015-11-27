package com.temenos.interaction.authorization.command.data;

import java.util.Objects;

import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.expression.OrderByExpression.Direction;

/*
 * Thin wrapper round the odat4j OrderByExpression.
 */

/*
 * #%L
 * interaction-commands-Authorization
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

public class OrderBy {

    // Field name
    private FieldName name;

    // Direction Enum imported from OrderByExpression
    private Direction direction;

    public OrderBy(String name, OrderByExpression.Direction direction) {
        this.name = new FieldName(name);
        this.direction = direction;
    }

    public OrderBy(OrderByExpression orderBy) {
        this(((EntitySimpleProperty) orderBy.getExpression()).getPropertyName(), orderBy.getDirection());
    }

    public FieldName getFieldName() {
        return name;
    }

    public boolean isAcsending() {
        return direction == Direction.ASCENDING;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getDirectionString() {
        if (direction == Direction.ASCENDING) {
            return "asc";
        } else {
            return "desc";
        }
    }

    /**
     * Define equality of state. To enable comparison.
     */
    @Override
    public boolean equals(Object aThat) {
        if (this == aThat)
            return true;
        if (!(aThat instanceof OrderBy))
            return false;

        OrderBy that = (OrderBy) aThat;
        return (this.getFieldName().getName().equals(that.getFieldName().getName()) && (this.getDirection() == that
                .getDirection()));
    }

    /**
     * Return same hash code for identical objects. So contains() works.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, direction);
    }

}
