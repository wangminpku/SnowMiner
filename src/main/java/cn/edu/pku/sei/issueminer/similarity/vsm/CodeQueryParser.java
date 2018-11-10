package cn.edu.pku.sei.issueminer.similarity.vsm;

import cn.edu.pku.sei.issueminer.extraction.data_tokenizer.DataTokenizer;
import cn.edu.pku.sei.issueminer.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodeQueryParser {

    private GraphDatabaseService db;

    public CodeQueryParser(GraphDatabaseService db){
        this.db = db;
    }

    public List<String> getCodeElement(String query){
        List<String> codeElementList = new ArrayList<>();
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Node> nodes = db.getAllNodes().iterator();
            while(nodes.hasNext()){
                Node node = nodes.next();
                if(node.hasLabel(JavaCodeGraphBuilder.METHOD) || node.hasLabel(JavaCodeGraphBuilder.CLASS)||node.hasLabel(JavaCodeGraphBuilder.FIELD)){
                    String name = (String)node.getProperty(JavaCodeGraphBuilder.NAME);
                    List<String> codeelelist = DataTokenizer.englishTokenization(name);
                    String nameLower = name.toLowerCase();
                    if(query.toLowerCase().contains(nameLower)){
                        //codeElementList.addAll(codeelelist);
                        codeElementList.add(name);
                    }
                }
            }
            tx.success();
        }
        return codeElementList;
    }

    public String getVsmQuery(String query){
        return query.replaceAll("[^A-Z^a-z^0-9 ]"," ");
    }

    public static void main(String[] args){

    }



}
