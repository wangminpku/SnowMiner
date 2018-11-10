package cn.edu.pku.sei.issueminer.similarity.textrank;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopWords {
    public static Set<String> englishStopWords = new HashSet<>();
    static{
        List<String> lines = new ArrayList<>();
        try{
            lines = FileUtils.readLines(new File(cn.edu.pku.sei.issueminer.similarity.textrank.StopWords.class.getResource("/").getPath()+"stopwords_issue.txt"),"utf-8");

        }catch (IOException e){
            e.printStackTrace();
        }
        englishStopWords.addAll(lines);
    }
    public static boolean isStopWord(String word){ return englishStopWords.contains(word);}
}
