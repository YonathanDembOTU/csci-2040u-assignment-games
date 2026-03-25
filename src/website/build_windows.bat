@echo off
echo Installing dependencies...
call npm install

echo Running tests...
call npm pkg set scripts.test="jest --verbose"
call npm test

echo Starting server...
node server.js
