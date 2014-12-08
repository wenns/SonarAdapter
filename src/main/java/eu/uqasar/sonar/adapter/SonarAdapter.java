package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.SystemAdapter;
import eu.uqasar.adapter.exception.uQasarException;
import eu.uqasar.adapter.model.BindedSystem;
import eu.uqasar.adapter.model.Measurement;
import eu.uqasar.adapter.model.User;
import eu.uqasar.adapter.model.uQasarMetric;
import eu.uqasar.adapter.query.QueryExpression;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@inheritDoc}
 */
public class SonarAdapter implements SystemAdapter {
  private static final Map<String, String> UQMETRIC_TO_SONARMETRIC = new TreeMap<String, String>() {
    {
      put("NCLOC", "ncloc");
      put("LINES", "lines");
      put("STATEMENTS", "statements");
      put("CLASSES", "classes");
      put("FILES", "files");
      put("DIRECTORIES", "directories");
      put("FUNCTIONS", "functions");
      put("COMMENT_LINES_DENSITY", "comment_lines_density");
      put("COMMENT_LINES", "comment_lines");
      put("DUPLICATED_LINES", "duplicated_lines");
      put("DUPLICATED_LINES_DENSITY", "duplicated_lines_density");
      put("COMPLEXITY", "complexity");
      put("FUNCTION_COMPLEXITY", "function_complexity");
      put("FILE_COMPLEXITY", "file_complexity");
      put("CLASS_COMPLEXITY", "class_complexity");
      put("VIOLATIONS", "violations");
      put("UT_COVERAGE", "coverage");
      put("AT_COVERAGE", "it_coverage");
      put("OVERALL_COVERAGE", "overall_coverage");
      put("PACKAGE_TANGLE_INDEX", "package_tangle_index");
      put("PACKAGE_TANGLES", "package_tangles");
      put("TEST_SUCCESS_DENSITY", "test_success_density");
      put("TEST_FAILURES", "test_failures");
      put("TEST_ERRORS", "test_errors");
      put("TESTS", "tests");
    }
  };

  private static final Map<String, uQasarMetric> UQM_NAME_TO_VALUE = new TreeMap<String, uQasarMetric>() {
    {
      for (uQasarMetric uqm : uQasarMetric.values()) {
        put(uqm.name(), uqm);
      }
    }
  };

  private static final List<String> FIELDS_TO_STRIP =
      Arrays.asList("id", "scope", "qualifier", "creationDate",
          "lname", "lang", "version", "description", "date");

  private Requester requester;

  /**
   * The one and only way to create a SonarAdapter
   */
  public SonarAdapter() {
    requester = new DefaultRequester();
  }

  /**
   * Inject the given requester to be used instead of the default one.
   */
  public void injectRequester(Requester requester) {
    this.requester = requester;
  }

  @Override
  public List<Measurement> query(BindedSystem system, User user, QueryExpression expr) throws uQasarException {
    LinkedList<Measurement> measurements = new LinkedList<Measurement>();
    String query = system.getUri();
    if(!query.endsWith("/")){
      query += "/";
    }
    query += "api/resources?metrics=";

    List<String> metricsToQuery = Arrays.asList(expr.getQuery().split(","));
    for (String metric : metricsToQuery) {
      String responce = querySonar(query + mapMetricName(metric));

      JSONArray jsonArray = new JSONArray(responce);
      JSONArray res = new JSONArray();

      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jb = stripJSON(jsonArray.getJSONObject(i));
        res.put(jb);
      }

      measurements.add(new Measurement(UQM_NAME_TO_VALUE.get(metric), res.toString()));
    }

    return measurements;
  }

  @Override
  public List<Measurement> query(String url, String credentials, String expr) throws uQasarException {
    String username = null;
    String passwd = null;
    if (credentials != null && !credentials.equals("")) {
      String[] creds = credentials.split(":");
      username = creds[0];
      passwd = creds[1];
    }

    return query(new BindedSystem(0, url, 0), new User(username, passwd), new SonarQueryExpression(expr));
  }

  private JSONObject stripJSON(JSONObject sonarJson) {
    // strip all sonar specific, unnecessary stuff from the json object
    for (String key : FIELDS_TO_STRIP) {
      sonarJson.remove(key);
    }

    JSONArray measures = sonarJson.getJSONArray("msr");
    if (measures != null) {
      sonarJson.put("value", measures.getJSONObject(0).get("val"));
      sonarJson.remove("msr");
    }

    return sonarJson;
  }

  private String mapMetricName(String metric) throws uQasarException {
    String sonarMetricName = UQMETRIC_TO_SONARMETRIC.get(metric);
    if (sonarMetricName == null) {
      throw new uQasarException(String.format("The UASAR metric %s is unknown in SonarQube", metric));
    }
    return sonarMetricName;
  }

  private String querySonar(String query) throws uQasarException {
    return requester.fetch(query);
  }

  private static String usage() {
    return "Usage: java -jar SonarAdapter-1.0.jar <url> <metric>";
  }

  /**
   * This implements a rudimentary command line access (for testing and
   * demostrating purposes)
   */
  public static void main(String[] argv) {
    if (argv.length != 2) {
      System.out.println(usage());
      System.exit(1);
    }

    String url = argv[0];
    String metric = argv[1];

    SonarAdapter adapter = new SonarAdapter();
    try {
      List<Measurement> result = adapter.query(url, null, metric);
      System.out.println("** Query successfull, result: **");
      System.out.println(result.get(0));
    } catch (uQasarException u) {
      System.out.println("Error quering Sonar: " + u);
    }
  }
}
