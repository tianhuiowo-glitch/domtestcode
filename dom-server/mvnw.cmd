@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements. See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership. The ASF licenses this file to
@REM you under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0")
@IF NOT "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%__MVNW_ARG0_NAME__%")

@SET MAVEN_PROJECTBASEDIR=%BASE_DIR%
@IF NOT "%MAVEN_BASEDIR%"=="" (SET "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%")

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
@IF "%MVN_CMD%"=="" (SET "MVN_CMD=mvn")

@IF EXIST %WRAPPER_JAR% (
    @SET MVNW_REPOURL=
) ELSE (
    @SET MVNW_REPOURL=%DOWNLOAD_URL%
    @powershell -Command "&{"^
        "$webclient = new-object System.Net.WebClient;"^
        "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
        "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
        "}"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%DOWNLOAD_URL%', %WRAPPER_JAR%)"^
        "}"
    @IF "%ERRORLEVEL%"=="0" GOTO execute
    @ECHO "Failed to download the Maven Wrapper JAR"
    @EXIT /b 1
)

:execute
@SET JAVA_EXE=%JAVA_HOME%/bin/java.exe
@IF NOT EXIST "%JAVA_EXE%" SET "JAVA_EXE=java"

@SET JVMCONFIG_CMD_LINE_ARGS=
@FOR /F "usebackq tokens=*" %%j IN (`"%JAVA_EXE%" -jar %WRAPPER_JAR% %MVNW_REPOURL% %*`) DO (
    @SET JVMCONFIG_CMD_LINE_ARGS=!JVMCONFIG_CMD_LINE_ARGS! %%j
)

@"%JAVA_EXE%" %JVMCONFIG_CMD_LINE_ARGS% -jar %WRAPPER_JAR% %MVNW_REPOURL% %*
