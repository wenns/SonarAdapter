package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.exception.uQasarException;
import eu.uqasar.adapter.model.Measurement;
import eu.uqasar.adapter.model.uQasarMetric;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertTrue;

public class SonarAdapterTest {
  SonarAdapter adapter = new SonarAdapter("localhost", "9000");
  
  @Test
  public void queryingExistingThingsShouldWork() throws uQasarException{
    String projName = "org.codehaus.sonar-plugins.cxx:cxx";
    
    Map<uQasarMetric, String> input_2_expectedOutput = new TreeMap<uQasarMetric, String>();
    input_2_expectedOutput.put(uQasarMetric.NCLOC, "100");
    input_2_expectedOutput.put(uQasarMetric.STATEMENTS, "100");
    input_2_expectedOutput.put(uQasarMetric.DUPLICATED_LINES, "100");
    input_2_expectedOutput.put(uQasarMetric.DUPLICATED_LINES_DENSITY, "100");
    input_2_expectedOutput.put(uQasarMetric.COMPLEXITY, "100");
    input_2_expectedOutput.put(uQasarMetric.UT_COVERAGE, "100");
    //input_2_expectedOutput.put(uQasarMetric.AT_COVERAGE, "100");
    input_2_expectedOutput.put(uQasarMetric.TEST_SUCCESS_DENSITY, "100");
    
    for (Map.Entry<uQasarMetric, String> entry : input_2_expectedOutput.entrySet()) {
      //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
      List<Measurement> result = adapter.query(projName, entry.getKey());
      //assertEquals(result.get(0), new Measurement(entry.getKey(), entry.getValue()));
      System.out.println(result);
    }

    // List<Measurement> result = adapter.query(projName, uQasarMetric.LOC);
    // assertEquals(result.get(0), new Measurement(uQasarMetric.LOC, new String("52")));
  }

  //Whats the behavior in the case "no current measure for this metric available??"
  
  // @Test(expected = uQasarException.class)
  // public void queryingNotexistentProjectShouldThrow() throws uQasarException{
  //   List<Measurement> result = adapter.query("Abrakadabra", uQasarMetric.NCLOC);
  // }

  @Test(expected = uQasarException.class)
  public void queryingNotexistentProjectShouldThrow() throws uQasarException{
    List<Measurement> result = adapter.query("Abrakadabra", uQasarMetric.NCLOC);
  }

  @Test(expected = uQasarException.class)
  public void queryingUnknownMetricShouldThrow() throws uQasarException{
    List<Measurement> result = adapter.query("CxxPlugin:Sample", uQasarMetric.PROJECTS_PER_SYSTEM_INSTANCE);
  }

  @Test(expected = uQasarException.class)
  public void queriengUnresolvableSonarInstancesShouldThrow() throws uQasarException{
    SonarAdapter adapter = new SonarAdapter("whereverhost", "9000");
    adapter.query("CxxPlugin:Sample", uQasarMetric.NCLOC);
  }
}
