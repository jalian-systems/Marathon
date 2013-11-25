"""Marathon Jython Interfaces

	Interface routines to hook into Marathon runtime. These calls are used
	by Marathon to record operations on components. Some of the functions
	are utility functions that can be used while developing the test
	scripts.

"""

from net.sourceforge.marathon.api import ComponentId
from net.sourceforge.marathon.api import WindowHandler
from net.sourceforge.marathon.util import AssertionLogManager
from net.sourceforge.marathon.player import MarathonJava
from marathon import results

import net
import sys
import java

true = 1
false = 0

class Marathon(net.sourceforge.marathon.player.MarathonJava):
	def __init__(self):
		self.collector = results.collector(self.result, __file__)

	def execTest(self, test):
		self.collector.callprotected(test, self.result)

	def execFixtureSetup(self, fixture):
		self.collector.callprotected(fixture.setup, self.result)
		
	def execFixtureTeardown(self, fixture):
		write_assertions_to_file(__test_name__)
		self.collector.callprotected(fixture.teardown, self.result)

	def execTestSetup(self, fixture):
	    if hasattr(fixture, 'test_setup'):
			self.collector.callprotected(fixture.test_setup, self.result)

	def execTestTeardown(self, fixture):
	    if hasattr(fixture, 'test_teardown'):
			self.collector.callprotected(fixture.test_teardown, self.result)

	def handleFailure(self, e):
		if e.isAbortTestCase():
			net.sourceforge.marathon.player.MarathonJava.handleFailure(self, e)
		else:
			self.collector.addfailure(e.getMessage(), self.result)

assertion = AssertionLogManager.getInstance()
marathon = Marathon()

def window(windowTitle, timeout = 0):
	"""Wait for a window to appear. The default timeout is 30seconds"""

	marathon.window(windowTitle, timeout)
	return true

def frame(windowTitle, timeout = 0):
	"""Wait for a internal frame to appear. The default timeout is 30seconds"""

	marathon.frame(windowTitle, timeout)
	return true

def window_closed(windowTitle):
	"""Post a window close event"""

	marathon.windowClosed(windowTitle)

def window_changed(state):
	"""Post a window close event"""

	marathon.windowChanged(state)

def close():
	"""Pop the window out of the stack. The next operation takes place
	   only when the Window below the stack is focused or a new Window
	   call is made.
	"""

	marathon.close()

def select_menu(menuitems, keystroke=None):
	"""Select a given menu item. Menu items are separated by '>>'
		If a keystroke is given - the given keystroke is used to
		activate the menu.
	"""

	marathon.selectMenu(menuitems, keystroke)

def keystroke(componentName, keysequence, componentInfo=None):
	"""Send the given keysequence to the application. Keysequence are
		of the form [modifier]+[modifier]+...+[keystroke]. If the given
		keysequence is a single character like 'A' - the corresponding
		keystroke (Shift+A) is sent to the application.
	"""

	marathon.keystroke(ComponentId(componentName, componentInfo), keysequence)

def click(componentName, o1 = None, o2 = None, o3 = None, o4 = None, o5 = None):
	"""Send a click to the component"""

	marathon.click(componentName, false, o1, o2, o3, o4, o5)

def hover(componentName, delay = 500, componentInfo=None):
	"""Send a hover over the component"""

	marathon.hover(componentName, delay, componentInfo)

def mouse_pressed(componentName, o1 = None, o2 = None, o3 = None, o4 = None, o5 = None):
	"""Send a mouse pressed to the component"""
	
	marathon.mousePressed(componentName, false, o1, o2, o3, o4, o5)

def mouse_down(componentName, o1 = None, o2 = None, o3 = None, o4 = None, o5 = None):
	"""Send a mouse pressed to the component"""
	
	marathon.mousePressed(componentName, false, o1, o2, o3, o4, o5)

def mouse_released(componentName, o1 = None, o2 = None, o3 = None, o4 = None, o5 = None):
	"""Send a mouse released to the component"""
	
	marathon.mouseReleased(componentName, false, o1, o2, o3, o4, o5)

def mouse_up(componentName, o1 = None, o2 = None, o3 = None, o4 = None, o5 = None):
	"""Send a mouse released to the component"""

	marathon.mouseReleased(componentName, false, o1, o2, o3, o4, o5)

def drag(componentName, o1, o2, o3, o4, o5 = None, o6 = None):
	"""Send a drag to the component"""

	marathon.drag(componentName, o1, o2, o3, o4, o5, o6)

def doubleclick(componentName, o1 = None, o2 = None, o3 = None, o4 = None):
	"""Send a double click to the component"""

	marathon.click(componentName, false, 2, o1, o2, o3, o4)

def rightclick(componentName, o1 = None, o2 = None, o3 = None, o4 = None, o5 = None):
	"""Send a right click to the component"""

	marathon.click(componentName, true, o1, o2, o3, o4, o5)

def select(componentName, text, componentInfo=None):
	"""Select a given component and set the state corresponding to the given text."""

	marathon.select(ComponentId(componentName, componentInfo), text)

def get_component(componentName):
	"""Get the Java component represented by the given name"""

	return marathon.getComponent(ComponentId(componentName, None))

def get_mcomponent(componentName, componentInfo=None):
	"""Get the Java component represented by the given name"""

	return marathon.getMComponent(ComponentId(componentName, componentInfo))

def get_named_components():
	"""Get a map that contains components for the current window"""
	
	return marathon.getNamedComponents()

def dump_components():
        """Get a list of all visible components in the current window"""

        return marathon.dumpComponents()

def sleep(seconds):
	"""Sleep for the given number of seconds"""

	marathon.sleep(long(seconds))

def fail(message):
	"""Fail the test case with the given message"""

	marathon.fail(message)

def error(message):
	"""Fail the test case with the given message"""

	marathon.error(message)

def get_window():
	"""Gets the title of the current window"""

	return marathon.getWindow()

def get_window_object():
	"""Gets the current window"""

	return marathon.getWindowObject()

# Get frames

def get_frames():
	"""Gets all the internal frames in the current window"""
	
	return marathon.getFrames()
	
def get_frame_objects():
	"""Gets all the internal frame objects (with names) in the current window"""
	
	return marathon.getFrameObjects()

def drag_and_drop(source, sourceinfo, target, targetinfo, action):
	"""Recording sequence for a drag and drop operation. Marathon uses a Clipboard copy and paste
		to perform the operation.
	"""

	marathon.dragAndDrop(ComponentId(source, sourceinfo), ComponentId(target, targetinfo), action)

def assert_p(component, property, value, componentInfo=None):
	"""Main marathon assertion function. Assert that the given value of the property matches that
		of the component currently in the application.
	"""

	marathon.assertProperty(ComponentId(component, componentInfo), property, value)
	assertion.addAssertion("Content", str(component))

def wait_p(component, property, value, componentInfo=None):
	"""Main marathon assertion function. Assert that the given value of the property matches that
		of the component currently in the application.
	"""

	marathon.waitProperty(ComponentId(component, componentInfo), property, value)

def assertContent(componentName, content, componentInfo=None):
	marathon.assertContent(ComponentId(componentName, componentInfo), content)

def assert_content(componentName, content, componentInfo=None):
	marathon.assertContent(ComponentId(componentName, componentInfo), content)

def get_p(component, property, componentInfo=None):
	"""Get a property for the given component. Note that what is returned is a String representation
		of the property"""

	return marathon.getProperty(ComponentId(component, componentInfo), property)

def get_po(component, property, componentInfo=None):
	"""Get a property for the given component. Note that what is returned is a Java object""" 

	return marathon.getPropertyObject(ComponentId(component, componentInfo), property)

def screen_capture(fileName):
	"""Capture an image of the current screen and save it to the specified file."""

	return marathon.screenCapture(fileName)

def window_capture(fileName, windowName):
	"""Capture an image of the specified window and save it to the specified file."""

	return marathon.screenCapture(fileName, windowName)

def component_capture(fileName, windowName, componentName):
	"""Capture an image of the specified component and save it to the specified file."""
	return marathon.screenCapture(fileName, windowName, ComponentId(componentName, None))

def image_compare(path1, path2, differencesInPercent=0):
	"""Compare two images defined by their paths, returns their differences in an array [0] is no. of different pixels, [1] is the percentage."""
	return marathon.compareImages(path1,path2,differencesInPercent)

def files_equal(path1, path2):
	"""Compare the contents of two files"""

	return marathon.filesEqual(path1, path2)

def accept_checklist(filename):
	"""Show and accept input from the given checklist"""
	
	return __accept_checklist(filename)

def show_checklist(filename):
	"""Show data from the given checklist"""
	
	return __show_checklist(filename)

def assert_equals(expected, actual, message = None):
	"""Asserts that expected.equals(actual)"""
	
	marathon.assertEquals(message, expected, actual)

def assert_true(actual, message = None):
	"""Asserts that actual == true"""
	marathon.assertTrue(message, actual)
	
def use_data_file(filename):
	pass

def with_data(filename):
	return marathon.getDataReader(filename)

def get_java_recorded_version():
	global java_recorded_version
	return recorded_version

def set_java_recorded_version(jrv):
	global java_recorded_version
	java_recorded_version = jrv

# By default if the AUT exits, Marathon records that as an error. This flags turns off
# that behavior

from net.sourceforge.marathon.player import MarathonPlayer

def set_no_fail_on_exit(b):
	MarathonPlayer.exitIsNotAnError = b	



def write_assertions_to_file(testcase):
	assertionFile = open(__marathon_project_dir__ + "/TestReports/" + testcase + ".txt", "w")
	types = assertion.getTypes()
	assertions = assertion.getAssertions()
	passed = assertion.getPassed()
	for i in range(len(types)):
		assertionFile.write("<notes><passed>" + str(passed[i]) + "</passed><type>" + str(types[i]) + ": </type>" + "<assertion>" + str(assertions[i]) + "</assertion></notes>")
		assertionFile.write("\n")
	assertionFile.seek(0)

def marathon_help():

    print '''
click(componentName, o1=None, o2=None, o3=None, o4=None, o5=None)
****    Send a click to the component
close()
****    Pop the window out of the stack. The next operation takes place
****    only when the Window below the stack is focused or a new Window
****    call is made.
component_capture(fileName, windowName, componentName)
****    Capture an image of the specified component and save it to the specified file
doubleclick(componentName, o1=None, o2=None, o3=None, o4=None)
****    Send a double click to the component
drag_and_drop(source, sourceinfo, target, targetinfo, action)
****    Recording sequence for a drag and drop operation. Marathon uses a Clipboard copy and paste
****    to perform the operation.
dump_components()
****    Get a list of all visible components in the current window
get_component(componentName)
****    Get the Java component represented by the given name
get_named_components()
****    Get a map that contains components for the current window
get_p(component, property, componentInfo=None)
****    Get a property for the given component. Note that what is returned is a String representation
****    of the property
get_po(component, property, componentInfo=None)
****    Get a property for the given component. Note that what is returned is a Java object
get_window()
****    Gets the title of the current window
image_compare(path1, path2, differencesInPercent=0)
****    Compare two images defined by their paths, returns their differences in an array [0] is no. of
****    different pixels, [1] is the percentage.
keystroke(componentName, keysequence, componentInfo=None)
****    Send the given keysequence to the application. Keysequence are
****    of the form [modifier]+[modifier]+...+[keystroke]. If the given
****    keysequence is a single character like 'A' - the corresponding
****    keystroke (Shift+A) is sent to the application.
rightclick(componentName, o1=None, o2=None, o3=None, o4=None, o5=None)
****    Send a right click to the component
screen_capture(fileName)
****    Capture an image of the current screen and save it to the specified file.
select(componentName, text, componentInfo=None)
****    Select a given component and set the state corresponding to the given text.
select_menu(menuitems, keystroke=None)
****    Select a given menu item. Menu items are separated by '>>'
****    If a keystroke is given - the given keystroke is used to
****    activate the menu.
window(windowTitle, timeout=0)
****    Wait for a window to appear. The default timeout is 30seconds
window_capture(fileName, windowName)
****    Capture an image of the specified window and save it to the specified file.'''
