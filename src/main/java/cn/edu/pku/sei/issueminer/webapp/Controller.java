package cn.edu.pku.sei.issueminer.webapp;

import cn.edu.pku.sei.issueminer.similarity.vsm.CodeDescriptionSearcher;
import cn.edu.pku.sei.issueminer.similarity.vsm.CodeQueryParser;
import cn.edu.pku.sei.issueminer.webapp.entity.Conf;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
public class Controller {

    @Autowired
    private Context context;
    private GraphDatabaseService db;

    @PostConstruct
    public void init(){
        db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(context.graphDir));
        System.out.println("Springboot is OK");
    }

    @ResponseBody
    @RequestMapping(value = "/issueSearch",method = {RequestMethod.GET,RequestMethod.POST})
    synchronized public String issueSearch(@RequestParam(value = "codequery") String query) throws IOException, ParseException {
        System.out.println("开始search...");
        CodeDescriptionSearcher cds = new CodeDescriptionSearcher(db,context.dataDir);
        CodeQueryParser cqp = new CodeQueryParser(db);
        String queryvsm = cqp.getVsmQuery(query);
        System.out.println(queryvsm);
        System.out.println(cds.search(queryvsm));
        List<String> list = cds.search(queryvsm);
        String result="";
        for(String s:list){
            result+=s+"\n";
        }
        return result;
    }

}

@Component
class Context{
    String graphDir = null;
    String dataDir = null;

    @Autowired
    public Context(Conf conf){
        this.dataDir = conf.getDataDir();
        this.graphDir = conf.getGraphDir();
    }
}
