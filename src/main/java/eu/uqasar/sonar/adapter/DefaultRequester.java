package eu.uqasar.sonar.adapter;

import java.io.InputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.net.URL;
import eu.uqasar.adapter.exception.uQasarException;

public class DefaultRequester implements Requester{
  public String fetch(String query) throws uQasarException{
    String result;
    URL url;
    //System.out.println(query);
    try{
      url = new URL(query);
    } catch(java.net.MalformedURLException e){
      // TODO: encapsulate the original exception
      throw new uQasarException("query is invalid");
    }
    
    try{
      InputStream is = url.openStream();
      try {
        result = IOUtils.toString(is, "UTF-8");
      } finally {
        is.close();
      }
    } catch(IOException oie) {
      // TODO: encapsulate the original exception
      throw new uQasarException("querying failed");
    }
    
    // System.out.println(query);
    // System.out.println("--->");
    // System.out.println("!!" + result);
    
    return result;
  }
}
