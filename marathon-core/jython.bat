@ECHO OFF

REM    $Id: jython.bat 2 2008-10-01 12:19:08Z kd $

SETLOCAL

REM
REM Works on NT/2000/XP - Sets DIST to directory of the script
REM

SET DIST=%~dp0

SET CLASSPATH=%DIST%Support\jython-2.2\jython.jar;%CLASSPATH%

java -Dmarathon.home="%DIST%." -Dpython.home="%DIST%." net.sourceforge.marathon.Main "org.python.util.jython" %1 %2 %3 %4 %5 %6 %7 %8 %9

ENDLOCAL
