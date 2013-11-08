@ECHO OFF

REM    $Id: marathon.bat 176 2008-12-22 11:04:49Z kd $
REM    Copyright (C) 2006 Jalian Systems Private Ltd.
REM    Copyright (C) 2006 Contributors to Marathon OSS Project
REM
REM    This library is free software; you can redistribute it and/or
REM    modify it under the terms of the GNU Library General Public
REM    License as published by the Free Software Foundation; either
REM    version 2 of the License, or (at your option) any later version.
REM
REM    This library is distributed in the hope that it will be useful,
REM    but WITHOUT ANY WARRANTY; without even the implied warranty of
REM    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
REM    Library General Public License for more details.
REM
REM    You should have received a copy of the GNU Library General Public
REM    License along with this library; if not, write to the Free Software
REM    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
REM
REM	Project website: http://marathonman.sourceforge.net
REM	Help: Marathon help forum @ http://sourceforge.net/projects/marathonman
REM	Developer Mailing List: marathonman-devel@lists.sourceforge.net
REM

SETLOCAL

REM
REM Works on NT/2000/XP - Sets DIST to directory of the script
REM

SET DIST=%~dp0

IF DEFINED DIST goto :distdefined
SET DIST=%MARATHON_HOME%
IF NOT DEFINED DIST goto :nodist
SET DIST=%MARATHON_HOME%\
:distdefined

SET CLASSPATH=%DIST%;%DIST%marathon.jar
SET CLASSPATH=%CLASSPATH%;%DIST%support/forms-1.2.1/forms-1.2.1.jar;%DIST%support/jaccess-1.3/jaccess.jar;%DIST%support/jline-0.9.93.jar;%DIST%support/junit4.8.2/junit-4.8.2.jar;%DIST%support/looks-2.2.0/looks-2.2.0.jar;%DIST%support/vldocking-3.0.0/src/jar/vldocking-3.0.0.jar;%DIST%support/guice-3.0/guice-3.0.jar;%DIST%support/guice-3.0/aopalliance.jar;%DIST%support/guice-3.0/javax.inject.jar;%DIST%support/BrowserLauncher2-all-1_3.jar;%DIST%support/RSyntaxTextArea/dist/rsyntaxtextarea.jar;%DIST%support/snakeyaml/target/snakeyaml-1.11.jar;%DIST%support/osxutil/dist/osxutil.jar;%DIST%support/opencsv/deploy/opencsv-2.2.jar;%DIST%/support/jython\jython-standalone-2.5.3.jar;%DIST%/support/jruby\jruby-complete-1.7.2.jar;;%DIST%/support/JRubyParser.jar

for %%i in (%1 %2 %3 %4 %5 %6 %7 %8 %9) do if %%i==-batch goto :batch
for %%i in (%1 %2 %3 %4 %5 %6 %7 %8 %9) do if %%i==-b goto :batch
for %%i in (%1 %2 %3 %4 %5 %6 %7 %8 %9) do if %%i==-h goto :batch
for %%i in (%1 %2 %3 %4 %5 %6 %7 %8 %9) do if %%i==-help goto :batch
for %%i in (%1 %2 %3 %4 %5 %6 %7 %8 %9) do if %%i==-i goto :batch
for %%i in (%1 %2 %3 %4 %5 %6 %7 %8 %9) do if %%i==-ignore goto :batch
start javaw -Dmarathon.home="%DIST%." -Dpython.home="%DIST%." net.sourceforge.marathon.Main %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end
:batch
java -Dmarathon.home="%DIST%." -Dpython.home="%DIST%." net.sourceforge.marathon.Main %1 %2 %3 %4 %5 %6 %7 %8 %9
goto :end

:nodist
echo Could not find Marathon install directory...
echo Please set MARATHON_HOME to the directory where Marathon is installed.

:end

ENDLOCAL
