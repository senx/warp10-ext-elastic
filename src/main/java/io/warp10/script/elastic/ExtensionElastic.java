package io.warp10.script.elastic;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.warp10.WarpConfig;
import io.warp10.warp.sdk.WarpScriptExtension;

public class ExtensionElastic extends WarpScriptExtension {

  private final Map<String, Object> functions;
  
  public ExtensionElastic() {
    Properties properties = WarpConfig.getProperties();
    ElasticUtils.MAX_TIMEOUT = properties.getProperty(ElasticUtils.MAX_TIMEOUT_CONF, "NO_TIMEOUT");
    this.functions = new HashMap<String, Object>();
    this.functions.put("GETELASTIC", new GETELASTIC("GETELASTIC", false));
    this.functions.put("MULTIGETELASTIC", new GETELASTIC("MULTIGETELASTIC", true));
    this.functions.put("SEARCHELASTIC", new SEARCHELASTIC("SEARCHELASTIC", false));
    this.functions.put("METASEARCHELASTIC", new SEARCHELASTIC("METASEARCHELASTIC", true));
  }
  
  public Map<String, Object> getFunctions() {
    return this.functions;
  }

}

