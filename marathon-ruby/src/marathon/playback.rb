# $Id: playback.rb 269 2009-01-16 08:07:57Z kd $
# Marathon JRuby Interfaces
# 
#   Interface routines to hook into Marathon runtime. These calls are used
#   by Marathon to record operations on components. Some of the functions
#   are utility functions that can be used while developing the test
#   scripts.
# 

java_import 'net.sourceforge.marathon.api.ComponentId'
java_import 'net.sourceforge.marathon.api.WindowHandler'
java_import 'net.sourceforge.marathon.player.MarathonJava'
java_import 'net.sourceforge.marathon.util.AssertionLogManager'

require 'marathon/results'

class RubyMarathon < MarathonJava
    $assertion = AssertionLogManager.getInstance()

    def initialize()
        @collector = Collector.new()
    end

    def execTest(test)
        @collector.callprotected(test, result)
    end

    def execFixtureSetup
        setup = proc { $fixture.setup }
        @collector.callprotected(setup, result)
    end

    def execFixtureTeardown
    	write_assertions_to_file($test_name)
        teardown = proc { $fixture.teardown }
        @collector.callprotected(teardown, result)
    end

    def execTestSetup
      	return unless $fixture.respond_to? :test_setup
        setup = proc { $fixture.test_setup }
        @collector.callprotected(setup, result)
    end

    def execTestTeardown
      	return unless $fixture.respond_to? :test_teardown
        teardown = proc { $fixture.test_teardown }
        @collector.callprotected(teardown, result)
    end

    def handleFailure(e)
    	raise e if result == nil
      @collector.addfailure(e, result) unless e.isAbortTestCase
      raise e.getMessage if e.isAbortTestCase
    end
end

$marathon = RubyMarathon.new

# Wait for a window to appear. The default timeout is 30seconds

def with_window(windowTitle, timeout = 0)
    $marathon.window(windowTitle, timeout)
    yield
    $marathon.close
    return true
end

# Wait for a internal frame to appear. The default timeout is 30seconds

def with_frame(windowTitle, timeout = 0)
    $marathon.frame(windowTitle, timeout)
    yield
    $marathon.close
    return true
end

# Wait for a window to appear. The default timeout is 30seconds

def window(windowTitle, timeout = 0)
    $marathon.window(windowTitle, timeout)
    return true
end

# Post a window closed event

def window_closed(windowTitle)
	$marathon.windowClosed(windowTitle)
end

# Post a window changed event

def window_changed(state)
	$marathon.windowChanged(state)
end

# Pop the window out of the stack. The next operation takes place
# only when the Window below the stack is focused or a new Window
# call is made.
#

def close
    $marathon.close
end

# Select a given menu item. Menu items are separated by '>>'
# If a keystroke is given - the given keystroke is used to
# activate the menu.

def select_menu(menuitems, keystroke=nil)
    $marathon.selectMenu(menuitems, keystroke)
end

# Send the given keysequence to the application. Keysequence are
# of the form [modifier]+[modifier]+...+[keystroke]. If the given
# keysequence is a single character like 'A' - the corresponding
# keystroke (Shift+A) is sent to the application.

def keystroke(componentName, keysequence, componentInfo=nil)
    $marathon.keystroke(ComponentId.new(componentName, componentInfo), keysequence)
end

# Send a click to the component

def click(componentName, o1 = nil, o2 = nil, o3 = nil, o4 = nil, o5 = nil)
    $marathon.click(componentName, false, o1, o2, o3, o4, o5)
end

# Send a click to the component

def hover(componentName, delay = 500, componentInfo = nil)
    $marathon.hover(componentName, delay, componentInfo)
end

# Send a mouse pressed to the component
def mouse_pressed(componentName, o1 = nil, o2 = nil, o3 = nil, o4 = nil, o5 = nil)
    $marathon.mousePressed(componentName, false, o1, o2, o3, o4, o5)
end

def mouse_down(componentName, o1 = nil, o2 = nil, o3 = nil, o4 = nil, o5 = nil)
    $marathon.mousePressed(componentName, false, o1, o2, o3, o4, o5)
end

# Send a mouse released to the component
def mouse_released(componentName, o1 = nil, o2 = nil, o3 = nil, o4 = nil, o5 = nil)
    $marathon.mouseReleased(componentName, false, o1, o2, o3, o4, o5)
end

def mouse_up(componentName, o1 = nil, o2 = nil, o3 = nil, o4 = nil, o5 = nil)
    $marathon.mouseReleased(componentName, false, o1, o2, o3, o4, o5)
end

# Send a drag to the component

def drag(componentName, o1, o2, o3, o4, o5 = nil, o6 = nil)
		$marathon.drag(componentName, o1, o2, o3, o4, o5, o6)
end

# Send a double click to the component

def doubleclick(componentName, o1 = nil, o2 = nil, o3 = nil, o4 = nil)
    $marathon.click(componentName, false, 2, o1, o2, o3, o4)
end

# Send a right click to the component

def rightclick(componentName, o1 = nil, o2 = nil, o3 = nil, o4 = nil, o5 = nil)
    $marathon.click(componentName, true, o1, o2, o3, o4, o5)
end

# Select a given component and set the state corresponding to the given text.

def select(componentName, text, componentInfo=nil)
    $marathon.select(ComponentId.new(componentName, componentInfo), text)
end

# Get the Java component represented by the given name

def get_component(componentName)
    return $marathon.getComponent(ComponentId.new(componentName, nil))
end

def get_mcomponent(componentName, componentInfo=nil)
	return $marathon.getMComponent(ComponentId.new(componentName, componentInfo))
end

# Get a map that contains components for the current window

def get_named_components()
    return $marathon.getNamedComponents()
end

# Get a list of all visible components in the current window
def dump_components()
    return $marathon.dumpComponents()
end

# Sleep for the given number of seconds

def sleep(seconds)
    $marathon.sleep(seconds)
end

# Fail the test case with the given message
def fail(message)
    $marathon.fail(message)
end

def error(message)
    $marathon.error(message)
end

# Gets the title of the current window

def get_window()
    return $marathon.getWindow()
end

# Gets the current window

def get_window_object()
    return $marathon.getWindowObject()
end

# Gets the available frames
def get_frames
    return $marathon.getFrames()
end

def get_frame_objects
  return $marathon.getFrameObjects()
end

# Recording sequence for a drag and drop operation. Marathon uses a Clipboard copy and paste
# to perform the operation.

def drag_and_drop(source, sourceinfo, target, targetinfo, action)
    $marathon.dragAndDrop(ComponentId.new(source, sourceinfo), ComponentId.new(target, targetinfo), action)
end

# Main $marathon assertion function. Assert that the given value of the property matches that
# of the component currently in the application.

def assert_p(component, property, value, componentInfo=nil)
    $marathon.assertProperty(ComponentId.new(component, componentInfo), property, value)
    $assertion.addAssertion("Property" , component.to_s + " is " + property.to_s)    
end

def wait_p(component, property, value, componentInfo=nil)
    $marathon.waitProperty(ComponentId.new(component, componentInfo), property, value)
end

def assert_content(componentName, content, componentInfo=nil)
	$marathon.assertContent(ComponentId.new(componentName, componentInfo), content.to_java([].to_java(:String).class))
	$assertion.addAssertion("Content" , content.to_s)
end

# Get a property for the given component. Note that what is returned is a String representation
# of the property

def get_p(component, property, componentInfo=nil)
    return $marathon.getProperty(ComponentId.new(component, componentInfo), property)
end

# Get a property for the given component. Note that what is returned is a Java object
def get_po(component, property, componentInfo=nil)
    return $marathon.getPropertyObject(ComponentId.new(component, componentInfo), property)
end

# Capture an image of the current screen and save it to the specified file.
def screen_capture(fileName)
    return $marathon.screenCapture(fileName)
end

# Capture an image of the specified window and save it to the specified file.

def window_capture(fileName, windowName)
    return $marathon.screenCapture(fileName, windowName)
end

# Capture an image of the specified component and save it to the specified file.

def component_capture(fileName, windowName, componentName)
    return $marathon.screenCapture(fileName, windowName, ComponentId.new(componentName, nil))
end

# Compare two images defined by their paths, returns their differences in an array [0] is no. of different pixels, [1] is the percentage.

def image_compare(path1, path2, differencesInPercent=0)
    return $marathon.compareImages(path1,path2,differencesInPercent)
end

def files_equal(path1, path2)
    return $marathon.filesEqual(path1, path2)
end

# Show and accept input from the given checklist

def accept_checklist(filename)
	return $marathon_trace_func.acceptChecklist(filename)
end

def show_checklist(filename)
	return $marathon_trace_func.showChecklist(filename)
end

def assert_equals(expected, actual, message = nil)
	$marathon.assertEquals(message, expected, actual)
    $assertion.addAssertion("Equals" , expected.to_s)    
end

def assert_true(actual, message = nil)
	$marathon.assertTrue(message, actual)
end	

def use_data_file(filename)
end

def with_data(filename)
	reader = $marathon.get_data_reader(filename)
	while(reader.read_next)	
		yield
	end
end

java_import 'java.lang.System'
java_import 'net.sourceforge.marathon.Constants'

$fixture_dir = System.getProperty(Constants::PROP_FIXTURE_DIR)

def require_fixture(s)
	require $fixture_dir + '/' + s
end

java_import 'net.sourceforge.marathon.player.MarathonPlayer'

# By default if the AUT exits, Marathon records that as an error. This flags turns off
# that behavior

def set_no_fail_on_exit(b)
	MarathonPlayer.exitIsNotAnError = b	
end

def write_assertions_to_file(testcase)
	assertion = File.new($marathon_project_dir + "/TestReports/" + testcase + ".txt", "w")
	types = $assertion.getTypes()
	assertions = $assertion.getAssertions()
	passed = $assertion.getPassed()
	for i in 0 .. (types.length - 1) do
   		assertion.puts("<notes><passed>" + passed[i].to_s + "</passed><type>" + types[i].to_s + ": </type>" + "<assertion>" + assertions[i].to_s + "</assertion></notes>")
		assertion.puts("\n")
	end 
	
end

def marathon_help
    print "click(componentName, o1=None, o2=None, o3=None, o4=None, o5=None)\n" +
    "****    Send a click to the component\n" +
    "component_capture(fileName, windowName, componentName)\n" +
    "****    Capture an image of the specified component and save it to the specified file.\n" +
    "close()\n" +
    "****    Pop the window out of the stack. The next operation takes place\n" +
    "****    only when the Window below the stack is focused or a new Window\n" +
    "****    call is made.\n" +
    "doubleclick(componentName, o1=None, o2=None, o3=None, o4=None)\n" +
    "****    Send a double click to the component\n" +
    "drag_and_drop(source, sourceinfo, target, targetinfo, action)\n" +
    "****    Recording sequence for a drag and drop operation. Marathon uses a Clipboard copy and paste\n" +
    "****    to perform the operation.\n" +
    "dump_components()\n" +
    "****    Get a list of all visible components in the current window\n" +
    "get_component(componentName)\n" +
    "****    Get the Java component represented by the given name\n" +
    "get_named_components()\n" +
    "****    Get a map that contains components for the current window\n" +
    "get_p(component, property, componentInfo=None)\n" +
    "****    Get a property for the given component. Note that what is returned is a String representation\n" +
    "****    of the property\n" +
    "get_po(component, property, componentInfo=None)\n" +
    "****    Get a property for the given component. Note that what is returned is a Java object\n" +
    "get_window()\n" +
    "****    Gets the title of the current window\n" +
    "image_compare(path1, path2, differencesInPercent=0)\n" +
    "****    Compare two images defined by their paths, returns their differences in an array [0] is no. of different pixels, [1] is the percentage.\n" +
    "keystroke(componentName, keysequence, componentInfo=None)\n" +
    "****    Send the given keysequence to the application. Keysequence are\n" +
    "****    of the form [modifier]+[modifier]+...+[keystroke]. If the given\n" +
    "****    keysequence is a single character like 'A' - the corresponding\n" +
    "****    keystroke (Shift+A) is sent to the application.\n" +
    "rightclick(componentName, o1=None, o2=None, o3=None, o4=None, o5=None)\n" +
    "****    Send a right click to the component\n" +
    "screen_capture(fileName)\n" +
    "****    Capture an image of the current screen and save it to the specified file.\n" +
    "select(componentName, text, componentInfo=None)\n" +
    "****    Select a given component and set the state corresponding to the given text.\n" +
    "select_menu(menuitems, keystroke=None)\n" +
    "****    Select a given menu item. Menu items are separated by '>>'\n" +
    "****    If a keystroke is given - the given keystroke is used to\n" +
    "****    activate the menu.\n" +
    "window(windowTitle, timeout=0)\n" +
    "****    Wait for a window to appear. The default timeout is 30seconds\n" +
    "window_capture(fileName, windowName)\n" +
    "****    Capture an image of the specified window and save it to the specified file.\n"
end
