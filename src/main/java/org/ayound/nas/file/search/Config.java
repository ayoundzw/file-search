package org.ayound.nas.file.search;

import org.json.JSONObject;  
import java.io.*;  
import java.nio.file.Files;  
import java.nio.file.Paths;  
import java.util.Objects;  
  
public class Config {  
    private String configFilePath;  
    private JSONObject configObject;  
  
    public Config(String configFilePath) {  
        this.configFilePath = configFilePath;  
        this.configObject = new JSONObject();  
    }  
  
    public void loadConfig() throws IOException {  
        if (Files.exists(Paths.get(configFilePath))) {  
            String jsonString = new String(Files.readAllBytes(Paths.get(configFilePath)));  
            configObject = new JSONObject(jsonString);  
        }  
    }  
  
    public void saveConfig() throws IOException {  
        Files.write(Paths.get(configFilePath), configObject.toString(4).getBytes());  
    }  
  
    public String getSourceFilePath() {  
        return configObject.optString("sourceFilePath", "");  
    }  
  
    public void setSourceFilePath(String sourceFilePath) {  
        configObject.put("sourceFilePath", sourceFilePath);  
    }  
  
    public String getIndexFilePath() {  
        return configObject.optString("indexFilePath", "");  
    }  
  
    public void setIndexFilePath(String indexFilePath) {  
        configObject.put("indexFilePath", indexFilePath);  
    }  
  
    @Override  
    public boolean equals(Object o) {  
        if (this == o) return true;  
        if (o == null || getClass() != o.getClass()) return false;  
        Config config = (Config) o;  
        return Objects.equals(configFilePath, config.configFilePath) &&  
                Objects.equals(configObject.toString(), config.configObject.toString());  
    }  
  
    @Override  
    public int hashCode() {  
        return Objects.hash(configFilePath, configObject);  
    }  
}