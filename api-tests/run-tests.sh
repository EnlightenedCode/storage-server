echo "$(tput setaf 2)Starting local node server $(tput sgr0)" && \
npm install connect serve-static && \
sh -c 'node server &' && \
cd .. && \
echo "$(tput setaf 2)Starting appengine devserver$(tput sgr0)" && \
mvn appengine:devserver_start && \
cd api-tests && \
casperjs --cookies-file=../../cookies test api-tests-casper.js $1 ; \
RET_CODE=$? ;
cd .. ; \
echo "$(tput setaf 2)Stopping appengine devserver$(tput sgr0)" ; \
mvn appengine:devserver_stop ; \
echo "$(tput setaf 2)Stopping local node server $(tput sgr0)" ; \
kill $(ps aux |grep "node server" | head -n 1 | awk '{print $2}')
exit $RET_CODE
