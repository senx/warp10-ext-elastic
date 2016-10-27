package io.warp10.script.elastic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import io.warp10.script.WarpScriptException;

public final class ElasticUtils {
  

  public static final String MAX_TIMEOUT_CONF = "warpscript.extension.elastic.maxtimeout";

  public static String MAX_TIMEOUT = "NO_TIMEOUT";
  
  private static TransportClient client = null;
  
  public static boolean hasClient() {
    return client != null;
  }
  
  public static boolean isJSONValid(String test) {
    try {
        new JSONObject(test);
    } catch (JSONException ex) {
        return false;
    }
    return true;
}
  
  public static boolean isParamValid(Object param) throws WarpScriptException {
    
    //
    // Check validity of the User given parameter
    //
    
    if(!(param instanceof Map)) {
      throw new WarpScriptException("CONNECTELASTIC expects a MAP on top of the stack with keys host and port.");
    }

    Map mapParams = (Map) param;
    
    if(!(mapParams.containsKey("host"))) {
      throw new WarpScriptException("CONNECTELASTIC expect key host in param Map");
    }
    
    if(!(mapParams.containsKey("port"))) {
      throw new WarpScriptException("CONNECTELASTIC expect key host in param Map");
    }
    
    if (!(mapParams.get("host") instanceof String)) {
      throw new WarpScriptException("The host must be a String");
    }
    
    if (!(mapParams.get("port") instanceof Long)) {
      throw new WarpScriptException("The port must be a Number");
    }
    
    return true;
  }
  
  public static void openClient(String host, Long port, Map<String,Object> settings) throws UnknownHostException {
    
    //
    // Case empty settings
    //
    
    Settings elasticSettings = Settings.EMPTY;
    
    //
    // Else build settings from the map given as parameter
    //
    if (null != settings) {
      Builder settingBuilder = Settings.builder();
      for (String setting : settings.keySet()) {
        settingBuilder.put(setting, settings.get(setting));
      }
      elasticSettings = settingBuilder.build();
    }
    
    //
    // Open client
    // 
    
    client = new PreBuiltTransportClient(elasticSettings);
    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port.intValue()));
  }
  
  public static TransportClient getClient() {
    return client;
  }
  
  public static void closeClient() {
    if (null != client) {
      client.close();  
      client = null;
    }
  }
  
  
}
