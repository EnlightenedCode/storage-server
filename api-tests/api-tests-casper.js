"use strict";
var url = "http://localhost:8055/api-test-page.html"
   ,noPasswordMessage="\n\nGoogle Sign-in requested.\n\n" +
     "For first run or expired cache use\ncasperjs " +
     "--cookies-file=../../cookies test api-tests-casper.js" +
     " --password=password\n" +
     "This can be done from the api-tests directory via\n" +
     "./run-tests --password=password\n\n" +
     "Also make sure appengine devserver is running";

casper.options.waitTimeout = 10000;
casper.test.begin('Connecting to ' + url, function suite(test) {
  casper.start(url, function(resp) {
    this.echo('Response ' + resp.status + " " + resp.statusText +
              ' from ' + resp.url);
  });

  casper.then(function() {waitForLogin(true);});

  function waitForLogin(signInOnFail) {
    casper.waitFor(function() {
      return casper.evaluate(function() {
        return document.getElementById("response").innerHTML === "logged-in";
      });
    }, undefined, signInOnFail ? function(){signIn();} : undefined);
  }

  function signIn() {
    casper.then(function() {
      if (! casper.cli.options.password) {
        casper.echo(noPasswordMessage, "ERROR");
        casper.exit(1);
      }
      casper.waitForPopup("ServiceLogin");
    });

    casper.then(function() {
      casper.withPopup("ServiceLogin", function() {
        this.test.assertTitle("Sign in - Google Accounts");
        var loginFormId = casper.evaluate(function() {
          return document.querySelector('#Email').parentNode.id;
        });

        this.fill('form#' + loginFormId, {
          "Email": "jenkins@risevision.com"
          ,"Passwd": casper.cli.options.password
        }, true);
      });
    });

    casper.then(function() {waitForLogin(false);});
  }

  casper.on("popup.closed", function() {
    casper.evaluate(function() {
      document.getElementById("response").innerHTML = "logged-in";
    });
  });

  casper.then(function() {
    casper.echo("Creating bucket.");
    casper.click("#createBucket");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '"result":true');
  });

  casper.then(function() {
    casper.echo("Creating file.");
    casper.click("#createFile");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '{"kind"');
  });

  casper.then(function() {
    casper.echo("Deleting file.");
    casper.click("#deleteFile");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '"result":true');
  });

  casper.then(function() {
    casper.echo("Creating folder.");
    casper.click("#createFolder");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '"result":true');
  });

  casper.then(function() {
    casper.echo("Creating files.");
    casper.click("#createFiles");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '{"kind"');
  });

  casper.then(function() {
    casper.echo("Deleting files.");
    casper.click("#deleteFiles");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '"result":true');
  });

  casper.then(function() {
    casper.echo("Deleting folder.");
    casper.click("#deleteFolder");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '"result":true');
  });

  casper.then(function() {
    casper.echo("Deleting bucket.");
    casper.click("#deleteBucket");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '"result":true');
  });

  casper.then(function() {
    casper.echo("Attempting to create folder with missing company.");
    casper.click("#createFolderMissingCompany");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '"result":false');
  });

  casper.then(function() {
    casper.echo("Attempting to create folder with missing folder.");
    casper.click("#createFolderMissingFolder");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '"result":false');
  });

  casper.run(function() {
    test.done();
  });
});
