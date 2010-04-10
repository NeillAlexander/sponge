@echo off

REM Build the classpath using SPONGE_HOME as the reference point
if "%SPONGE_HOME%" == "" goto error

set CLASSPATH=%SPONGE_HOME%\lib\*;%SPONGE_HOME%\config

set SPONGE_SESSIONS="%SPONGE_HOME%\sessions"

java -Xms128m -Xmx256m -Dsponge.sessions="%SPONGE_SESSIONS%" -Dsponge.home="%SPONGE_HOME%" com.nwalex.sponge.Client

goto :end

:error
echo Set SPONGE_HOME to point to the sponge installation directory

:end
