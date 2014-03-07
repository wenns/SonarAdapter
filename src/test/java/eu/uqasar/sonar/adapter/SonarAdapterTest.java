package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.exception.uQasarException;
import eu.uqasar.adapter.model.Measurement;
import eu.uqasar.adapter.model.uQasarMetric;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SonarAdapterTest {
  SonarAdapter adapter;
  String sonarUrl = "http://localhost:9000";

  private Requester mockThrowingRequester() {
    Requester requester = new Requester() {
      public String fetch(String query) throws uQasarException {
        throw new uQasarException("some message");
      }
    };
    return requester;
  }

  private Requester mockEmptySonar() throws uQasarException {
    Requester mockedRequester = mock(Requester.class);
    when(mockedRequester.fetch(anyString())).thenReturn("[]");
    return mockedRequester;
  }

  private Requester mockFullSonar() throws uQasarException, IOException {
    Requester mockedRequester = mock(Requester.class);

    when(mockedRequester
        .fetch(matches(".*metrics=it_coverage")))
        .thenReturn("[]"); // that one is unmeasured
    when(mockedRequester
        .fetch(matches(".*metrics=ncloc")))
        .thenReturn(fromResource("ncloc_response.json"));
    when(mockedRequester
        .fetch(matches(".*metrics=statements")))
        .thenReturn(fromResource("statements_response.json"));
    when(mockedRequester
        .fetch(matches(".*metrics=duplicated_lines")))
        .thenReturn(fromResource("duplicated_lines_response.json"));
    when(mockedRequester
        .fetch(matches(".*metrics=duplicated_lines_density")))
        .thenReturn(fromResource("duplicated_lines_density_response.json"));
    when(mockedRequester
        .fetch(matches(".*metrics=complexity")))
        .thenReturn(fromResource("complexity_response.json"));
    when(mockedRequester
        .fetch(matches(".*metrics=coverage")))
        .thenReturn(fromResource("coverage_response.json"));
    when(mockedRequester
        .fetch(matches(".*metrics=test_success_density")))
        .thenReturn(fromResource("test_success_density_response.json"));

    return mockedRequester;
  }

  @Before
  public void setUp() {
    adapter = new SonarAdapter();

    Requester mockedRequester = null;
    try {
      mockedRequester = mockFullSonar();
      adapter.injectRequester(mockedRequester);
    } catch (Exception e) {
      System.out.println("Something went wrong while stubbing the requester, Details: " + e);
    }
  }

  public String fromResource(String resourceName) throws IOException {
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
  public void queryingExistingThingsShouldWork() throws uQasarException {
    Map<uQasarMetric, String> input_2_expectedOutput = new TreeMap<uQasarMetric, String>();

    input_2_expectedOutput.put(uQasarMetric.NCLOC,
        "[{\"name\":\"Cxx\",\"value\":6251,\"key\":\"org.codehaus.sonar-plugins.cxx:cxx\"}]"
        );
    input_2_expectedOutput.put(uQasarMetric.STATEMENTS,
        "[{\"name\":\"Cxx\",\"value\":2061,\"key\":\"org.codehaus.sonar-plugins.cxx:cxx\"}]"
        );
    input_2_expectedOutput.put(uQasarMetric.DUPLICATED_LINES,
        "[{\"name\":\"Cxx\",\"value\":133,\"key\":\"org.codehaus.sonar-plugins.cxx:cxx\"}]"
        );
    input_2_expectedOutput.put(uQasarMetric.DUPLICATED_LINES_DENSITY,
        "[{\"name\":\"Cxx\",\"value\":1.4,\"key\":\"org.codehaus.sonar-plugins.cxx:cxx\"}]"
        );
    input_2_expectedOutput.put(uQasarMetric.COMPLEXITY,
        "[{\"name\":\"Cxx\",\"value\":946,\"key\":\"org.codehaus.sonar-plugins.cxx:cxx\"}]"
        );
    input_2_expectedOutput.put(uQasarMetric.UT_COVERAGE,
        "[{\"name\":\"Cxx\",\"value\":90.5,\"key\":\"org.codehaus.sonar-plugins.cxx:cxx\"}]"
        );
    input_2_expectedOutput.put(uQasarMetric.TEST_SUCCESS_DENSITY,
        "[{\"name\":\"Cxx\",\"value\":100,\"key\":\"org.codehaus.sonar-plugins.cxx:cxx\"}]"
        );

    for (Map.Entry<uQasarMetric, String> entry : input_2_expectedOutput.entrySet()) {
      List<Measurement> result = adapter.query(sonarUrl, null, entry.getKey().name());
      assertEquals("A query of a should-be-exising measure returned an unexpected responce",
          new Measurement(entry.getKey(), entry.getValue()), result.get(0));
    }
  }

  @Test
  public void queringUnmeasuredMetrikShouldReturnNothing() throws uQasarException {
    List<Measurement> result = adapter.query(sonarUrl, null, "AT_COVERAGE");

    // currently, we cannot differentiate the situations 'there are no projects'
    assertEquals("Quering unmeasured metrics should return an empty result",
        result.get(0), new Measurement(uQasarMetric.AT_COVERAGE, "[]"));
  }

  @Test
  public void queringAnEmptySonarInstanceReturnsNothing() throws uQasarException {
    adapter.injectRequester(mockEmptySonar());
    List<Measurement> result = adapter.query(sonarUrl, null, "NCLOC");

    // currently, we cannot differentiate the situations 'there are no projects'
    assertEquals("Quering empty Sonar instance should return an empty result",
        result.get(0), new Measurement(uQasarMetric.NCLOC, "[]"));
  }

  @Test(expected = uQasarException.class)
  public void queryingUnknownMetricShouldThrow() throws uQasarException {
    List<Measurement> result = adapter.query(sonarUrl, null, "zyz");
  }

  @Test(expected = uQasarException.class)
  public void queriengUnresolvableSonarInstancesShouldThrow() throws uQasarException {
    adapter.injectRequester(mockThrowingRequester());
    adapter.query("habbadubbada", null, "NCLOC");
  }
}
