# Load data from ElasticSearch to WarpScript

This WarpScript extension had 4 new functions to WarpScript. A [single and multi](GET.md) get and a [search and a meta search](SEARCH.md) on an elasticsearch.

## Build

Execute the following command

```
gradle shadowJar
```

## Warp 10 config

Add in warp10.conf: the ExtensionElastic class to warpscript.extensions.

```
//
// Comma separated list of WarpScriptExtension classes to instantiate to modify the defined WarpScript functions
//
warpscript.extensions = io.warp10.script.elastic.ExtensionElastic
```

Add the generated jar to your Warp10 platform classpath.

Restart the Warp 10 platform.