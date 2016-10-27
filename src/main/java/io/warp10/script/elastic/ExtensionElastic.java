package io.warp10.script.elastic;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.warp10.WarpConfig;
import io.warp10.script.WarpScriptStack;
import io.warp10.warp.sdk.WarpScriptExtension;

public class ExtensionElastic extends WarpScriptExtension {

  public Map<String, Object> getFunctions() {
    
    Properties properties = WarpConfig.getProperties();
    ElasticUtils.MAX_TIMEOUT = properties.getProperty(ElasticUtils.MAX_TIMEOUT_CONF, "NO_TIMEOUT");
    
    Map<String, Object> functions = new HashMap<String, Object>();
    //functions.put("HELLOWARP10", new HELLOWARP10("HELLOWARP10"));
    functions.put("GETELASTIC", new GETELASTIC("GETELASTIC", false));
    functions.put("MULTIGETELASTIC", new GETELASTIC("MULTIGETELASTIC", true));
    functions.put("SEARCHELASTIC", new SEARCHELASTIC("SEARCHELASTIC", false));
    functions.put("METASEARCHELASTIC", new SEARCHELASTIC("METASEARCHELASTIC", true));
    return functions;
  }

}

