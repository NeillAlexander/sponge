@echo off

REM Build the classpath using SPONGE_HOME as the reference point
if "%SPONGE_HOME%" == "" goto error
set CLASSPATH=%SPONGE_HOME%\lib\*
java com.nwalex.sponge.Client

goto :end

:error
echo Set SPONGE_HOME to point to the sponge installation directory

:end
