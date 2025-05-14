@echo off
setlocal
set DIR=%~dp0
set JAVA_CMD=java

if defined JAVA_HOME (
  set JAVA_CMD=%JAVA_HOME%\bin\java
)

"%JAVA_CMD%" -cp "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
