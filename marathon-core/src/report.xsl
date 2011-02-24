<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:date="http://exslt.org/dates-and-times" version="1.0">
  <xsl:output method="html" encoding="UTF-8" indent="yes" version="1.0" omit-xml-declaration="yes" media-type="text/html" standalone="yes"/>

  <xsl:strip-space elements="*" />
  <xsl:template match="/">
    <html>
      <head>
        <title>Marathon Acceptance Test Results</title>
        <style type="text/css">
          body {
            font:normal 68% verdana,arial,helvetica;
            color:#000000;
          }
          table tr td, table tr th {
            font-size: 68%;
          }
          table.details tr th{
            font-weight: bold;
            text-align:left;
            background:#a6caf0;
          }
          table.details tr td{
            background:#eeeee0;
          }

          p {
            line-height:1.5em;
            margin-top:0.5em; margin-bottom:1.0em;
          }
          h1 {
            margin: 0px 0px 5px; font: 165% verdana,arial,helvetica
          }
          h2 {
            margin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica
          }
          h3 {
            margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica
          }
          h4 {
            margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
          }
          h5 {
            margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
          }
          h6 {
            margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
          }
          .Error {
            font-weight:bold; color:red;
          }
          .Failure {
            font-weight:bold; color:purple;
          }
          .Properties {
            text-align:right;
          }
        </style>
      </head>
      <body>
        <xsl:call-template name="pageHeader"/> 
        <xsl:call-template name="summary"/>
        <xsl:call-template name="packagelist"/>
        <xsl:call-template name="testcases"/>
        <xsl:call-template name="checklist"/>
        <xsl:call-template name="screencaptures"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="pageHeader">
    <xsl:variable name="now" select="date:date-time()"/>
    <h1><xsl:value-of select="/test/@projectname"/> - Results</h1>
    <table width="100%">
      <tr>
        <td align="left">Generated on <b><xsl:value-of select="concat(date:day-name(), ' ', date:month-abbreviation(),' ',date:day-in-month(),' ',date:hour-in-day(),':',date:minute-in-hour(),':',date:second-in-minute(),' ',date:year())"/></b></td>
      </tr>
    </table>
    <hr size="1"/>
  </xsl:template>

  <xsl:template name="summary">
    <h2>Summary</h2>
    <xsl:variable name="testCount" select="count(//testcase)"/>
    <xsl:variable name="errorCount" select="count(//testcase[@status = '1'])"/>
    <xsl:variable name="failureCount" select="count(//testcase[@status = '2'])"/>
    <xsl:variable name="timeCount" select="sum(//testcase/@time)"/>
    <xsl:variable name="successRate" select="($testCount - $failureCount - $errorCount) div $testCount"/>
    <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
      <tr valign="top">
        <th>Tests</th>
        <th>Failures</th>
        <th>Errors</th>
        <th>Success rate</th>
        <th>Time</th>
      </tr>
      <tr valign="top">
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
            <xsl:when test="$errorCount &gt; 0">Error</xsl:when>
          </xsl:choose>
        </xsl:attribute>
        <td><xsl:value-of select="$testCount"/></td>
        <td><xsl:value-of select="$failureCount"/></td>
        <td><xsl:value-of select="$errorCount"/></td>
        <td><xsl:value-of select="format-number($successRate,'0.00%')"/></td>
        <td><xsl:value-of select="format-number($timeCount,'0.000')"/></td>
      </tr>
    </table>
    <hr size="1"/>
  </xsl:template>

  <xsl:template name="packagelist">   
    <h2>Packages</h2>
    Note: package statistics are not computed recursively, they only sum up all of its testsuites numbers.
    <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
      <xsl:call-template name="testsuite.test.header"/>
      <xsl:for-each select="/test//testsuite">
        <xsl:variable name="testCount" select="count(testcase)"/>
        <xsl:variable name="errorCount" select="count(testcase[@status = '1'])"/>
        <xsl:variable name="failureCount" select="count(testcase[@status = '2'])"/>
        <xsl:variable name="timeCount" select="sum(testcase/@time)"/>

        <tr valign="top">
          <xsl:attribute name="class">
            <xsl:choose>
              <xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
              <xsl:when test="$errorCount &gt; 0">Error</xsl:when>
            </xsl:choose>
          </xsl:attribute>
          <td><a href="#{@name}"><xsl:value-of select="@name"/></a></td>
          <td><xsl:value-of select="$testCount"/></td>
          <td><xsl:value-of select="$failureCount"/></td>
          <td><xsl:value-of select="$errorCount"/></td>
          <td><xsl:value-of select="format-number($timeCount,'0.000')"/></td>
        </tr>
      </xsl:for-each>
    </table>
    <hr size="1"/>
  </xsl:template>

  <xsl:template name="testcases">
    <xsl:for-each select="/test//testsuite">
      <a name="{@name}"/><h2>Test <xsl:value-of select="@name"/></h2>
      <xsl:variable name="testsuitename" select="@name"/>
      <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
        <xsl:call-template name="testcase.test.header"/>
        <xsl:for-each select="testcase">
          <tr valign="top">
            <xsl:attribute name="class">
              <xsl:choose>
                <xsl:when test="@status = 2">Error</xsl:when>
                <xsl:when test="@status = 1">Error</xsl:when>
              </xsl:choose>
            </xsl:attribute>
            <td><xsl:value-of select="@name"/></td>
            <xsl:variable name="testname" select="@name"/>
            <xsl:choose>
              <xsl:when test="@status = 1">
                <td>Error</td>
              </xsl:when>
              <xsl:when test="@status = 2">
                <td>Failure</td>
              </xsl:when>
              <xsl:otherwise>
                <td>Success</td>
              </xsl:otherwise>
            </xsl:choose>
            <td width="10%">
              <xsl:for-each select="checklist">
                <a href="#{@name}-{@index}-{$testsuitename}-{$testname}"><xsl:value-of select="@name"/></a>
                (<xsl:value-of select="@status"/>)
                <br/>
              </xsl:for-each>
            </td>
            <td>
              <xsl:for-each select="screen_captures/screen_capture">
                <a href="#{@file}"><xsl:value-of select="@file"/></a><br/>
              </xsl:for-each>
            </td>
            <td><xsl:call-template name="br-replace"><xsl:with-param name="word" select="."/></xsl:call-template></td>
            <td><xsl:value-of select="format-number(@time,'0.000')"/></td>
          </tr>
        </xsl:for-each>
      </table>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="checklist">
    <xsl:for-each select="/test//testsuite">
      <xsl:variable name="testsuitename" select="@name"/>
      <xsl:for-each select="testcase">
        <xsl:variable name="testname" select="@name"/>
        <xsl:for-each select="checklist">
          <a name="{@name}-{@index}-{$testsuitename}-{$testname}"/>
          <h2>Checklist <xsl:value-of select="@name"/></h2>
          <h3><xsl:value-of select="$testname"/>(<xsl:value-of select="$testsuitename"/>)</h3>
          <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
            <xsl:call-template name="checklist.test.header"/>
            <xsl:for-each select="checkitem">
              <tr valign="top">
                <xsl:choose>
                  <xsl:when test="@type = 'header'">
                    <td><b><xsl:value-of select="@label"/></b></td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td><xsl:value-of select="@label"/></td>
                  </xsl:otherwise>
                </xsl:choose>
                <td>
                  <xsl:choose>
                    <xsl:when test="@selected = 1">
                      Success
                    </xsl:when>
                    <xsl:when test="@selected = 3">
                      Failure
                    </xsl:when>
                    <xsl:when test="@selected = 2">
                      Notes
                    </xsl:when>
                  </xsl:choose>
                </td>
                <td><xsl:value-of select="@text"/></td>
              </tr>
            </xsl:for-each>
          </table>
          <xsl:if test="@capture">
            <hr/>
            <xsl:variable name="checklistname" select="@name"/>
            <xsl:variable name="checklistindex" select="@index"/>
			<map name="map-{$checklistname}-{$checklistindex}-{$testsuitename}-{$testname}">
				<xsl:for-each select="annotations/annotation">
					<area shape="rect" coords="{@x}, {@y}, {@x+@w}, {@y+@h}" href="#a-{$checklistname}-{$checklistindex}-{$testsuitename}-{$testname}-{@x}-{@y}" />
				</xsl:for-each>
			</map>

			<img src="{@capture}" usemap="#map-{$checklistname}-{$checklistindex}-{$testsuitename}-{$testname}"></img>
            
            <!-- <applet codebase="../" code="net/sourceforge/marathon/screencapture/MarathonAnnotateApplet.class" width="100%" height="600">
                 alt="Your browser understands the &lt;APPLET&gt; tag but isn't running the applet, for some reason."
                 Your browser is completely ignoring the &lt;APPLET&gt; tag!
              <param name="IMAGE" value="/{/test/@reportdir}/{@capture}"/>
            </applet> -->
            <ol>
				<xsl:for-each select="annotations/annotation">
					<a name="a-{$checklistname}-{$checklistindex}-{$testsuitename}-{$testname}-{@x}-{@y}"><li><xsl:value-of select="@text"/></li></a>
				</xsl:for-each>
			</ol>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="testsuite.test.header">
    <tr valign="top">
      <th width="80%">Name</th>
      <th>Tests</th>
      <th>Failures</th>
      <th>Errors</th>
      <th nowrap="nowrap">Time(s)</th>
    </tr>
  </xsl:template>

  <xsl:template name="testcase.test.header">
    <tr valign="top">
      <th>Name</th>
      <th>Status</th>
      <th>Checklist</th>
      <th>Screen Captures</th>
      <th width="80%">Type</th>
      <th nowrap="nowrap">Time(s)</th>
    </tr>
  </xsl:template>

  <xsl:template name="checklist.test.header">
    <tr valign="top">
      <th>Name</th>
      <th>Selected</th>
      <th width="70%">Text</th>
    </tr>
  </xsl:template>


  <xsl:template name="br-replace">
    <xsl:param name="word"/>
      <xsl:choose>
      <xsl:when test="contains($word,'&#10;')">
        <xsl:value-of select="substring-before($word,'&#10;')"/>
        <br/>
        <xsl:call-template name="br-replace">
          <xsl:with-param name="word" select="substring-after($word,'&#10;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$word"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="screencaptures">
    <xsl:for-each select="/test//testsuite">
      <xsl:variable name="testsuitename" select="@name"/>
      <xsl:for-each select="testcase">
        <xsl:variable name="testname" select="@name"/>
        <xsl:for-each select="screen_captures/screen_capture">
          <a name="{@file}"/>
          <h2>Screencapture <xsl:value-of select="@file"/></h2>
          <h3><xsl:value-of select="$testname"/>(<xsl:value-of select="$testsuitename"/>)</h3>
          <img src="{@file}" width="600"/>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>

