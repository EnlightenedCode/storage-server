"use strict";
/* global gapi: true */
function init() {
  var ROOT = "http://localhost:8888/_ah/api"
     ,SCOPE = ["https://www.googleapis.com/auth/userinfo.email"
              ,"https://www.googleapis.com/auth/devstorage.full_control"]
     ,CLIENT = "614513768474.apps.googleusercontent.com";

  console.log("Initializing");
  gapi.client.load("storage", "v0.01", function() {
    console.log("Storage API loaded");
    gapi.client.load("oauth2", "v2", function() {
      console.log("Authorizing");
      gapi.auth
        .authorize({client_id: CLIENT, scope: SCOPE, immediate: false}
                  ,authCallback);
    });
  }, ROOT);

  function authCallback(resp) {
    if (resp.error) {
      document.getElementById("response").innerHTML = "not authorized: "
                                                      + resp.error;
    } else {
      document.getElementById("response").innerHTML = "authorized";
    }
  }
}

var randomId = Math.floor(Math.random()*90000) + 10000;
randomId = "api-test-" + randomId;

function createBucket() {
  storageApiCall("createBucket", {"companyId": randomId});
}

function createFolder() {
  storageApiCall("createFolder", {"companyId": randomId,
                                  "folder": "test folder"});
}

function deleteFolder() {
  storageApiCall("files.delete", {"companyId": randomId,
                                  "files": ["test folder/"]});
}

function deleteBucket() {
  storageApiCall("deleteBucket", {"companyId": randomId});
}

function createFolderMissingCompany() {
  storageApiCall("createFolder", {"folder": "test folder"});
}

function createFolderMissingFolder() {
  storageApiCall("createFolder", {"companyId": randomId});
}


function storageApiCall(commandString, paramObj) {
  var commandObject, commandArray;
  document.getElementById("response").style.display="none";
  commandArray = commandString.split(".");

  commandObject = gapi.client.storage;
  commandArray.forEach(function(val) {
    commandObject = commandObject[val];
  });
 
  commandObject(paramObj)
      .execute(function(resp) {
        document.getElementById("response").innerHTML=JSON.stringify(resp);
        document.getElementById("response").style.display="inline";
      });
}

function createFiles(fileNames) {
  if (fileNames.length === 0) {return;}
  document.getElementById("response").style.display="none";
  createFile(fileNames.shift());

  function createFile(fileName) {
    gapi.client.request({
      "path": "/upload/storage/v1/b/" + "risemedialibrary-" + randomId + "/o",
      "method": "POST",
      "params": {"uploadType": "media", "name": fileName},
      "body": {"media": {"data": "test file data"}}})
    .execute(function(resp) {
      processNextFile(resp);
    });
  }

  function processNextFile(resp) {
    if (fileNames.length > 0 && resp.hasOwnProperty("kind")) {
      createFile(fileNames.shift());
    } else {
      document.getElementById("response").innerHTML=JSON.stringify(resp);
      document.getElementById("response").style.display="inline";
    }
  }
}

function deleteFiles(fileNames) {
  storageApiCall("files.delete", {"companyId": randomId, "files": fileNames});
}
