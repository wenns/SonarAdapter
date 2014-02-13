package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.exception.uQasarException;
import eu.uqasar.adapter.model.Measurement;
import eu.uqasar.adapter.model.uQasarMetric;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Before;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Matchers.matches;

public class SonarAdapterTest {
  SonarAdapter adapter;
  String projName = "org.codehaus.sonar-plugins.cxx:cxx";
  String host = "localhost";
  String port = "9000";
  
  private Requester mockRequester() throws uQasarException, IOException{
    Requester mockedRequester = mock(Requester.class);
    when(mockedRequester
         .fetch(matches(".*" + projName + ".*metrics=ncloc")))
      .thenReturn(fromResource("ncloc_response.json"));
    when(mockedRequester
         .fetch(matches(".*" + projName + ".*metrics=it_coverage")))
      .thenReturn("[]");
    when(mockedRequester
         .fetch(matches(".*" + projName + ".*metrics=statements")))
      .thenReturn(fromResource("statements_response.json"));
    when(mockedRequester
         .fetch(matches(".*" + projName + ".*metrics=duplicated_lines")))
      .thenReturn(fromResource("duplicated_lines_response.json"));
    when(mockedRequester
         .fetch(matches(".*" + projName + ".*metrics=duplicated_lines_density")))
      .thenReturn(fromResource("duplicated_lines_density_response.json"));
    when(mockedRequester
         .fetch(matches(".*" + projName + ".*metrics=complexity")))
      .thenReturn(fromResource("complexity_response.json"));
    when(mockedRequester
         .fetch(matches(".*" + projName + ".*metrics=coverage")))
      .thenReturn(fromResource("coverage_response.json"));
    when(mockedRequester
         .fetch(matches(".*" + projName + ".*metrics=test_success_density")))
      .thenReturn(fromResource("test_success_density_response.json"));

    // when(mockedRequester
    //  .fetch(matches(".*lala.*")))
    //.fetch("http://localhost:9000/api/resources?resource=bad_project&metrics=ncloc"))
    //     .fetch(matches(".*lala.*")))
    // .thenThrow(new uQasarException(""));
    
    // when(requester
    //      //.fetch(matches(".*bad_project.*")))
    //      .fetch("http://localhost:9000/api/resources?resource=bad_project&metrics=ncloc"))
    //   .thenThrow(new uQasarException(""));

    
    return mockedRequester;
  }

  @Before
  public void setUp(){
    adapter = new SonarAdapter("localhost", "9000");
    Requester mockedRequester = null;
    try{
      mockedRequester = mockRequester();
      adapter.injectRequester(mockedRequester);
    } catch(Exception e) {
      System.out.println("Something went wrong while stubbing the requester, Details: " + e);
    }
  }
  
  public String fromResource(String resourceName) throws IOException{
    String result;
    URL resource = SonarAdapterTest.class.getResource(resourceName);

    InputStream is = resource.openStream();
    try {
      result = IOUtils.toString(is, "UTF-8");
    } finally {
      is.close();
    }
    
    return result;
  }
  
  @Test
  public void queryingExistingThingsShouldWork() throws uQasarException{  
    Map<uQasarMetric, String> input_2_expectedOutput = new TreeMap<uQasarMetric, String>();
    input_2_expectedOutput.put(uQasarMetric.NCLOC, "6251");
    input_2_expectedOutput.put(uQasarMetric.STATEMENTS, "2061");
    input_2_expectedOutput.put(uQasarMetric.DUPLICATED_LINES, "133");
    input_2_expectedOutput.put(uQasarMetric.DUPLICATED_LINES_DENSITY, "1.4");
    input_2_expectedOutput.put(uQasarMetric.COMPLEXITY, "946");
    input_2_expectedOutput.put(uQasarMetric.UT_COVERAGE, "90.5");
    input_2_expectedOutput.put(uQasarMetric.TEST_SUCCESS_DENSITY, "100");
    
    for (Map.Entry<uQasarMetric, String> entry : input_2_expectedOutput.entrySet()) {
      List<Measurement> result = adapter.query(projName, entry.getKey());
      assertEquals(result.get(0), new Measurement(entry.getKey(), entry.getValue()));
    }
  }

  @Test
  public void queringUnmeasuredMetrikShouldReturnNull() throws uQasarException{
    List<Measurement> result = adapter.query(projName, uQasarMetric.AT_COVERAGE);
    assertEquals(result.get(0), new Measurement(uQasarMetric.AT_COVERAGE, null));
  }

  // @Test//(expected = uQasarException.class)
  // public void queryingNotexistentProjectShouldThrow() throws uQasarException{
  //   //public void queryingNotexistentProjectShouldThrow() {
  //   List<Measurement> result = adapter.query("lala", uQasarMetric.NCLOC);
  //   //System.out.println(result);
  // }
  
  // @Test(expected = uQasarException.class)
  // public void queryingUnknownMetricShouldThrow() throws uQasarException{
  //   List<Measurement> result = adapter.query(projName, uQasarMetric.PROJECTS_PER_SYSTEM_INSTANCE);
  // }

  // @Test(expected = uQasarException.class)
  // public void queriengUnresolvableSonarInstancesShouldThrow() throws uQasarException{
  //   SonarAdapter adapter = new SonarAdapter("bad_host", "9000");
  //   adapter.query(projName, uQasarMetric.NCLOC);
  // }
}
