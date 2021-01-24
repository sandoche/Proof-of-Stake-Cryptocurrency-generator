----
# DEVELOPERS GUIDE #

----
## How can you contribute? ##

  - create pull requests
  - review pull requests
  - help users on issues
  - ask us, the dev team

----
## Tools and Tips ##
### Usable URLs ###

  - **API** - http://localhost:7876/test
  - **DB Interface** - http://localhost:7876/dbshell
  - **Java Class Browser** - http://localhost:7876/doc

### Database ###
  
  - H2 embedded database
  - main database: `nxt_db/`
  - test database: `nxt_test_db/`
  - the database directories do not contain user specific data and can be safely deleted
  - but no need to delete them on upgrade, the DbVersion code takes care of schema updates
  - use the nxt.db framework when accessing the database from your code

----
## Coding Process ##


### Branching Model ###

  - [Vincent Driessen's Branching Model](http://nvie.com/posts/a-successful-git-branching-model/)
  - **tl;dr:**
    - master is release branch
    - develop is maintained by Jean-Luc
    - feature/abc is yours

### Design ###

  - better think twice than code twice
  - communicate with community and us

### Implementation ###

  - Coding Style
    - use the existing code as example for whitespace and indentation
    - the default settings of IntelliJ IDEA are good enough
    - make sure your code fits in
    - use [IDE-Code-Style-Config]
  - Code should be self-documenting and readable, only special cases need comments
  - The "Effective Java" book is a must read
  - Some of the advice in "Clean Code" is good

### Testing ###

  - [to be filled by Lior, kushti]
  - all API calls can be tested manually from the auto-generated http://localhost:7876/test page
  - many tests need blocks to be generated, see the examples how to fake the forging process
  - write your tests against the http API or the public java API, which are relatively stable
  
### Documentation ###

  - API calls should be documented first (because if you don't, you never will)

----
## UI Developers ##


### Where to Look ###

  - index.html: all of the html markup for pages and modals 
  - js/nrs.*.js: corresponding js files for each task the file name displays, one roughly for each page
  - js/nrs.modals.*.js: The modal js (popups) for each set of popups, one for each set of modals
  - any CSS: Bootstrap is used for the design, so changes to CSS rules should be generally avoided
  
### Programming Style ###

  - HTML style
    - Make sure to use the i18n for any text data, internationalization
    - Follow everything else as already set up by Wesley
  - JS style
    - Same as above, just make the code fit into every other part
  - Adding a page
    - Create a new html page markup in index.html, refer to other pages, starts with <div id="NAME-page" class="page">
    - Reference the page with a new link in the menu fixed to the left side of the page, from line 245 to 290 at time of writing
    - Create a corresponding js file in /js directory that handles all page specific scripting.
  - Adding a modal
    - Create a new html modal also in index.html, the modals start around line 1750 at time of writing
    - It is fairly easy to make a modal based upon the information from other modals already created.

### Translation ###

#### Language Translations ####

Translation of the client UI to other languages is done by the community within a crowdsourced process on the platform **Crowdin**:

- https://crowdin.com/project/nxt-ui-translation

If you feel comfortable translating you are very welcome to join and help with translations.

#### Coding ####

On the development side, the ``i18next`` Javascript translation library is used for translation. Translations and associated translation keys can be added to the code in the following way:

- With ``data-i18n`` data attribute in HTML code, e.g. ``<span data-i18n="send_message">Send Message</span>``
- Via ``$.t()`` function in JS, e.g. ``$.t("send_message")``

Translation files can be found in the ``locales`` folder, base language is the english translation in ``locales/en/translation.json``.

When adding new text/labeling visible in the UI do the following:

- Use one of the methods outlined above, choose an appropriate translation key
- Add both the key and the english text to the top of the english translation file
- Please don't use namespaces in your keys (e.g. not ``namespace.mynewkey``) since this is complicating the filestructure of translation files when created automatically and cause problems when importing files to translation service
- If possible, don't use the ``$.t()`` function in a dynamic way (e.g. ``$.t(type + "_currency")``), otherwise translation keys can't be extracted from the code
- If you later change the english text in the HTML please also change the text within the english translation file, otherwise the new english text is overwritten with the old english text from translation file
- DON'T USE TRANSLATION TEXTS CONTAINING HTML (TAGGED WITH ``[html]``) FOR SECURITY REASONS!

#### Updating base translation file #####

The basis for other translations is the **english translation** file in ``ui/locales/en/translation.json``. From time to time it might be necessary to collect translation keys forgotten to be added by developers systematically by using the i18next parser. To update the file with the latest keys and english base translations do the following:

1. Make a permanent backup of your ``locales`` folder outside of your Git repository
2. Count the rows of the english translation file, e.g. ``wc -l ui/locales/en/translation.json``
3. To avoid intervening with 3rd party files create a temporary folder for files to be parsed ``mkdir ui/trans-tmp/`` (``cp ui/js/*.* ui/trans-tmp/``, ``cp -R ui/html ui/trans-tmp/``, ``cp ui/*.html ui/trans-tmp/``)
4. Parse translation strings not yet included in the english translation file with the i18next parser (extra install) with ``i18next ui/trans-tmp -r -l en -o ui/locales/`` (if there is a strange "reuseSuffix" entry at the top of the file: this is a bug, delete!)
5. There are dynamic uses of the ``$.t()`` function in the code base causing ``i18next`` to not detect all keys. If there is a generated ``translation_old.json`` file, don't throw these away. Instead add these strings manually to the ``translation.json`` file (keep an eye on commas at the end of the lines!)
6. Search for empty translation strings in english translation file forgotten by developers (by searching for empty string ""), full-text search in client folders for associated key and manually fill-in english string to translation file.

#### Publish new base translations ####

For providing new translation strings on the platform for the community to translate do the following:

1. Update the english base translation file (see above guide)
2. Build the Crowdin project (permissions needed) and make a backup download of the latest translations
2. Compare the number of translation keys in your generated file with the number of keys in the file on the Crowdin website and make sure, these fit together and there weren't any misses in the creation process (don't be confused by Crowdin actually displaying "Words" count, you have to look at the "Settings -> Reports" tab for comparison)
3. Update the ``translation.json`` file on Crowdin
4. Inform the community about new translation tasks

#### Integrating new translations into the client ####

1. Build/download the latest translation files from Crowdin (permissions needed) and replace the language folders like ``fa``, ``pt-BR``,... with the folders downloaded. Please make sure to NOT touch the english folder ``en``.
2. Rename all folder names to lowercase, e.g. ``es-ES`` to ``es-es``.
3. Make some consistency checks (lengths of old/new files, "git diff" on language files)
4. New languages can be added to ``NRS.languages`` in ``ui/js/nrs.settings.js`` file. Review the status of the languages (40-50%+ Experimental, 70-80%+ Beta, 90-95%+ Stable), eventually add new languages

### Mobile App ###

To build the Android APK application follow these steps (Tested on Windows 10 64 bit):
1. Install node.js (tested with Node v8.11.3 and npm 5.6.0), Apache Cordova (tested with Cordova 8.1.1 (cordova-lib@8.1.0)) - see https://cordova.apache.org/
2. Install Android SDK Manager (tested with Android Studio 3.2) - follow the guidelines regarding necessary USB device drivers and modules to install to connect a physical mobile device or use the AVD Manager to setup an emulator. 
2.1 Use the SDK Manager utility to download the necessary dependencies. Make sure to always accept the licenses. 
3. Make sure the commands: node, npm, cordova are available from the command prompt.
4. On Windows make sure the adb.exe is in your search path (c:\Users\%Username%\AppData\Local\Android\sdk\platform-tools), probably also required on other platforms.
5 To generate icons and splash screens: 
  5.1 Install imagemagick https://www.imagemagick.org and check "install legacy utilities (e.g. convert)" in the "Select Additional Tasks" panel
  5.2 Install node packages cordova-icon and cordova-splash
6. Under the <NXTRoot>\mobile\app folder execute the command: createapp.bat, see that all the plugins dependencies are downloaded and that the process completes without errors. If necessary adjust the plugin dependencies in 
   ./html/config.xml this file is copied to the wallet folder by the script.
7. To run the app invoke the command: runapp.bat, to update the app resources after making changes to the web wallet use updateapp.bat
8. Debug the application on the device or emulator using Chrome desktop development tools, open the options menu, more tools --> Remote Devices choose the device and click "Inspect"