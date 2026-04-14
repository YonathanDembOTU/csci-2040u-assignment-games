@echo off

cd /d ..\src\website

echo Installing dependencies...
call npm install

echo Setting test script...
call npm pkg set scripts.test="jest --verbose"

echo Running tests...
call npm test

PAUSE
