package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.exception.uQasarException;

/**
 * Interface for accessing metric data using a query
 */
interface Requester {
  /**
   * @param query The query to use when fetching data
   */
  String fetch(String query) throws uQasarException;
}
