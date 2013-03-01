# $Id: results.rb 260 2009-01-13 05:53:15Z kd $
# 
java_import 'net.sourceforge.marathon.api.SourceLine'

class Collector
  def initialize()
    @playbackresult = nil
    @exclude = nil
  end

  def callprotected(function, result, *args)
    @playbackresult = result
      begin
        function.call(*args)
		  return 1
		rescue NativeException => e
		  addjavaerror(e)
		  raise
		rescue => e
		  addpythonerror(e)
		  raise
      end
      return 0
  end

  def addfailure(exception, result)
    @playbackresult = result
    $assertion.assertionFailed()
    backtrace = nil
    begin
      raise NameError
    rescue
      backtrace = $!.backtrace
    end
	_addfailure(exception.message, backtrace)
  end

  def addpythonerror(exception)
    _addfailure(exception.to_s + ": " + exception.message.to_s, exception.backtrace)
  end

  def addjavaerror(exception)
    _addfailure(exception.message, exception.backtrace)
  end

  def _addfailure(message, backtrace)
    lines = convert(backtrace)
    @playbackresult.addFailure(message, lines.to_java(SourceLine))
  end

  def convert(backtrace)
    backtrace.find_all { |item| not excluded(item) }.map { |item|
      item = item[6,item.length] if(item.index('file:/') == 0)
      matched = item.match(/(.*):(.*):(.*)/).to_a
      fname = "Unknown"
      fname = matched[3].match("`(.*)'").to_a[1] if matched[3]
      SourceLine.new(matched[1], fname, matched[2].to_i)
    }
  end

  def excluded(item)
    item = item[6,item.length] if(item.index('file:/') == 0)
    item =~ /(.*):(.*):(.*)/
    file = $1
    dir = java.lang.System.getProperty 'marathon.project.dir'
    return file.index(dir) != 0 && file.index('Untitled') != 0
  end
end
