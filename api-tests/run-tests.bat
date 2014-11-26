echo starting local node server...
call npm install connect serve-static
start cmd /k node server
cd ..
echo starting appengine devserver
call start mvn appengine:devserver
echo waiting for server to startup
TIMEOUT 30
cd api-tests
start /WAIT casperjs --cookies-file=%USERPROFILE%/.api-test-cookies test api-tests-casper.js --password=%1
cd ..
echo stopping appengine devserver
call mvn appengine:devserver_stop
echo stopping all local node servers
taskkill /F /IM node.exe
cd api-tests
