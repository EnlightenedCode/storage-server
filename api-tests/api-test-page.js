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

function getUploadToken(fileName, cb) {
  storageApiCall("getResumableUploadURI", {"companyId": randomId, "fileName": fileName}, cb, true);
}

function deleteFiles(fileNames) {
  storageApiCall("files.delete", {"companyId": randomId, "files": fileNames});
}

function storageApiCall(commandString, paramObj, callback, doNotUpdateResponse) {
  var commandObject, commandArray;
  document.getElementById("response").style.display="none";
  commandArray = commandString.split(".");

  commandObject = gapi.client.storage;
  commandArray.forEach(function(val) {
    commandObject = commandObject[val];
  });
 
  commandObject(paramObj, doNotUpdateResponse)
      .execute(function(resp) {
        document.getElementById("response").innerHTML=JSON.stringify(resp);
        if (!doNotUpdateResponse) {document.getElementById("response").style.display="inline";}
        if (callback) {callback(resp);}
      });
}

function createFiles(fileNames) {
  if (fileNames.length === 0) {return;}
  document.getElementById("response").style.display="none";
  createFile(fileNames.shift());

  function createFile(fileName) {
    getUploadToken(fileName, uploadFile);

    function uploadFile(tokenResponse) {
      console.log(tokenResponse);

      var xhr = new XMLHttpRequest();
      xhr.open("PUT", tokenResponse.message, true);
      xhr.onload = function() {processNextFile(fileUploaded(fileName));};
      xhr.onerror = function() {processNextFile(fileUploaded(fileName));};
      xhr.send("test data");
    }
  }

  function processNextFile(withoutError) {
    if (fileNames.length > 0 && withoutError) {
      createFile(fileNames.shift());
    } else {
      document.getElementById("response").innerHTML = "Without error: " + withoutError;
      document.getElementById("response").style.display="inline";
    }
  }

  function fileUploaded(fileName) {
    var xhr = new XMLHttpRequest();
    xhr.open("GET",
             "https://www.googleapis.com/storage/v1/b/risemedialibrary-" +
             randomId + "/o/" + fileName, false);
    xhr.send();
    return !xhr.response.error;
  }
}
