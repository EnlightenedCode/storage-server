echo "$(tput setaf 2)Starting local node server $(tput sgr0)" && \
npm install connect serve-static && \
sh -c 'node server &' && \
cd .. && \
cd api-tests && \
casperjs --cookies-file=$HOME/.api-test-cookies test api-tests-casper.js $1 ;
RET_CODE=$? ;
exit $RET_CODE
