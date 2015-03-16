package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.exception.uQasarException;

/**
 * Interface for accessing metric data using a query
 */
interface Requester {
  /**
   * @param query The query to use when fetching data
   * @param login The login to use when authentificating by SonarQube
   * @param passwd The password to use when authentificating by SonarQube
   */
  String fetch(String query, String login, String passwd) throws uQasarException;
}
