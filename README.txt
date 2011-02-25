h1. Marathon

Marathon - Java GUI Testing Tool.

h2. What is Marathon?

Marathon is a tool for recording, replaying, refactoring test cases for Java GUI programs developed using Swing components. Marathon consists of an editor, a recorder and a player. Marathon records the test cases in an easy to read and maintainable format using Jython, or JRuby that can be selected at the time of project creation. The test cases can be run either through the UI or in batch mode.

h2. Building Marathon

You need to clone this repository as well update submodule to compile Marathon.

<pre><code>
$ git clone git://github.com/jalian-systems/Marathon.git
$ cd Marathon
$ git submodule update --init
$ ant
</code></pre>

Should generate marathon-{version}.zip.

h3. Using Eclipse

Clone the repository.

Install egit/jgit plugins. Import the projects from the cloned repository. There is an EclipseFormatting.xml file in the repository - set eclipse formatting preferences to use these preferences.

Build All. Enjoy.

Note: The build might fail first time (Version class not found). Just use Build All again.

h2. More Information 

You can get more information about Marathon and documentation/support from:

http://www.marathontesting.com/
