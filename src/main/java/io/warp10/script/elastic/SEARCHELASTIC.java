package io.warp10.script.elastic;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

public class SEARCHELASTIC extends NamedWarpScriptFunction implements WarpScriptStackFunction{

  private boolean isMetaSearch;
  
  public SEARCHELASTIC(String name, boolean meta) {
    super(name);
    final Logger logger = LogManager.getLogger(SEARCHELASTIC.class);
    isMetaSearch = meta;
  }

  //
  // Push on a WarpScript Stack the result of Search on Elasticsearch
  // Exepect on top of the Stack
  // Map containing the option for the Elasticsearch cluster
  // Map containing the Query settings
  // In case of a Meta Search a boolean to apply the search with the size given in Query settings
  //
  
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    //
    // Case isMetaSearch expect a boolean on top of the stack
    //
    Boolean isCustomSize = true;
    
    if (isMetaSearch) {
      Object customSize = stack.pop();
      if (! (customSize instanceof Boolean)) {
        throw new WarpScriptException("The use custom size setting must be a Boolean.");
      }
      isCustomSize = (Boolean) customSize;
    }
    
    //
    // Get query Settings as WarpScript Map
    //
    
    Object objQuerySettings = stack.pop();
    
    if (! (objQuerySettings instanceof Map)) {
      throw new WarpScriptException("The query settings must be a Map.");
    }
    
    Map querySettings = (Map) objQuerySettings;
    
    if (!querySettings.containsKey("query")) {
      throw new WarpScriptException("The query settings must contain a key query.");
    } 
    
    //
    // Get query as JSON format
    //
    
    Object objQuery = querySettings.get("query");
    
    if (! (objQuery instanceof String)) {
      throw new WarpScriptException("The query must be a Map.");
    }
    
    if (!ElasticUtils.isJSONValid(objQuery.toString())) {
      throw new WarpScriptException("Query must be a JSON object.");
    }

    this.isValidQuerySettings(querySettings, isCustomSize);
      
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
    // Connect client with params
    //
    
    try {
      
      ElasticUtils.openClient(host.toString(),(Long) port, settings);
      
      //
      // Search on the elastic client
      //
      
      TransportClient client = ElasticUtils.getClient();
      
      SearchResponse scrollResp = executeRequest(client, objQuery.toString(), querySettings, isCustomSize);
      
      //
      // Get resulting hits in non meta search
      //
      
      if (!isMetaSearch) {
        SearchHits searchHits = scrollResp.getHits();
        List<Map<String,Object>> resultMulti = new ArrayList<>();
        for (SearchHit searchHit : searchHits.getHits()) {
          resultMulti.add(searchHit.getSource());
        }
        
        stack.push(resultMulti);
      }
      
      if (isMetaSearch) {
        stack.push(getMeta(scrollResp));
      }
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      ElasticUtils.closeClient();
    }
    
    return stack;
  }

  
  //
  // Method used to get Meta-data from a search in Elastic
  //
  private Map<String,Object> getMeta(SearchResponse scrollResp) {
    
    //
    // Initialize maps: meta, shard and hits
    //
    
    Map<String,Object> mapMeta = new HashMap<>();
    Map<String, Object> mapShards = new HashMap<>();
    Map<String, Object> mapHits = new HashMap<>();
    
    //
    // Push values in Meta map
    //
    
    mapMeta.put("took_ms", scrollResp.getTookInMillis());
    mapMeta.put("time_out", scrollResp.isTimedOut());
    
    if (null != scrollResp.isTerminatedEarly()) {
      mapMeta.put("terminated_early", scrollResp.isTerminatedEarly());
    }
    
    //
    // Push values in Shards map, then push it in meta map
    //
    
    mapShards.put("total", scrollResp.getTotalShards());
    mapShards.put("successful", scrollResp.getSuccessfulShards());
    mapShards.put("failed", scrollResp.getFailedShards());
    
    mapMeta.put("_shards", mapShards);

    //
    // Push values in Hits map, then push it in meta map
    //
    
    mapHits.put("total", scrollResp.getHits().getTotalHits());
    mapHits.put("max_score", scrollResp.getHits().getMaxScore());
    
    mapMeta.put("hits", mapHits);
    
    return mapMeta;
  }

  //
  // Method that trwo exception if a field isn't valid in querySettings
  //
  private void isValidQuerySettings(Map querySettings, Boolean isCustomSize) throws WarpScriptException {
    
    //
    // Check validity of the query map String
    //
    
    //
    // Key "from" must be a Long
    //
    
    if (isCustomSize) {
      if (querySettings.containsKey("from")) {
        if (! (querySettings.get("from") instanceof Long)) {
          throw new WarpScriptException("Key from in querry settings must be of type Long.");
        }
      }
    }
    
    //
    // Key "size" must be a Long
    //
    if (isCustomSize) {
      if (querySettings.containsKey("size")) {
        if (! (querySettings.get("size") instanceof Long || querySettings.get("size") instanceof String)) {
          throw new WarpScriptException("Key size in querry settings must be of type Long.");
        } 
        
        if (querySettings.get("size") instanceof String) {
          if (!querySettings.get("size").equals("all")) {
            throw new WarpScriptException("If key size in querry settings is a string, it must be equals to the string \"all\".");
          }
        }
      }
    }
    
    //
    // Key "indexes" must be a List of String
    //
    
    if (querySettings.containsKey("indexes")) {
      if (! (querySettings.get("indexes") instanceof List)) {
        throw new WarpScriptException("Key indexes in querry settings must be of type List.");
      } 
      List indexes = (List) querySettings.get("indexes");
      for (Object index : indexes) {
        if (! (index instanceof String)) {
          throw new WarpScriptException("Each index in querry settings must be of type String.");
        }
      }
    }
    
    //
    // Key "types" must be a List of String
    //
    
    if (querySettings.containsKey("types")) {
      if (! (querySettings.get("types") instanceof List)) {
        throw new WarpScriptException("Key types in querry settings must be of type List.");
      } 
      List types = (List) querySettings.get("types");
      for (Object type : types) {
        if (! (type instanceof String)) {
          throw new WarpScriptException("Each type in querry settings must be of type String.");
        }
      }
    }
    
    if (querySettings.containsKey("timeout")) {
      if (! (querySettings.get("timeout") instanceof String)) {
        throw new WarpScriptException("Key timeout in querry settings must be of type String.");
      }
    }
    
    //
    // search_type can be "query_then_fetch" or "dfs_query_then_fetch"
    //
    if (querySettings.containsKey("search_type")) {
      if (! (querySettings.get("search_type") instanceof String)) {
        throw new WarpScriptException("Key search_type in querry settings must be of type String.");
      }
      
      ArrayList<String> searchTypes = new ArrayList<String>(
          Arrays.asList("dfs_query_then_fetch", "dfsQueryThenFetch", "dfs_query_and_fetch","dfsQueryAndFetch", "query_then_fetch",  "queryThenFetch", "query_and_fetch", "queryAndFetch"));
      
      if (!searchTypes.contains((String) querySettings.get("search_type")))
      {
        throw new WarpScriptException("Key search_type in querry settings must be one of \"dfs_query_then_fetch\"/\"dfsQueryThenFetch\", \"dfs_query_and_fetch\"/\"dfsQueryAndFetch\", \"query_then_fetch\"/\"queryThenFetch\", and \"query_and_fetch\"/\"queryAndFetch\".");   
      }
    }
    
    if (querySettings.containsKey("request_cache")) {
      if (! (querySettings.get("request_cache") instanceof Boolean)) {
        throw new WarpScriptException("Key request_cache in querry settings must be of type Boolean.");
      }
    }
  
    if (querySettings.containsKey("terminate_after")) {
      if (! (querySettings.get("terminate_after") instanceof Long)) {
        throw new WarpScriptException("Key terminate_after in querry settings must be of type Long.");
      }
    }
  }

  //
  // Method to prepare a request from the query settings
  // Return the search response of this query applied on the given client
  //
  private SearchResponse executeRequest(TransportClient client, String query, Map querySettings, Boolean isCustomSize) {
 
    //
    // Initialize query settings default value
    //
    
    int from = 0;
    int size = 10;
    List<String> indexes = new ArrayList<>();
    List<String> types = new ArrayList<>();
    boolean all = false;
    TimeValue ts = null;
    
    //
    // Set user value instead of default ones
    //
    if (isCustomSize) {
      if (querySettings.containsKey("from")) {
        from = ((Long) querySettings.get("from")).intValue();
      }
    }
    
    //
    // In case of size check if user choose a number or to get all values
    //
    if (isCustomSize) {
      if (querySettings.containsKey("size")) {
        if (querySettings.get("size") instanceof String && querySettings.get("size").equals("all")) {
          all = true;
        } else if (querySettings.get("size") instanceof Long) {
          size = ((Long) querySettings.get("size")).intValue();
        }
      }
    }
   
    if (querySettings.containsKey("indexes")) {
      indexes = (List) querySettings.get("indexes");
    }
    
    if (querySettings.containsKey("types")) {
      types = (List) querySettings.get("types");
    }
    
    if (querySettings.containsKey("timeout")) {
      if (!((String) querySettings.get("timeout")).equals("NO_TIMEOUT")) {
        ts = TimeValue.parseTimeValue((String) querySettings.get("timeout"), null, "timeout");
      }
    }
    
    //
    // Prepare request
    //
    
    SearchRequestBuilder request = client.prepareSearch();
    
    //
    // Set user indexes
    //
    
    if (indexes.size() > 0) {
      String[] indexString = new String[indexes.size()];
      int i = 0;
      for (String index : indexes) {
        indexString[i] = index;
        i++;
      }
      request.setIndices(indexString);
    }
    
    //
    // Set user types
    //
    
    if (types.size() > 0) {
      String[] typesString = new String[types.size()];
      int i = 0;
      for (String type : types) {
        typesString[i] = type;
        i++;
      }
      request.setTypes(typesString);
    }
    
    //
    // Set request timeout
    //
    
    if (null != ts) {
      request.setTimeout(ts);
    }
    
    //
    // Set user defined search_type
    //
    
    if (querySettings.containsKey("search_type")) {
      request.setSearchType((String) querySettings.get("search_type"));
    }
    
    //
    // Set user defined request_cache
    //
    
    if (querySettings.containsKey("request_cache")) {
      request.setRequestCache((Boolean) querySettings.get("request_cache"));
    }
    
    //
    // Set user defined terminate_after
    //

    if (querySettings.containsKey("terminate_after")) {
      request.setTerminateAfter(((Long) querySettings.get("terminate_after")).intValue());
    }
    
    //
    // In case user wants to get all results of its search query
    //
    if (all && !isMetaSearch) {
      SearchResponse response = request.setQuery(QueryBuilders.wrapperQuery(query)).setFrom(from).setSize(0).execute().actionGet();
      from = 0;
      size = ((Long) response.getHits().getTotalHits()).intValue();
    }
    
    //
    // In case of a Meta search, do not load resulting hits
    //
    if (isMetaSearch) {
      if (!isCustomSize) {
        from = 0;
        size = 0;
      }
    }

    //
    // Return request Search result
    //
    return request.setQuery(QueryBuilders.wrapperQuery(query)).setFrom(from).setSize(size).execute().actionGet();
  }

}
