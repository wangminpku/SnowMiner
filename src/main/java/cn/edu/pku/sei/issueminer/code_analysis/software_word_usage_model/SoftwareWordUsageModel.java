package cn.edu.pku.sei.issueminer.code_analysis.software_word_usage_model;

import cn.edu.pku.sei.issueminer.extraction.javacode_to_neo4j.JavaCodeGraphBuilder;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.neo4j.graphdb.*;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoftwareWordUsageModel {


    private GraphDatabaseService db;
    public static void main(String[] args){

    }

    public SoftwareWordUsageModel(GraphDatabaseService db){
        this.db = db;
    }


    public Set<Node> getSubGraphNode(String methondname){

        Set<Node> nodeSet = new HashSet<>();

        try(Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = db.findNodes(JavaCodeGraphBuilder.METHOD);
            while (nodes.hasNext()) {
                Node node = nodes.next();
                String name = (String) node.getProperty(JavaCodeGraphBuilder.NAME);
                if (name.equals(methondname)) {
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

    public static String extractNaturalPhrasesFromSig(Boolean isConstructor , String returnType , List<String> methodName, List<String> params ,String comments){
        String naturalPhrases = "";
        if(!isConstructor){
            methodName.remove(0);
            if(returnType.equals("boolean")) {
                if(!methodName.toString().contains("is")){
                    naturalPhrases = comments.replaceAll("[^(^)^>^<^,^'^!^=^\\-^0-9^a-z^A-Z ]","");
                    return naturalPhrases;
                }
                else{
                    if(params.isEmpty()){
                        if(methodName.size() == 2){
                            naturalPhrases = "check " + methodName.get(1) + ", and return true if is" + methodName.get(1);
                            return naturalPhrases;
                        }
                        else if(methodName.size() == 3){
                            naturalPhrases = "check " + methodName.get(1) +" " + methodName.get(2)+", and return true if is " + methodName.get(1) + " "+methodName.get(2);
                            return naturalPhrases;
                        }
                        else{
                            naturalPhrases = "check " + methodName.remove(0).toString().replaceAll("^0-9^a-z ","");
                            return naturalPhrases;
                        }
                    }
                    else{
                        if(methodName.size() == 2 && params.size() == 1){
                            naturalPhrases = "check " + methodName.get(1) + ", and return true if "+ params.get(0) + " is" + methodName.get(1);
                            return naturalPhrases;
                        }
                        else if(methodName.size() == 3){
                            naturalPhrases = "check " + methodName.get(1) +" " + methodName.get(2)+", and return true if "+ params.get(0) + " is " + methodName.get(1) + " "+methodName.get(2);
                            return naturalPhrases;
                        }
                        else{
                            naturalPhrases = "check " + methodName.remove(0).toString().replaceAll("^0-9^a-z ","")+" by "+params.toString();
                            return naturalPhrases;
                        }
                    }
                }
            }

            else if(returnType.equals("String[]")||returnType.equals("int[]")||returnType.equals("char[]")||returnType.equals("float[]")||returnType.equals("double[]")){
                if(params.isEmpty()){
                    String returnNoun=" ";
                    for(String e : methodName){
                        String ePos = getWordPos(e);
                        if(ePos.equals("V")||ePos.equals("VB")){
                            naturalPhrases = e + " "+ naturalPhrases;
                        }
                        else{
                            naturalPhrases = naturalPhrases + " " + e;
                            returnNoun = returnNoun + " " + e;
                        }
                    }
                    naturalPhrases = naturalPhrases + ",and return a " + returnNoun + " array";
                    return naturalPhrases;
                }
                else{
                    if(params.size() == 1){
                        String returnNoun=" ";
                        for(String e : methodName){
                            String ePos = getWordPos(e);
                            if(ePos.equals("V")||ePos.equals("VB")){
                                naturalPhrases = e + " "+ naturalPhrases;
                            }
                            else{
                                naturalPhrases = naturalPhrases + " " + e;
                                returnNoun = returnNoun + " " + e;
                            }
                        }
                        naturalPhrases = naturalPhrases + " from " + params.get(0) + " ,and return a " + returnNoun + " array";
                        return naturalPhrases;
                    }else{
                        String returnNoun=" ";
                        for(String e : methodName){
                            String ePos = getWordPos(e);
                            if(ePos.equals("V")||ePos.equals("VB")){
                                naturalPhrases = e + " "+ naturalPhrases;
                            }
                            else{
                                naturalPhrases = naturalPhrases + " " + e;
                                returnNoun = returnNoun + " " + e;
                            }
                        }
                        naturalPhrases = naturalPhrases + " by " + params.toString() + " ,and return a " + returnNoun + " array";
                        return naturalPhrases;
                    }

                }
            }

            else if(returnType.equals("void") || returnType.equals("String") || returnType.equals("int")||returnType.equals("float")||returnType.equals("double")||returnType.equals("char")){
                if(params.isEmpty()){
                    for(String e : methodName){
                        String ePos = getWordPos(e);
                        if(ePos.equals("V")||ePos.equals("VB")){
                            naturalPhrases = e + " "+ naturalPhrases;
                        }
                        else{
                            naturalPhrases = naturalPhrases + " " + e;
                        }
                    }
                    return naturalPhrases;
                }
                else{
                    if(params.size() == 1){
                        for(String e : methodName){
                            String ePos = getWordPos(e);
                            if(ePos.equals("V")||ePos.equals("VB")){
                                naturalPhrases = e + " "+ naturalPhrases;
                            }
                            else{
                                naturalPhrases = naturalPhrases + " " + e;
                            }
                        }
                        naturalPhrases = naturalPhrases + " from " + params.get(0) ;
                        return naturalPhrases;
                    }else{
                        for(String e : methodName){
                            String ePos = getWordPos(e);
                            if(ePos.equals("V")||ePos.equals("VB")){
                                naturalPhrases = e + " "+ naturalPhrases;
                            }
                            else{
                                naturalPhrases = naturalPhrases + " " + e;
                            }
                        }
                        naturalPhrases = naturalPhrases + " by " + params.toString();
                        return naturalPhrases;
                    }
                }
            }
            else{
                if(params.isEmpty()){
                    for(String e : methodName){
                        String ePos = getWordPos(e);
                        if(ePos.equals("V")||ePos.equals("VB")){
                            naturalPhrases = e + " "+ naturalPhrases;
                        }
                        else{
                            naturalPhrases = naturalPhrases + " " + e;
                        }
                    }
                    return naturalPhrases + " ,and return a " + returnType;
                }
                else{
                    if(params.size() == 1){
                        for(String e : methodName){
                            String ePos = getWordPos(e);
                            if(ePos.equals("V")||ePos.equals("VB")){
                                naturalPhrases = e + " "+ naturalPhrases;
                            }
                            else{
                                naturalPhrases = naturalPhrases + " " + e;
                            }
                        }
                        naturalPhrases = naturalPhrases + " from " + params.get(0) + " ,and return a " + returnType;
                        return naturalPhrases;
                    }else{
                        for(String e : methodName){
                            String ePos = getWordPos(e);
                            if(ePos.equals("V")||ePos.equals("VB")){
                                naturalPhrases = e + " "+ naturalPhrases;
                            }
                            else{
                                naturalPhrases = naturalPhrases + " " + e;
                            }
                        }
                        naturalPhrases = naturalPhrases + " by " + params.toString() + " ,and return a " + returnType;
                        return naturalPhrases;
                    }
                }
            }
        }
        else{
            naturalPhrases = "Construct a " + methodName.get(0);
            return  naturalPhrases;
        }
    }


    public static String getWordPos(String word){
        String wordPos = "";
        Properties pros = new Properties();
        pros.put("annotators","pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP();
        Annotation document = new Annotation(word);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence:sentences){
            for(CoreLabel token:sentence.get(CoreAnnotations.TokensAnnotation.class)){
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                wordPos = pos;
            }
        }
        return wordPos;
    }


    public static List<String> englishTokenization(String word){
        List<String> tokens = new ArrayList<>();
        String[] eles = word.trim().split("[^A-Za-z]+");
        for(String e:eles){
            List<String> humps = camelSplit(e);
            tokens.add(e.toLowerCase());
            if(humps.size()>0)
                tokens.addAll(humps);
        }
        return tokens;
    }

    public static List<String> camelSplit(String e){
        List<String> r = new ArrayList<>();
        Matcher m = Pattern.compile("^([a-z]+)|([A-Z][a-z]+)|([A-Z]+(?=([A-Z]|$)))").matcher(e);
        if(m.find()){
            String s = m.group().toLowerCase();
            r.add(s);
            if(s.length()<e.length())
                r.addAll(camelSplit(e.substring(s.length())));
        }
        return r;
    }
}
