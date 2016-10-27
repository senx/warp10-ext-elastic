# Get data from ElasticSearch in Warp10

The data set used in this demonstration corresponds to the bank one avalaible in the [elasticsearch getting started - account set](https://www.elastic.co/guide/en/kibana/5.0/tutorial-load-dataset.html)

## The SINGLE GET function

This function can be used to GET a single point from elasticsearch.

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

// The index to GET
'bank'

// The type to GET
'account'

// The ID to GET
'99'

// Call the function
GETELASTIC
```

This will leave on the map a WarpScript Map containing all the attributes stored in Elastic search

Example
```
{"account_number":99,"firstname":"Ratliff","address":"806 Rockwell Place","balance":47159,"gender":"F","city":"Shaft","employer":"Zappix","state":"ND","age":39,"email":"ratliffheath@zappix.com","lastname":"Heath"}
```

## The MULTI GET function

The multi get function function in the same way than the GET function for the fourth first parameters. 

```
// The param map to connect to The Elastic cluster
{ 
	
	// Cluster host - mandatory parameter
	'host' 'localhost'

	// Cluster port -  mandatory parameter
	'port' 9300
}

// The index to GET
'bank'

// The type to GET
'account'
```

The only difference is for the ID, it expects or a LIST or a MAP on the stack. In the case of list, strictly all ids element will be gotten from the elasticsearch cluster. The map case works only for numerics id, as will get all the id that belongs inside the two id given as parameter (key first and last).

LIST CASE
```
// The IDs to GET
[ 2 '90' '99' ]
```

MAP CASE
```
// The IDs to GET
{ 
	'first' 92
	'last' 99
}
```

then
```
// Call the function
MULTIGETELASTIC
```

With the list case the get brings 3 items inside a WarpScript list
```
[{"account_number":2,"firstname":"Roberta","address":"560 Kingsway Place","balance":28838,"gender":"F","city":"Bennett","employer":"Chillium","state":"LA","age":22,"email":"robertabender@chillium.com","lastname":"Bender"},{"account_number":90,"firstname":"Herman","address":"737 College Place","balance":25332,"gender":"F","city":"Flintville","employer":"Lunchpod","state":"IA","age":22,"email":"hermansnyder@lunchpod.com","lastname":"Snyder"},{"account_number":99,"firstname":"Ratliff","address":"806 Rockwell Place","balance":47159,"gender":"F","city":"Shaft","employer":"Zappix","state":"ND","age":39,"email":"ratliffheath@zappix.com","lastname":"Heath"}]
```

With the Map case all the items that have an id in [92,99] are gotten inside a WarpScript list.
```
[{"account_number":92,"firstname":"Gay","address":"369 Ditmars Street","balance":26753,"gender":"M","city":"Moquino","employer":"Savvy","state":"HI","age":34,"email":"gaybrewer@savvy.com","lastname":"Brewer"},{"account_number":93,"firstname":"Jeri","address":"322 Roosevelt Court","balance":17728,"gender":"M","city":"Leming","employer":"Geekology","state":"ND","age":31,"email":"jeribooth@geekology.com","lastname":"Booth"},{"account_number":94,"firstname":"Brittany","address":"183 Kathleen Court","balance":41060,"gender":"F","city":"Cornucopia","employer":"Mixers","state":"AZ","age":30,"email":"brittanycabrera@mixers.com","lastname":"Cabrera"},{"account_number":95,"firstname":"Dominguez","address":"539 Grace Court","balance":1650,"gender":"M","city":"Wollochet","employer":"Portica","state":"KS","age":20,"email":"dominguezle@portica.com","lastname":"Le"},{"account_number":96,"firstname":"Shirley","address":"817 Caton Avenue","balance":15933,"gender":"M","city":"Nelson","employer":"Equitox","state":"MA","age":38,"email":"shirleyedwards@equitox.com","lastname":"Edwards"},{"account_number":97,"firstname":"Karen","address":"512 Cumberland Walk","balance":49671,"gender":"F","city":"Fredericktown","employer":"Tsunamia","state":"MO","age":40,"email":"karentrujillo@tsunamia.com","lastname":"Trujillo"},{"account_number":98,"firstname":"Cora","address":"555 Neptune Court","balance":15085,"gender":"F","city":"Independence","employer":"Kiosk","state":"MN","age":24,"email":"corabarrett@kiosk.com","lastname":"Barrett"}]
```

## Example to convert a WarpScript List coming from Elastic search to GTS

```
//
// Get from elastic
//
{
  'host' 'localhost'
  'port' 9300
}
'bank'
'account'
{ 
  'first' 90 
  'last' 99
}
MULTIGETELASTIC

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
```

This script leave the following serie on the stack

```
{"c":"age","l":{},"a":{},"v":[[90,22],[91,20],[92,34],[93,31],[94,30],[95,20],[96,38],[97,40],[98,24]]}
``` 

The value in a WarpScript time series can be of several types: string, double, boolean or long.

