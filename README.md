FoxyDocs : A FOXopen documentation Editor
=========================================

A simple developer tool for adding, editing and exporting documentation for one or more Fox Modules.

___WARNING:_ The actual version is not yet ready to be used.__

http://foxopen.net/

## Demonstration Video

https://www.youtube.com/watch?v=J8tuOmkc5YA

## Getting Started with Eclipse
This section describes how to setup the project using Eclipse.

You should be able to use an alternative IDE, such as NetBean. You however will need some libraries from Eclipse.

### Install Eclipse
Download Eclipse from _http://www.eclipse.org/downloads/_

### Install WindowBuilder
This project has been build with JFace/SWT using WindowBuilder. You'll also need a couple of libraries from this plugin.

Install it using the Eclipse Wizard `Help` -> `Install New Software...`. 
You'll find the URL to add here : _http://www.eclipse.org/windowbuilder/download.php_

### Import the project
1. Get the code from Github
2. In Eclipse, create a new SWT/Jface Java Project (from `File` -> `New` -> `Other`)
3. Set the project location to the local copy of the code

### Add Databinding libraries
FoxyDocs a library called Databinding included in Eclipse. Add it to the Build Path following these steps :

1. Open the project's Properties
2. Go to the tab `Libraries` within `Java Build Path` 
3. Click on `Add External JARs`
4. Browse to the Eclipse install directory (such ash _/opt/eclipse_ or _C:\Program Files\eclipse_)
5. Open the `plugins` folder
6. Add every jars containing `databinding`

In the future, the relevant JARs will be added in the lib directory so we won't depend on an Eclipse installation.

## Usage

1. Open a folder containing Fox Module (Ctrl + O)
2. Browse to each module to add proper documentation for each entry. 
  * Green Tick : entry completed
  * Red Dash : partial entry
  * Red Cross : missing entry
3. If the file is locked (grey icon), unlock it with SVN or Preforce.
4. Save the files (Ctrl + S)
5. Export the directory to HTML (Ctrl + H) or the currently open module to PDF (Ctrl + P)

## Known issues
* The Pretty Print function does not align with the XMLSpy one
* Adding or removing folders or files within the opened folder may behave improperly
* The user can select multiple text fields at the same time
* Comments within a documentation node are not accessible
* There is no CSS nor image in the PDF export
* It is not possible to export an entire directory as PDF
* After saving a file, the currently opened entry is not highlighted anymore
* You can close an unsaved tab without any warning

## Development Notes
### Static assets
The static assets such as XSL and images are stored in `lib/assets.jar` with the following structure:
<pre>
\_ img      
  \_ actions
    \_ ...			All actions images (save, open, etc..)
  \_ icons
    \_ ... 			All other useful icons
\_ xsl
  \_ bg.png 		Background image for the HTML report
  \_ index.html 	Frame set for the HTML report
  \_ listing.xsl 	XSL for the list of modules
  \_ logo.png 		FOXopen logo
  \_ module.xsl 	XSL for extracting the documentation out of a FOX module
  \_ style.css 		Cascading style sheet for index.html
  \_ summary.html 	Module specification container
  \_ xhtml2fo.xsl 	XSL to convert a XHTML document to FOP for PDF export
</pre>

Those assets can be loaded directly from the similar structure in `lib` or from `assets.jar`.

### Databinding source files
All sources in `src/org/eclipse/wb` are automaticly imported by Eclipse when adding databinding libraries. Those files are not to be modified.

### Source structure
Structure inside `src/net/foxopen/foxydocs`:
<pre>
foxydocs
\_ FoxyDocs.java 				Main class; starts the GUI
\_ model
  \_ ... 						Model files for databinding
\_ utils
  \_ Export.java 				Generates the HTML or PDF report
  \_ Loader.java 				Load all FOX modules from a folder
  \_ Logger.java 				Log a message into the terminal
  \_ ResourceManager.java 		Handle static ressources (please see the static assets section
  \_ WatchDog.java 				Watch for files to be modified into a folder (similar to Clobber
  \_ WatchDogEventHandler.java 	Handle the events raised by the watch dog
\_ view
  \_ .. 						GUI elements
</pre>

## Credits

### Development team
* Pierre-Dominique Putallaz
* Mike Leonard

### XSL for export
* William Friesen

### Libraries
* XML-Region-Analyzer : https://github.com/vincent-zurczak/Xml-Region-Analyzer
* FOP 1.1
* jConfig
* jDom2

