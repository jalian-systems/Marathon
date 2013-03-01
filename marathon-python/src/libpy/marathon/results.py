from net.sourceforge.marathon.api import SourceLine
from java.lang import System
import jarray
import sys

class collector:
	def __init__(self, result, exclude):
		self.playbackresult = result
		self.exclude = exclude

	def callprotected(self, function, result, *args):
		self.playbackresult = result
		import java.lang.Throwable
		try:			
			function.__call__(*args)
			return 1
		except java.lang.Throwable:
			(exc_type, e, tb) = sys.exc_info()
			self.addjavaerror(e, tb)
			raise
		except:
			(exc_type, e, tb) = sys.exc_info()
			self.addpythonerror(exc_type, e, tb)
			raise
		return 0

	def addfailure(self, message, result):
		self.playbackresult = result
		assertion.assertionFailed()
		frame = sys._getframe(0)
		self._addfailure(message, frame)

	def addpythonerror(self, exception_type, exception, traceback):
		try:
			self._addfailure(str(exception_type) + ': ' + exception.__str__(), _firstframe(traceback))
		except:
			self._addfailure("Unknown Error:",_firstframe(traceback))

	def addjavaerror(self, exception, traceback):
		self._addfailure(exception.getMessage(), _firstframe(traceback))

	def _addfailure(self, message, frame):
		lines = []
		while frame != None:
			code = frame.f_code
			filename = code.co_filename
			print filename
			dir = System.getProperty('marathon.project.dir')
			if filename.find(dir) == 0 or filename.find('Untitled') == 0:
				lines.append(SourceLine(code.co_filename, code.co_name, frame.f_lineno))

			frame = frame.f_back

		self.playbackresult.addFailure(message, jarray.array(lines, SourceLine))

def _firstframe(traceback):
	while traceback.tb_next != None:
		traceback = traceback.tb_next
	return traceback.tb_frame

