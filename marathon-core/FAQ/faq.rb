# $Id: faq.rb 2 2008-10-01 12:19:08Z kd $
# vim:ts=2:sw=2:expandtab:

require 'cgi'

class FAQ
  attr_reader(:title, :content)

  def initialize(title)
    @title = title.sub(/^# */,'').chomp
    @content = nil
  end

  def <<(line)
    if line.chomp == ''
      @content << "<br/><br/>\n" if @content
    else
      @content = '' unless @content
      @content << CGI.escapeHTML(line)
    end
  end
end



def outputTOC(faqs)
  puts '<div style="border:1px solid #aaaaaa; background-color:#f9f9f9; padding:5px; font-size: 95%; margin: 10px 0px 10px 0px; width: 500px;">'
  puts '<p style="text-align:center;font-weight:bold">Table of Contents</p>'
  puts '  <ol>'
  n = 1
  for faq in faqs
    puts '    <li><a href="#faq-' + n.to_s + '">' + CGI.escapeHTML(faq.title) + '</a></li>'
    n += 1
  end
  puts '  </ol>'
  puts '</div>'
end




def outputContent(faqs)
  puts '<div style="padding:5px; font-size: 95%; margin: 10px 0px 10px 0px; width: 500px;">'
  n = 1
  for faq in faqs
    puts '<div>'
    puts '    <a name="faq-' + n.to_s + '"></a>'
    puts '<p style="margin-bottom: 0.3em;font-size: 110%;border-bottom: 1px dashed #aaaaaa;font-weight: bold;background-color: #f9f9f9;margin-top: 1.5em;">' + CGI.escapeHTML(faq.title) + '</p>'
    puts '<p>'
    puts faq.content
    puts '</p>'
    puts '</div>'
    n += 1
  end
  puts '</div>'
end

faqs = []
faq = nil

IO.foreach('faq.txt') { |line|
  if line.match(/^#/)
    faq = FAQ.new(line)
    faqs << faq
  else
    faq << line if faq
  end
}

outputTOC(faqs)
outputContent(faqs)
