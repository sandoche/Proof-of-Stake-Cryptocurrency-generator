----
# PLUGIN DEVELOPERS GUIDE #

----
Current Plugin Version: 1

## Introduction ##

By developing a plugin, you can add functionality to the NRS client. Have
a look at the client source code and documentation to get an overview
about the various javascript APIs and best practices and examples how to
use them.

For a plugin to be valid it has to be delivered with a minimum set of files
and come with a ``manifest.json`` plugin manifest file being compatible with
the current major plugin version and providing some meta information about
the plugin. 

----
## Example Plugin ##

There is an example plugin ``hello_world`` which can be found in the ``plugins``
folder. If you want to see this plugin in the client UI you can activate it
in the associated ``manifest.json`` file by setting the ``deactivated`` flag to ``false``.

----
## File Structure ###

The following is the minimal file structure for a plugin:

```
[plugin_name]/
[plugin_name]/manifest.json
[plugin_name]/html/pages/[plugin_name].html
[plugin_name]/html/modals/[plugin_name].html
[plugin_name]/js/nrs.[plugin_name].js
[plugin_name]/css/[plugin_name].css
```

### Manifest File ###

Meta information about the plugin is provided as a ``JSON`` dictionary in a
``manifest.json`` file in following format:

```
{
    //mandatory
    "pluginVersion": 1, //Integer, don't use parenthesis!
    
    "name": "Name of your plugin", //max. 20 characters
    "myVersion": "Your plugin version", //no format requirements
    "short_description": "A description of your plugin", //max. 200 characters
    "infoUrl": "http://infosaboutmyplugin.info",
    "startPage": "p_hello_world", //One of the pages used for NRS.pages.PAGENAME method(s)

    "nrsVersion": "1.5.0", //ALWAYS provide three sequence numbers, no additions like "e"!

    //optional
    "deactivated": true, //hard-set deactivation, default: false
    "sidebarOptOut": true //opt out of being listed under sidebar "Plugins" entry, default: false
}
```

Hint: Don't use comments in your own ``JSON`` file!

### Plugin Compatibility/Valdation ###

Plugins are compatible when the manifest file is written for the same
major plugin version supported by the installed client.

Major plugin versions won't change very often, minor plugin version releases will
remain compatible within the major version.

After a detected plugin is determined as compatible the NRS client will be validating the
manifest file format and file structure.

### NRS Compatibility ###

Due to the broad scope of plugins the functional compatility of the plugin 
with various NRS versions can't be guaranteed by the plugin mechanism 
and is the responsibility of the plugin creator.

The ``nrs_version`` attribute in the manifest file indicates the NRS version
the plugin was written for. Due to possible changes in javascript API behaviour
it is recommended to release a new plugin version for every new NRS release,
though a plugin will still be running after minor release updates (e.g. a
plugin written for "1.5.1" running under "1.5.5" client installation).

After a major NRS update (e.g. from "1.5.9" to "1.6.0"), the plugin will stop
working and has to be updated.

## Best Practices for Development ##

- Namespace your function names, CSS IDs and classes and other possible
identifiers to avoid collisions affecting core NRS behaviour
- Convention vor namespacing: "p_[PLUGINNAME]_[LOCALIDENTIFIER]", e.g.
"p_hello_world_info_modal"
- Don't manipulate non-plugin HTML or CSS with your javascript code or CSS
declarations

----
## Changelog ##

**Version 1.0, 2015/02/16**

Initial plugin/manifest version









