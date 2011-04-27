#this file is here for testing purposes only
#While I personally abhor distributing test-only code
#with production code, I also firmly believe that it is
#better to do this than to let bits of code go untested
#therefore this file, which does nothing but hold a static 
#integer value is used to make sure that the static value
#is reset to zero, every time that the python interpreter
#is invoked, and therefore indicating that modules are
#being reloaded in between interpreter invocations

staticint = 0

def incr():
	global staticint
	staticint += 1



