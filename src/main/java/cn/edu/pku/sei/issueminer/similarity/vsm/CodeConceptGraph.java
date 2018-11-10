package cn.edu.pku.sei.issueminer.similarity.vsm;

import cn.edu.pku.sei.issueminer.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import org.neo4j.graphdb.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CodeConceptGraph {

    private GraphDatabaseService db;

    public CodeConceptGraph(GraphDatabaseService db){
        this.db = db;
    }


    public Set<Node> getSubGraphNode(String methondname){

        Set<Node> nodeSet = new HashSet<>();

        try(Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = db.findNodes(JavaCodeGraphBuilder.METHOD);
            while (nodes.hasNext()) {
                Node node = nodes.next();
                String name = (String) node.getProperty(JavaCodeGraphBuilder.NAME);
                if (name.toLowerCase().equals(methondname)) {
                    nodeSet.add(node);
                    //他是哪个类的函数
                    Iterator<Relationship> rels = node.getRelationships(JavaCodeGraphBuilder.HAVE_METHOD, Direction.INCOMING).iterator();
                    while (rels.hasNext()) {
                        Relationship rel = rels.next();
                        Node classNode = rel.getStartNode();
                        nodeSet.add(classNode);
                    }
                    //他调用了谁
                    Iterator<Relationship> rels2 = node.getRelationships(JavaCodeGraphBuilder.METHOD_CALL, Direction.OUTGOING).iterator();
                    while (rels2.hasNext()) {
                        Relationship rel = rels2.next();
                        Node mNode = rel.getEndNode();
                        nodeSet.add(mNode);
                    }
                    //谁调用了他
                    Iterator<Relationship> rels3 = node.getRelationships(JavaCodeGraphBuilder.METHOD_CALL, Direction.INCOMING).iterator();
                    while (rels3.hasNext()) {
                        Relationship rel = rels3.next();
                        Node mNode = rel.getStartNode();
                        nodeSet.add(mNode);
                    }
                }
            }
            tx.success();
        }
        return nodeSet;

    }
}
