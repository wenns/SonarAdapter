package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.exception.uQasarException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * That one implements fetching data from Sonar using HTTP
 */
public class DefaultRequester implements Requester {
  /**
   * {@inheritDoc}
   */
  public String fetch(String query) throws uQasarException {
    String result;
    URL url = null;
    try {
      url = new URL(query);
    } catch (java.net.MalformedURLException e) {
      throw new uQasarException("The query is invalid, details: ", e);
    }

    try {
      InputStream is = url.openStream();
      try {
        result = IOUtils.toString(is, "UTF-8");
      } finally {
        is.close();
      }
    } catch (IOException oie) {
      throw new uQasarException("Reading the responce failed, details: ", oie);
    }

    return result;
  }
}
