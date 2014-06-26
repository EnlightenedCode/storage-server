"use strict";
var url = "http://localhost:8055/api-test-page.html"
   ,noPasswordMessage="For first invocation use casperjs " +
     "--cookies-file=../../cookies test api-tests-casper.js" +
     " --password=password\nAlso make sure appengine devserver is running";

casper.test.begin('Connecting to ' + url, function suite(test) {
  casper.start(url, function(resp) {
    this.echo('Response ' + resp.status + " " + resp.statusText +
              ' from ' + resp.url);
  });

  casper.then(function() {
    casper.waitFor(function() {
                     return casper.evaluate(function() {
                       return document.getElementById("response").innerHTML === "logged-in";
                     });},
                   function() {},
                   function(){signIn();});
  });

  function signIn() {
    casper.then(function() {
      if (! casper.cli.options.password) {
        casper.echo(noPasswordMessage, "error");
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

    casper.then(function() {
      casper.waitFor(function() {
                       return casper.evaluate(function() {
                         return document.getElementById("response").innerHTML === "logged-in";
                       });}
    );});
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

  casper.run(function() {
    test.done();
  });
});
