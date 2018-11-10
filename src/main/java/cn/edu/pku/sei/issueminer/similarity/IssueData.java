package cn.edu.pku.sei.issueminer.similarity;


import cn.edu.pku.sei.issueminer.extraction.jira_to_neo4j.JiraGraphBuilder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.*;

public class IssueData {


    public static Map<String,String> getIssueTitle(String graphDir){
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));
        Map<String,String> issuedata = new HashMap<>();
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Node> nodes = db.findNodes(JiraGraphBuilder.ISSUE);
            while(nodes.hasNext()){
                Node node = nodes.next();
                if(node.getProperty(JiraGraphBuilder.ISSUE_TYPE).equals("New Feature")){
                    String id = (String) node.getProperty(JiraGraphBuilder.ISSUEUSER_NAME);
                    String title = (String) node.getProperty(JiraGraphBuilder.ISSUE_SUMMARY);
                    String description = (String)node.getProperty(JiraGraphBuilder.ISSUE_DESCRIPTION);
                    issuedata.put(id,title+". "+description);
                }
            }
            tx.success();
        }
        return issuedata;
    }


    public static void main(String[] args) {
        Map<String, String> map1;
        map1 = IssueData.getIssueTitle("F:\\Apache\\GraphDataBase\\Graph-Lucene");
        int count = 0;
        for (Map.Entry<String, String> entry : map1.entrySet()) {
            count++;
            System.out.println(entry.getKey() + "  ---  " + entry.getValue());
            if(count == 100)
                break;

        }
    }
}
