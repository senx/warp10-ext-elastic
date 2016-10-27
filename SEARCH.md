# Search data from ElasticSearch in Warp10

The data set used in this demonstration corresponds to the bank one avalaible in the [elasticsearch getting started - account set](https://www.elastic.co/guide/en/kibana/5.0/tutorial-load-dataset.html)

## The SEARCH function

This function can be used to SEARCH hits in an elasticsearch. To execute a search add a parametrical map to configure the elastic cluster on stack. Then add a map to configure the request. The query settings map have only one field mandatory: query. Other one are optional, and corresponds to the one that can be found [here](https://www.elastic.co/guide/en/elasticsearch/reference/5.0/search-request-body.html). They have the same default value. Moreover two others parameters are added ("indexes" and "types") that allows the user to choose the indexes and the types on which the search will be applied. Those parameters expects a list of string as value.

The following code is a simple search that will get all the object of elasticsearch that contains the field firstname with a value indexed in lower case equals to aurelia. 

```
// The param map to connect to The Elastic cluster
{ 
	
	// Cluster host - mandatory parameter
	'host' 'localhost'

	// Cluster port -  mandatory parameter
	'port' 9300
	
	// Cluster settings - Optionnal 
	'settings'
	{
		// Add settings to an elastic cluster by adding a key and its value
		// Example with cluster.name setting
		'cluster.name' 'elasticsearch'
	}
}

// The query setting map
{
  'query'
  // The query applied in JSON format
  '{ "term" : { "firstname" : "aurelia" } }'
}

// Call the function
SEARCHELASTIC
```

The query string correponds to the [QueryDSL](https://www.elastic.co/guide/en/elasticsearch/reference/5.0/query-dsl.html) of elasticsearch. 
Put directly the JSON contained in the first query field of those requests.
This will leave on the map a WarpScript List containing all the answer elements of this search.

Result - Example
```
[{"account_number":44,"firstname":"Aurelia","address":"502 Baycliff Terrace","balance":34487,"gender":"M","city":"Yardville","employer":"Orbalix","state":"DE","age":37,"email":"aureliaharding@orbalix.com","lastname":"Harding"}]
```

The second example shows how to use every parameters of the query settings map. In this search, all the data from elastic search contained in the index "bank", with type "account" will be contained in the result of the search. Only 2 results will be loaded in WarpScript starting from the second result.

```
// The param map to connect to The Elastic cluster
{ 

	// Cluster host - mandatory parameter
	'host' 'localhost'
	
	// Cluster port -  mandatory parameter
	'port' 9300
	
	// Cluster settings - Optionnal 
	'settings'
	
	{
    	// Add settings to an elastic cluster by adding a key and its value
    	// Example with cluster.name setting
    	'cluster.name' 'elasticsearch'
    }
}

// The query setting map
{

	// Indexes list (index must be string) to apply the search (default: all)
	'indexes' [ 'bank' ]
	
	// Types list (type must be string) to apply the search (default: all)
	'types' [ 'account' ]
	
	// Add a timeout (Defaults to no timeout, type must be a String)
	// 'timeout' 'NO_TIMEOUT' set to no timeout
	'timeout' '1000ms'
	
	// Result starting from (default: 0, type must be Long)
	'from' 1
	
	// Number of object loaded in result (default: 10, type must be Long) 
	// Size can also have one String value: 'all' to load all results of the search (This will reset from to 0)
	'size' 2
	
	// Search type (default: 'query_then_fetch', type must be String)
	// Values can be one of 'dfs_query_then_fetch'/'dfsQueryThenFetch', 'dfs_query_and_fetch'/'dfsQueryAndFetch', 'query_then_fetch'/'queryThenFetch', and 'query_and_fetch'/'queryAndFetch'.");   
	'search_type' 'dfs_query_then_fetch'
	
	// Set to true or false to enable or disable the caching of search results for requests where size is 0 (Must be a Boolean)
	'request_cache' false
	
	// The maximum number of documents to collect for each shard (Must be a Long)
	'terminate_after' 1000
	
	// The query applied in JSON format
	'query' '{ "match_all" : {} }'
}

// Call the function
SEARCHELASTIC
```

The resulting stack

```
[{"account_number":44,"firstname":"Aurelia","address":"502 Baycliff Terrace","balance":34487,"gender":"M","city":"Yardville","employer":"Orbalix","state":"DE","age":37,"email":"aureliaharding@orbalix.com","lastname":"Harding"},{"account_number":99,"firstname":"Ratliff","address":"806 Rockwell Place","balance":47159,"gender":"F","city":"Shaft","employer":"Zappix","state":"ND","age":39,"email":"ratliffheath@zappix.com","lastname":"Heath"}]
```


## Search Meta function
This function will load all Meta parameter on an elastic search cluster, and load them on the stack.

```
// The param map to connect to The Elastic cluster
{ 
	
	// Cluster host - mandatory parameter
	'host' 'localhost'

	// Cluster port -  mandatory parameter
	'port' 9300
	
	// Cluster settings - Optionnal 
	'settings'
	{
		// Add settings to an elastic cluster by adding a key and its value
		// Example with cluster.name setting
		'cluster.name' 'elasticsearch'
	}
}

// The query setting map
{

	// Indexes list (index must be string) to apply the search (default: all)
	'indexes' [ 'bank' ]
	// Types list (type must be string) to apply the search (default: all)
	'types' [ 'account' ]
	// The query applied in JSON format
	'query' '{ "term" : { "firstname" : "aurelia" } }'
}

// Params from and size can be set using the query map
// Does the METASEARCHELASTIC use them, or set from and size to 0
// Value true will give the META for a search with the custom defined size but WILL NOT load them
false

// Call the function
METASEARCHELASTIC
```

The result on the stack

```
{"_shards":{"total":5,"failed":0,"successful":5},"hits":{"total":1,"max_score":0.0},"took_ms":63,"time_out":null}
```

## Example to convert a WarpScript List coming from Elastic search to GTS

```
{
  'host' 'localhost'
  'port' 9300
  'settings' {
    'cluster.name' 'elasticsearch'
  }
}
{
  'indexes' [ 'bank' ]
  'types' [ 'account' ]
  'size' 'all'
  'query'
  '{ "match_all" : {} }'
}
SEARCHELASTIC


//
// Create a new GTS 
//

NEWGTS

//
// Rename it
//

'age' RENAME

//
// Store the GTS in a variable
//
'myGTS' STORE

//
// Foreach items in the get Result
//
<%

  'currentObject' STORE

  //
  // Put GTS on stack
  //
  $myGTS 

  //
  // Set GTS timestamp
  //
  $currentObject 'account_number' GET

  //
  // Set loaction and elevation
  //
  NaN NaN NaN

  //
  // Set age value 
  //
  $currentObject 'age' GET

  //
  // Add age as GTS value
  //
  ADDVALUE

  //
  // Store the GTS in a variable
  //
  'myGTS' STORE
%>
FOREACH

//
// Put GTS on stack
//
$myGTS
SORT
```

This script leave the following serie on the stack. The value in a WarpScript time series can be of several types: string, double, boolean or long.

```
{"c":"age","l":{},"a":{},"v":[[0,29],[1,32],[2,22],[3,26],[4,31],[5,30],[6,36],[7,22],[8,35],[9,39],[10,37],[11,20],[12,20],[13,28],[14,39],[15,21],[16,34],[17,31],[18,33],[19,28],[20,36],[21,38],[22,23],[23,20],[24,39],[25,39],[26,39],[27,26],[28,20],[29,33],[30,26],[31,22],[32,34],[33,30],[34,30],[35,27],[36,39],[37,39],[38,32],[39,22],[40,40],[41,20],[42,26],[43,25],[44,37],[45,21],[46,23],[47,23],[48,39],[49,23],[50,33],[51,31],[52,31],[53,29],[54,22],[55,33],[56,32],[57,21],[58,40],[59,37],[60,31],[61,20],[62,37],[63,30],[64,35],[65,24],[66,28],[67,39],[68,25],[69,24],[70,33],[71,39],[72,25],[73,32],[74,38],[75,22],[76,24],[77,24],[78,23],[79,29],[80,30],[81,40],[82,39],[83,28],[84,34],[85,20],[86,36],[87,22],[88,21],[89,28],[90,22],[91,20],[92,34],[93,31],[94,30],[95,20],[96,38],[97,40],[98,24],[99,39],[100,32],[101,31],[102,27],[103,33],[104,29],[105,33],[106,36],[107,28],[108,21],[109,31],[110,23],[111,35],[112,30],[113,27],[114,31],[115,31],[116,24],[117,38],[118,33],[119,28],[120,33],[121,32],[122,31],[123,27],[124,20],[125,30],[126,39],[127,33],[128,34],[129,33],[130,33],[131,22],[132,35],[133,36],[134,30],[135,40],[136,38],[137,29],[138,39],[139,35],[140,32],[141,29],[142,27],[143,39],[144,30],[145,32],[146,32],[147,28],[148,34],[149,21],[150,20],[151,20],[152,21],[153,31],[154,31],[155,39],[156,24],[157,20],[158,27],[159,22],[160,23],[161,37],[162,35],[163,33],[164,26],[165,40],[166,23],[167,20],[168,20],[169,34],[170,36],[171,39],[172,20],[173,32],[174,23],[175,28],[176,28],[177,40],[178,39],[179,25],[180,32],[181,22],[182,21],[183,26],[184,27],[185,40],[186,32],[187,35],[188,24],[189,38],[190,30],[191,28],[192,31],[193,34],[194,39],[195,31],[196,28],[197,33],[198,38],[199,26],[200,39],[201,25],[202,31],[203,33],[204,39],[205,28],[206,20],[207,35],[208,26],[209,30],[210,24],[211,22],[212,39],[213,27],[214,37],[215,20],[216,35],[217,38],[218,24],[219,25],[220,22],[221,34],[222,36],[223,26],[224,28],[225,24],[226,40],[227,22],[228,20],[229,30],[230,28],[231,34],[232,34],[233,27],[234,37],[235,31],[236,39],[237,27],[238,28],[239,36],[240,35],[241,26],[242,21],[243,20],[244,27],[245,28],[246,21],[247,37],[248,36],[249,38],[250,39],[251,39],[252,22],[253,31],[254,21],[255,38],[256,31],[257,35],[258,37],[259,30],[260,30],[261,34],[262,36],[263,29],[264,35],[265,26],[266,35],[267,21],[268,36],[269,34],[270,39],[271,30],[272,25],[273,20],[274,33],[275,31],[276,23],[277,31],[278,27],[279,32],[280,26],[281,20],[282,25],[283,30],[284,29],[285,28],[286,35],[287,35],[288,39],[289,29],[290,37],[291,40],[292,20],[293,28],[294,26],[295,20],[296,34],[297,35],[298,20],[299,36],[300,26],[301,35],[302,40],[303,24],[304,35],[305,29],[306,40],[307,23],[308,25],[309,30],[310,39],[311,23],[312,25],[313,36],[314,35],[315,33],[316,32],[317,31],[318,34],[319,36],[320,37],[321,35],[322,27],[323,34],[324,22],[325,25],[326,30],[327,27],[328,27],[329,25],[330,34],[331,34],[332,28],[333,27],[334,21],[335,24],[336,25],[337,37],[338,35],[339,38],[340,40],[341,30],[342,36],[343,29],[344,35],[345,38],[346,36],[347,24],[348,37],[349,22],[350,22],[351,29],[352,31],[353,37],[354,22],[355,38],[356,20],[357,39],[358,40],[359,28],[360,34],[361,36],[362,26],[363,21],[364,40],[365,31],[366,31],[367,20],[368,39],[369,28],[370,25],[371,32],[372,24],[373,21],[374,30],[375,25],[376,21],[377,34],[378,36],[379,21],[380,33],[381,31],[382,37],[383,28],[384,31],[385,22],[386,39],[387,29],[388,26],[389,27],[390,32],[391,30],[392,35],[393,24],[394,38],[395,31],[396,38],[397,36],[398,35],[399,23],[400,21],[401,38],[402,32],[403,32],[404,26],[405,26],[406,28],[407,29],[408,30],[409,31],[410,39],[411,22],[412,26],[413,39],[414,37],[415,36],[416,28],[417,35],[418,32],[419,29],[420,22],[421,27],[422,26],[423,21],[424,34],[425,30],[426,31],[427,36],[428,20],[429,31],[430,34],[431,26],[432,40],[433,39],[434,25],[435,22],[436,23],[437,29],[438,27],[439,35],[440,31],[441,29],[442,27],[443,23],[444,24],[445,34],[446,32],[447,35],[448,35],[449,39],[450,25],[451,31],[452,39],[453,24],[454,22],[455,36],[456,33],[457,34],[458,21],[459,20],[460,21],[461,34],[462,27],[463,20],[464,21],[465,29],[466,30],[467,32],[468,40],[469,26],[470,35],[471,36],[472,32],[473,25],[474,40],[475,22],[476,31],[477,40],[478,35],[479,40],[480,24],[481,33],[482,39],[483,29],[484,35],[485,40],[486,22],[487,26],[488,38],[489,36],[490,26],[491,24],[492,35],[493,24],[494,30],[495,40],[496,35],[497,30],[498,39],[499,26],[500,28],[501,36],[502,31],[503,39],[504,23],[505,29],[506,28],[507,31],[508,27],[509,40],[510,28],[511,24],[512,29],[513,37],[514,34],[515,27],[516,37],[517,38],[518,29],[519,31],[520,32],[521,34],[522,29],[523,40],[524,30],[525,25],[526,33],[527,35],[528,27],[529,23],[530,37],[531,38],[532,26],[533,23],[534,25],[535,34],[536,33],[537,29],[538,21],[539,23],[540,32],[541,32],[542,35],[543,31],[544,21],[545,20],[546,33],[547,32],[548,37],[549,40],[550,22],[551,27],[552,39],[553,28],[554,39],[555,31],[556,35],[557,20],[558,20],[559,38],[560,26],[561,30],[562,39],[563,30],[564,22],[565,37],[566,37],[567,40],[568,29],[569,39],[570,24],[571,28],[572,20],[573,36],[574,24],[575,39],[576,33],[577,38],[578,37],[579,36],[580,34],[581,32],[582,24],[583,34],[584,40],[585,32],[586,26],[587,33],[588,31],[589,39],[590,31],[591,34],[592,36],[593,37],[594,26],[595,36],[596,26],[597,33],[598,33],[599,36],[600,37],[601,34],[602,33],[603,31],[604,23],[605,24],[606,31],[607,38],[608,32],[609,30],[610,24],[611,33],[612,32],[613,34],[614,35],[615,28],[616,35],[617,22],[618,30],[619,36],[620,38],[621,26],[622,38],[623,32],[624,39],[625,23],[626,31],[627,37],[628,37],[629,26],[630,31],[631,32],[632,20],[633,34],[634,38],[635,33],[636,25],[637,27],[638,31],[639,32],[640,25],[641,39],[642,35],[643,23],[644,21],[645,26],[646,31],[647,30],[648,21],[649,26],[650,28],[651,34],[652,26],[653,33],[654,25],[655,30],[656,36],[657,34],[658,32],[659,40],[660,33],[661,39],[662,33],[663,37],[664,40],[665,36],[666,40],[667,32],[668,27],[669,28],[670,22],[671,34],[672,36],[673,33],[674,22],[675,27],[676,34],[677,26],[678,28],[679,33],[680,32],[681,33],[682,22],[683,39],[684,25],[685,24],[686,30],[687,31],[688,22],[689,28],[690,35],[691,22],[692,21],[693,30],[694,31],[695,26],[696,32],[697,24],[698,36],[699,37],[700,21],[701,27],[702,26],[703,29],[704,22],[705,22],[706,39],[707,30],[708,28],[709,29],[710,37],[711,35],[712,37],[713,21],[714,34],[715,24],[716,34],[717,31],[718,22],[719,25],[720,32],[721,26],[722,34],[723,27],[724,31],[725,26],[726,21],[727,36],[728,28],[729,36],[730,30],[731,35],[732,37],[733,37],[734,23],[735,32],[736,21],[737,23],[738,32],[739,33],[740,22],[741,22],[742,26],[743,23],[744,21],[745,32],[746,28],[747,38],[748,25],[749,36],[750,20],[751,23],[752,31],[753,21],[754,25],[755,22],[756,32],[757,30],[758,28],[759,27],[760,37],[761,34],[762,20],[763,22],[764,30],[765,23],[766,38],[767,27],[768,21],[769,28],[770,26],[771,23],[772,21],[773,36],[774,38],[775,33],[776,24],[777,32],[778,28],[779,32],[780,26],[781,26],[782,36],[783,25],[784,21],[785,29],[786,33],[787,21],[788,39],[789,27],[790,39],[791,38],[792,40],[793,36],[794,32],[795,34],[796,35],[797,26],[798,30],[799,28],[800,28],[801,37],[802,40],[803,25],[804,27],[805,27],[806,31],[807,23],[808,20],[809,30],[810,40],[811,28],[812,32],[813,20],[814,26],[815,30],[816,20],[817,36],[818,26],[819,24],[820,24],[821,22],[822,25],[823,33],[824,33],[825,21],[826,22],[827,29],[828,33],[829,37],[830,23],[831,37],[832,39],[833,22],[834,25],[835,25],[836,25],[837,35],[838,37],[839,39],[840,38],[841,21],[842,23],[843,34],[844,31],[845,39],[846,22],[847,23],[848,38],[849,26],[850,37],[851,33],[852,26],[853,40],[854,25],[855,31],[856,25],[857,23],[858,36],[859,24],[860,37],[861,35],[862,38],[863,40],[864,23],[865,28],[866,28],[867,23],[868,22],[869,25],[870,21],[871,32],[872,36],[873,39],[874,22],[875,24],[876,21],[877,34],[878,40],[879,31],[880,35],[881,38],[882,39],[883,34],[884,40],[885,40],[886,38],[887,36],[888,39],[889,38],[890,25],[891,24],[892,29],[893,38],[894,32],[895,36],[896,26],[897,25],[898,29],[899,23],[900,23],[901,23],[902,23],[903,35],[904,26],[905,20],[906,36],[907,36],[908,31],[909,36],[910,23],[911,21],[912,26],[913,25],[914,32],[915,35],[916,40],[917,24],[918,25],[919,27],[920,26],[921,29],[922,32],[923,26],[924,24],[925,24],[926,21],[927,26],[928,22],[929,35],[930,39],[931,23],[932,33],[933,21],[934,34],[935,30],[936,36],[937,24],[938,40],[939,37],[940,38],[941,28],[942,26],[943,23],[944,38],[945,33],[946,36],[947,30],[948,40],[949,29],[950,32],[951,25],[952,33],[953,27],[954,22],[955,33],[956,22],[957,31],[958,40],[959,40],[960,40],[961,27],[962,21],[963,20],[964,34],[965,28],[966,35],[967,36],[968,39],[969,30],[970,28],[971,32],[972,26],[973,31],[974,26],[975,27],[976,26],[977,21],[978,33],[979,29],[980,33],[981,29],[982,24],[983,24],[984,35],[985,28],[986,31],[987,20],[988,34],[989,38],[990,35],[991,28],[992,33],[993,37],[994,31],[995,25],[996,30],[997,20],[998,40],[999,22]]}
```
