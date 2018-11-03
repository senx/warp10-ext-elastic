//
//   Copyright 2018  SenX S.A.S.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package io.warp10.script.elastic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GETELASTIC extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  private boolean isMultiGet;
  
  public GETELASTIC(String name, boolean multi) {
    super(name);
    final Logger logger = LogManager.getLogger(GETELASTIC.class);
    isMultiGet = multi;
  }

  @SuppressWarnings("resource")
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    //
    // Get params
    //

    Object id = stack.pop();
    Object type = stack.pop();
    Object index = stack.pop(); 
    
    //
    // Get param
    //
    
    Object param = stack.pop();
    
    //
    // Check if param is valid and convert it to map
    //
    
    ElasticUtils.isParamValid(param);

    Map mapParams = (Map) param;
    
    Map settings = null;
    
    if (mapParams.containsKey("settings")) {
      if (!(mapParams.get("settings") instanceof Map)) {
        throw new WarpScriptException("The settings must be a Map.");
      }
      settings = (Map) mapParams.get("settings");
    }

    //
    // Get mandatory host and url
    //
    
    String host = mapParams.get("host").toString();
    Long port = (Long) mapParams.get("port");

    //
    // Check validity of the User given parameter
    //
    
    if (!(index instanceof String)) {
      throw new WarpScriptException("The index must be a String.");
    }
    
    if (!(type instanceof String)) {
      throw new WarpScriptException("The type must be a String.");
    }

    //
    // Case of single GET expect an if of type Number or String
    //
    if (!isMultiGet) {
      if (!(id instanceof String || id instanceof Long)) {
        throw new WarpScriptException("The id must be a String or a Number.");
      }
    }
    
    //
    //
    // 
    if (isMultiGet) {
      if (!(id instanceof List || id instanceof Map)) {
        throw new WarpScriptException("The id must be a List or a Map.");
      }
      
      //
      // Check if id of type List contains only elements of type String or Long
      //
      
      if (id instanceof List) {
        List<Object> ids = (List<Object>) id;
        
        //
        // Get all items in ids LIST
        //
        
        for (Object object : ids) {
          
          //
          // Check if id is valid
          //
          
          if (!(object instanceof String || object instanceof Long)) {
            throw new WarpScriptException("The id must be a String or a Number.");
          }
        }
      }
      
      //
      // Check if id of type Map is correct (key first and last)
      //
      
      if (id instanceof Map) {

        Map mapId = (Map) id;
        if (!mapId.containsKey("first")) {
          throw new WarpScriptException("The map id must contains key first (corresponding to first id to get).");
        }
        if (!mapId.containsKey("last")) {
          throw new WarpScriptException("The map id must contains key last (corresponding to last id to get).");
        }

        if (!(mapId.get("first") instanceof Long)) {
          throw new WarpScriptException("The first id must be a Number.");
        }
        
        if (!(mapId.get("last") instanceof Long)) {
          throw new WarpScriptException("The last id must be a Number.");
        }
      }
    }
    
    //
    // Connect client with params
    //
    
    try {
      
      ElasticUtils.openClient(host.toString(),(Long) port, settings);
      
      //
      // Get elastic client
      //
      
      TransportClient client = ElasticUtils.getClient();
      
      //
      // Case of a Single GET on a specific id
      //
      
      if (!isMultiGet) {
        GetResponse response = client.prepareGet(index.toString(), type.toString(), id.toString()).get();
        if (response.isExists()) {  
          stack.push(response.getSource());
        } else {
          throw new WarpScriptException("Couldn't get data from elastisearch in " + this.getName() + ".");
        }
      }    
      
      //
      // Case of a Get on multiple ID for a same index,type
      //
      
      if (isMultiGet) {
        
        List<Map<String,Object>> resultMulti = new ArrayList<>();
        
        // 
        // Prepare GET
        //
        
        MultiGetRequestBuilder multiGetRequest = client.prepareMultiGet();
        
       
        
        if (id instanceof List) {
          List<Object> ids = (List<Object>) id;
          
          //
          // Get all items in ids LIST
          //
          
          for (Object object : ids) {
            multiGetRequest.add(index.toString(), type.toString(), object.toString());
          }
        } else if (id instanceof Map) {
          
          //
          // Check if the map is valid
          //
          
          Map mapId = (Map) id;
          
          //
          // Get all items between both id values
          //
          
          Long first = (Long) mapId.get("first");
          Long last = (Long) mapId.get("last");
          
          for (Long i = first; i < last; i++) {
            multiGetRequest.add(index.toString(), type.toString(), i.toString());
          }
        }
        MultiGetResponse multiGetItemResponses = multiGetRequest.get();
        
        //
        // Get response
        //
        
        for (MultiGetItemResponse itemResponse : multiGetItemResponses) { 
          GetResponse response = itemResponse.getResponse();
          if (response.isExists()) {                      
            resultMulti.add(response.getSource());
          } else {
            ElasticUtils.closeClient();
            throw new WarpScriptException("Couldn't get data from elastisearch " + this.getName() + ".");
          }
        }
        stack.push(resultMulti);
      } 
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      ElasticUtils.closeClient();
    }
    
    return stack;
  }
}
