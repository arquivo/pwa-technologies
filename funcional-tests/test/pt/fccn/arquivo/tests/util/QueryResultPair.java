/*
    Copyright (C) 2011 David Cruz <david.cruz@fccn.pt>
    Copyright (C) 2011 SAW Group - FCCN <saw@asa.fccn.pt>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package pt.fccn.arquivo.tests.util;

/**
 * A utility class that represent a pair of 'query' string and expected
 * query string 'response'.
 * @author David Cruz <david.cruz @ fccn.pt>
 */
public class QueryResultPair {

        private String query;
        private String result;

        /**
         * Empthy Constructor
         */
        public QueryResultPair() {}

        /**
         * Constructor
         */
        public QueryResultPair(String query, String result) {
                setQuery(query);
                setResult(result);
        }

        /**
         * Get the query string
         * @return the query string
         */
        public String getQuery() {
                return query;
        }

        /**
         * Get the result string from the query
         * @return the result string
         */
        public String getResult() {
                return result;
        }

        /**
         * Set the query string
         * @param query the query string
         */
        public void setQuery(String query) {
                this.query = query;
        }

        /**
         * Set the result string
         * @param result the result string
         */
        public void setResult(String result) {
                this.result = result;
        }

        /**
         * Set the query/result string pair
         * @param query the query string
         * @param result the result string
         */
        public void setPair(String query, String result) {
                setQuery(query);
                setResult(result);
        }

        /**
         * Returns the string representation of a query/result pair
         * @return the query/result pair as a string
         */
        public String toString() {
                return "Query: ["+ query + "]\tResult: ["+ result +"]";
        }
}