package cn.edu.pku.sei.issueminer.webapp.entity;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class Conf {

    private String graphDir;
    private String dataDir;

    public String getGraphDir() {
        return graphDir;
    }

    public void setGraphDir(String graphDir) {
        this.graphDir = graphDir;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

}
