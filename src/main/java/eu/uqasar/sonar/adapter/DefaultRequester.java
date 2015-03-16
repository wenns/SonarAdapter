package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.exception.uQasarException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;

/**
 * That one implements fetching data from Sonar using HTTP
 */
public class DefaultRequester implements Requester {
  /**
   * {@inheritDoc}
   */
  public String fetch(String query, String login, String passwd) throws uQasarException {
    String result;
    URL url = null;
    URLConnection urlConnection = null;
    try {
      String authString = login + ":" + passwd;
      authString = new String(Base64.encodeBase64(authString.getBytes()));
      url = new URL(query);
      urlConnection = url.openConnection();
      urlConnection.setRequestProperty("Authorization", "Basic " + authString);
    } catch (java.net.MalformedURLException e) {
      throw new uQasarException("The query is invalid, details: ", e);
    } catch (IOException e) {
      throw new uQasarException("IO Error while opening connection, details: ", e);
    }

    try {
      InputStream is = urlConnection.getInputStream();
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
