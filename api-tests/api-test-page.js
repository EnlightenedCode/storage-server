"use strict";
/* global gapi: true */
var responseId;

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
    responseId = document.getElementById("response");
    document.getElementById("bucketPath").innerHTML = 
    "https://www.googleapis.com/storage/v1/b/risemedialibrary-" + randomId;

    if (resp.error) {
      responseId.innerHTML = "not authorized: "
                                                      + resp.error;
    } else {
      responseId.innerHTML = "authorized";
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
  responseId.style.display="none";
  commandArray = commandString.split(".");

  commandObject = gapi.client.storage;
  commandArray.forEach(function(val) {
    commandObject = commandObject[val];
  });
 
  commandObject(paramObj).execute(function(resp) {
        responseId.innerHTML=JSON.stringify(resp);
        if (!doNotUpdateResponse) {responseId.style.display="inline";}
        if (callback) {callback(resp);}
      });
}

function createFiles(fileNames) {
  if (fileNames.length === 0) {return;}
  responseId.style.display="none";
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
      responseId.innerHTML = "Without error: " + withoutError;
      responseId.style.display="inline";
    }
  }

  function fileUploaded(fileName) {
    var xhr = new XMLHttpRequest();
    xhr.open("GET",
             "https://www.googleapis.com/storage/v1/b/risemedialibrary-" +
             randomId + "/o/" + fileName, false);
    xhr.send();
    return !xhr.response.error && xhr.status === 200;
  }
}

function initiateServerTask(task, params, cb) {
  var uri = "http://localhost:8888/servertask" +
            "?task=" + task;

  var xhr = new XMLHttpRequest();

  responseId.style.display="none";

  for (var paramKey in params) {
    uri += "&" + paramKey + "=" + params[paramKey];
  }

  xhr.onerror = function() {
    responseId.innerHTML = "Server task complete";
    responseId.style.display = "inline";
  };
  xhr.onload = xhr.onerror;
  xhr.open("GET", uri , true);
  xhr.withCredentials = true;

  console.log("initiating server task: " + uri);
  xhr.send();
}

function addPublicReadOneFile() {
  initiateServerTask("AddPublicReadObject",
  {bucket: "risemedialibrary-" + randomId, object: "test1"})
}

function removePublicReadOneFile() {
  initiateServerTask("RemovePublicReadObject",
  {bucket: "risemedialibrary-" + randomId, object: "test1"});
}

function addPublicReadBucket() {
  initiateServerTask("AddPublicReadBucket",
  {bucket: "risemedialibrary-" + randomId});
}

function removePublicReadBucket() {
  initiateServerTask("RemovePublicReadBucket",
  {bucket: "risemedialibrary-" + randomId});
}
