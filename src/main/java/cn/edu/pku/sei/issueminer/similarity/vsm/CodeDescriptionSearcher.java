package cn.edu.pku.sei.issueminer.similarity.vsm;


import cn.edu.pku.sei.issueminer.extraction.data_tokenizer.DataTokenizer;
import cn.edu.pku.sei.issueminer.extraction.jira_to_neo4j.JiraGraphBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CodeDescriptionSearcher {

    private GraphDatabaseService db;
    private String indexDirPath;
    private final String ID_FIELD = "id";
    private final String CONTENT_FIELD = "content";
    private DirectoryReader iReader;
    private IndexSearcher iSearcher;
    private QueryParser parser;

    public CodeDescriptionSearcher(GraphDatabaseService db,String indexDirPath){
        this.db = db;
        this.indexDirPath = indexDirPath;
    }

    public void createIndex() throws IOException{
        if(new File(indexDirPath).exists())
            return;
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new SimpleFSDirectory(new File(indexDirPath).toPath());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iWriter = new IndexWriter(directory,config);
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Node> nodes = db.getAllNodes().iterator();
            while (nodes.hasNext()){
                Node node = nodes.next();
                String indexStr = getIndexStr(node);
                if(indexStr == null){
                    continue;
                }
                Document document = new Document();
                document.add(new StringField(ID_FIELD,""+node.getId(), Field.Store.YES));
                document.add(new TextField(CONTENT_FIELD,indexStr,Field.Store.NO));
                iWriter.addDocument(document);

            }
            tx.success();
        }
        iWriter.close();

    }

    public List<String> search(String querystring) throws IOException, ParseException{

        createIndex();
        List<String> searchresult = new ArrayList<>();

        Directory directory = new SimpleFSDirectory(new File(indexDirPath).toPath());
        Analyzer analyzer = new StandardAnalyzer();
        iReader = DirectoryReader.open(directory);
        iSearcher = new IndexSearcher(iReader);
        parser = new QueryParser(CONTENT_FIELD, analyzer);
        Query query = parser.parse(querystring);
        ScoreDoc[] hits = iSearcher.search(query,1000).scoreDocs;
        try(Transaction tx = db.beginTx()){
            int count = 0;
            for(int i = 0;i<hits.length;i++){

                Document doc = iReader.document(hits[i].doc);
                long id = Long.parseLong(doc.getField(ID_FIELD).stringValue());
                Node node = db.getNodeById(id);
                if(node.hasLabel(JiraGraphBuilder.ISSUE)){
                    String issueid = (String) node.getProperty(JiraGraphBuilder.ISSUE_NAME);
                    String issuetext = (String)node.getProperty(JiraGraphBuilder.ISSUE_SUMMARY);
                    Iterator<Relationship> rels = node.getRelationships(JiraGraphBuilder.HAVE_PATCH,Direction.OUTGOING).iterator();
                    if(rels.hasNext()){
                        searchresult.add(issueid + "-"+issuetext);
                    }
                    else{
                        searchresult.add("NO_PATCH-"+issueid+"-"+issuetext);
                    }
                    count++;

                }
                if(count ==10)
                    break;
            }
            tx.success();
        }
        return searchresult;
    }

    public String getIndexStr(Node node){
        String issuetext = "";
        if(node.hasLabel(JiraGraphBuilder.ISSUE))
        {
            String issue_title = StringUtils.join(DataTokenizer.tokenization((String) node.getProperty(JiraGraphBuilder.ISSUE_SUMMARY))," ");
            String issue_description = StringUtils.join(DataTokenizer.tokenization((String) node.getProperty(JiraGraphBuilder.ISSUE_DESCRIPTION)),"");
            issuetext = issue_title + issue_description;
        }
        if(node.hasLabel(JiraGraphBuilder.ISSUECOMMENT))
        {
            String issue_comment = StringUtils.join(DataTokenizer.tokenization((String) node.getProperty(JiraGraphBuilder.ISSUECOMMENT_BODY))," ");
            issuetext += issue_comment;
        }
        return issuetext;
    }




}
