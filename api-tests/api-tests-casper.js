/* globals casper: false, document: false, console: false, require: false */
"use strict";
var url = "http://localhost:8888/_ah/login?" +
          "continue=http://localhost:8055/api-test-page.html"
   ,noPasswordMessage="\n\nGoogle Sign-in requested.\n\n" +
     "For first run or expired cache use\ncasperjs " +
     "--cookies-file=$HOME/.api-test-cookies test api-tests-casper.js" +
     " --password=password\n" +
     "This can be done from the api-tests directory via\n" +
     "./run-tests --password=password\n\n" +
     "Also make sure appengine devserver is running";

casper.options.waitTimeout = 40000;

casper.options.onWaitTimeout = function() {
  casper.echo("Wait Timeout");
  casper.captureSelector("waitTimeout.png", "body");
  casper.exit(1);
};

casper.on("remote.message", function(msg) {
  casper.echo("DOM console: " + msg);
});

casper.test.begin('Connecting to ' + url, function suite(test) {
  casper.start(url, function(resp) {
    casper.echo('Response ' + resp.status + " " + resp.statusText +
              ' from ' + resp.url);
  });

  casper.then(function() {
    casper.evaluate(function() {
      document.getElementById("isAdmin").checked = true;
      document.getElementById("email").value = "jenkins@risevision.com";
    });
    casper.click("#btn-login");
  });

  casper.then(waitForAuthResponse);
  
  casper.then(function() {
    if (checkResponse() !== "authorized") { 
      casper.echo("Authorization not granted", "ERROR");
      casper.exit(1);
    }

    function checkResponse() {
      return casper.evaluate(function() {
        return document.getElementById("response").innerHTML;
      });
    }
  });

  function waitForAuthResponse() {
    casper.waitFor(function() {
      return casper.evaluate(function() {
        return document.getElementById("response").innerHTML
                       .indexOf("authorized") > -1;
      });
    });
  }

  casper.on("popup.loaded", 
    function(page) {
      casper.echo("Pop up detected: " + page.title);
      if (page.title !== "Sign in - Google Accounts") {
        return;
      }

      if (!casper.cli.options.password) {
        casper.echo(noPasswordMessage, "ERROR");
      } else {
        casper.echo("Signing in");
        page.evaluate(function(pass) {
          document.querySelector('#Email').value = "jenkins@risevision.com";
          document.querySelector("#Passwd").value = pass;
          document.querySelector("#signIn").click();
        }, casper.cli.options.password);
      }
    }
  );


  casper.then(function() {
    casper.echo("Creating bucket.");
    casper.click("#createBucket");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    casper.echo("Creating file.");
    casper.click("#createFile");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", 'Without error: true');
  });

  casper.then(function() {
    casper.echo("Checking single file public read removal");
    casper.click("#removePublicReadOneFile");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    checkPublicReadPermission("test1");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    casper.test.assert
    (jsonResponse.error && jsonResponse.error.code === 401 , "Public read denied.");
  });

  casper.then(function() {
    casper.echo("Checking single file public read addition");
    casper.click("#addPublicReadOneFile");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    checkPublicReadPermission("test1")
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    casper.test.assert(jsonResponse.kind === "storage#object", "Public read granted.");
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
    casper.echo("Checking Root Folder Refresh Folders Only.");
    casper.click("#refreshRootFolder");
    casper.click("#refreshGoogleAPIRootFolder");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", 'result":true');
    this.test.assertSelectorHasText("#storageAPIFilesCount", "0");
    this.test.assertSelectorHasText("#googleAPIFilesCount", "0");
    this.test.assertSelectorHasText("#storageAPIFoldersCount", "1");
    this.test.assertSelectorHasText("#googleAPIFoldersCount", "1");
  });

  casper.then(function() {
    casper.echo("Creating files.");
    casper.click("#createFiles");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", 'Without error: true');
  });

  casper.then(function() {
    casper.echo("Checking Root Folder Refresh Folders and Files.");
    casper.click("#refreshRootFolder");
    casper.click("#refreshGoogleAPIRootFolder");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", 'result":true');
    this.test.assertSelectorHasText("#storageAPIFilesCount", "3");
    this.test.assertSelectorHasText("#googleAPIFilesCount", "3");
    this.test.assertSelectorHasText("#storageAPIFoldersCount", "1");
    this.test.assertSelectorHasText("#googleAPIFoldersCount", "1");
  });

  casper.then(function() {
    casper.echo("Checking File List refresh inside SubFolder.");
    casper.click("#refreshSubFolder");
    casper.click("#refreshGoogleAPISubFolder");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    //check for 2 files because of the previous folder file.
    this.test.assertSelectorHasText("#response", 'result":true');
    this.test.assertSelectorHasText("#storageAPIFilesCount", "2");
    this.test.assertSelectorHasText("#googleAPIFilesCount", "2");
    this.test.assertSelectorHasText("#storageAPIFoldersCount", "0");
    this.test.assertSelectorHasText("#googleAPIFoldersCount", "0");
  });

  casper.then(function() {
    casper.echo("Checking batch acl public read removal");
    casper.click("#removePublicReadBucket");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    checkPublicReadPermission("test2");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    casper.test.assert
    (jsonResponse.error && jsonResponse.error.code === 401 , "Public read denied.");
  });

  casper.then(function() {
    casper.echo("Checking batch acl public read addition");
    casper.click("#addPublicReadBucket");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    checkPublicReadPermission("test2");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    casper.test.assert(jsonResponse.kind === "storage#object", "Public read granted.");
  });

  casper.then(function() {
    casper.echo("Deleting folder.");
    casper.click("#deleteFolder");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    casper.echo("Checking Root Folder Refresh Files Only.");
    casper.click("#refreshRootFolder");
    casper.click("#refreshGoogleAPIRootFolder");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", 'result":true');
    this.test.assertSelectorHasText("#storageAPIFilesCount", "3");
    this.test.assertSelectorHasText("#googleAPIFilesCount", "3");
    this.test.assertSelectorHasText("#storageAPIFoldersCount", "0");
    this.test.assertSelectorHasText("#googleAPIFoldersCount", "0");
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
    casper.echo("Attempting to create bucket with incorrect company");
    casper.click("#createBucketWrongCompany");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", '"result":false');
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

  casper.then(function() {
    casper.echo("Invalidating auth token");
    casper.click("#invalidateToken");
  });

  casper.then(function() {
    casper.echo("Attempting to create bucket with no token");
    casper.click("#noTokenBucket");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", 'No user');
  });

  casper.then(function() {
    casper.echo("Attempting to create folder with no token");
    casper.click("#noTokenFolder");
  });

  casper.then(function() {
    casper.waitUntilVisible("#response");
  });

  casper.then(function() {
    this.test.assertSelectorHasText("#response", 'No user');
  });

  casper.run(function() {
    test.done();
  });

  function checkPublicReadPermission(filename) {
    var uri = casper.getHTML("#bucketPath") + "/o/" + filename;
    var curl = require("child_process").spawn("curl", uri);

    casper.evaluate(function() {
      document.getElementById("response").style.display = "none";
    });

    console.log("uri:" + uri);
    curl.stdout.on("data", function(data) {
      var jsonResponse = JSON.parse(data);
      console.log("public read result: " + data);
      casper.evaluate(function() {
        document.getElementById("response").innerHTML = data;
        document.getElementById("response").style.display = "inline";
      });
    });
  }
});


