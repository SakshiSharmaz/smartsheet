package Smartsheet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
/**
 * TODO:
 * 1. updating the assignees of issues
 * 2. getting and formatting information in a standardized way
 */

/** Hello world! */
public class App {
  public static void main(String[] args) throws IOException, GeneralSecurityException {

    System.out.println("Hello World!");

    int lastIssueRegistered = 1 ;
    while(true){
      ArrayList<String> issueInfo = new ArrayList<>();
      /*
     * Step 1 : Make Http call to github graphql server
     */
    CloseableHttpClient httpClientForGraphql = null;
    CloseableHttpResponse httpResponseFromGraphql = null;

    httpClientForGraphql = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost("https://api.github.com/graphql");

    /*
     * Step 1 : Create Graphql Request
     */
    String json2 =
        "{\"query\":\"{\\n  repository(name: \\\"Assignment\\\", owner: \\\"perk-y\\\") {\\n    issues(first: 10) {\\n      nodes {\\n        id\\n        title\\n        url\\n        number\\n        assignees(first: 10) {\\n          nodes {\\n            name\\n            id\\n          }\\n        }\\n      }\\n    }\\n  }\\n}\\n\",\"variables\":null}";
  String json3 = "{\"query\":\"{\\n  repository(name: \\\"Assignment\\\", owner: \\\"perk-y\\\") {\\n    issue(number: " + lastIssueRegistered +
          ") {\\n      id\\n      number\\n      title\\n      assignees(first: 10) {\\n        nodes {\\n          name\\n        }\\n      }\\n    }\\n  }\\n}\\n\",\"variables\":null}";
    httpPost.addHeader("Authorization", "token 3844803b440c42bf56225d4738d5a579f019550a");

    try {

      /*
       * Step 3 : Execute Http call
       */
      StringEntity params = new StringEntity(json3);

      httpPost.addHeader("content-type", "application/x-www-form-urlencoded");
      httpPost.setEntity(params);
      httpResponseFromGraphql = httpClientForGraphql.execute(httpPost);


      /*
       * Step 3 : Convert the response to String
       */

      BufferedReader streamReader =
          new BufferedReader(
              new InputStreamReader(httpResponseFromGraphql.getEntity().getContent(), "UTF-8"));
      StringBuilder responseStrBuilder = new StringBuilder();
      String inputStr;
      while ((inputStr = streamReader.readLine()) != null) responseStrBuilder.append(inputStr);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(responseStrBuilder.toString());

      JsonNode issue = rootNode.get("data").get("repository").get("issue");
      if(issue.get("number") == null){
        Thread.sleep(10000);
        continue;
      }

        issueInfo.add(issue.get("number").asText());
        issueInfo.add(issue.get("title").asText());
        Iterator<JsonNode> assignees = issue.get("assignees").get("nodes").elements();
        while ((assignees.hasNext())) {
          JsonNode assignee = assignees.next();
          if(assignee.get("name") != null)
          issueInfo.add(assignee.get("name").asText());
        }

      /*
       * Step 4 : Add issue info to Google sheets
       */

      SheetsQuickstart sheetService = new SheetsQuickstart();
      sheetService.updateValues("1IQ6cMyDz123GuJEUYWysNt73_UGoYxaBG0idq6xoV3U", "RAW", issueInfo);
      lastIssueRegistered++;
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    }
  }
}
