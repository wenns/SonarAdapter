package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.SystemAdapter;
import eu.uqasar.adapter.exception.uQasarException;
import eu.uqasar.adapter.model.*;
import eu.uqasar.adapter.query.QueryExpression;
import eu.uqasar.adapter.model.BindedSystem;
import java.net.URI;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONObject;
import org.json.JSONArray;

public class SonarAdapter implements SystemAdapter {
  private String host;
  private String port;
  private String queryTemplate;
  static final Map<uQasarMetric, String> UQMETRIC_TO_SONARMETRIC
    = new TreeMap<uQasarMetric, String>() {{
      put(uQasarMetric.NCLOC, "ncloc");
      put(uQasarMetric.STATEMENTS, "statements");
      put(uQasarMetric.DUPLICATED_LINES, "duplicated_lines");
      put(uQasarMetric.DUPLICATED_LINES_DENSITY, "duplicated_lines_density");
      put(uQasarMetric.COMPLEXITY, "complexity");
      put(uQasarMetric.UT_COVERAGE, "coverage");
      put(uQasarMetric.AT_COVERAGE, "it_coverage");
      put(uQasarMetric.TEST_SUCCESS_DENSITY, "test_success_density");
    }};
  
  private Requester requester = new DefaultRequester();
  
  public SonarAdapter(String host, String port) {
    this.host = host;
    this.port = port;
    queryTemplate = "http://" + host + ":" + port + "/api/resources?resource=";
  }

  public void injectRequester(Requester requester){
    this.requester = requester;
  }
  
  @Override
  public List<Measurement> query(BindedSystem bindedSystem, User user, QueryExpression queryExpression) throws uQasarException {
    URI uri = null;
    LinkedList<Measurement> measurements = new LinkedList<Measurement>();
    return measurements;
  }
  
  @Override
  public List<Measurement> query(String url, String credentials, String queryExpression) throws uQasarException {
    BindedSystem sonarInst = new BindedSystem(0, url, 0);
    
    String[] creds = credentials.split(":");
    User user = new User(creds[0], creds[1]);
    
    return query(sonarInst, user, new SonarQueryExpression(queryExpression));
  }
  
  private String mapMetricName(uQasarMetric metric) throws uQasarException{
    String sonarMetricName = UQMETRIC_TO_SONARMETRIC.get(metric);
    if(sonarMetricName == null){
      throw new uQasarException(String.format("The UASAR metric %s is unknown in SonarQube", metric));
    }
    return sonarMetricName;
  }
  
  private String querySonar(String query) throws uQasarException{
    return requester.fetch(query);
  }

  private static String usage(){
    return "Usage: <callable> <hostname> <port> <project name> <metric>";
  }
  
  public static void main(String[] argv){
    System.out.println("called!");
    
    if(argv.length != 4){
      System.out.println(usage());
      System.exit(1);
    }
    
    String hostname = argv[0];
    String port = argv[1];
    String projectName = argv[2];
    String metric = argv[3];
    
    // SonarAdapter adapter = new SonarAdapter(hostname, port);
    // try{
    //   List<Measurement> result = adapter.query(projectName, uQasarMetric.NCLOC);
    //   System.out.println(result);
    // } catch(uQasarException u){
    //   System.out.println("BOOM!: " + u);
    // }
  }
}
