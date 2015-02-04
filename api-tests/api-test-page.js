"use strict";
/* jslint node: true */
/* global gapi: true, jenkinsCompany, document, console, window, XMLHttpRequest */
var responseId;
var storageAPIFilesCountId;
var storageAPIFoldersCountId;
var googleAPIFilesCountId;
var googleAPIFoldersCountId;
var tagDefinitionId = "", fileTagEntryId = "";

function init() {
  var ROOT = "http://localhost:8888/_ah/api",
      SCOPE = ["https://www.googleapis.com/auth/userinfo.email",
               "https://www.googleapis.com/auth/devstorage.full_control"],
      CLIENT = "614513768474.apps.googleusercontent.com";

  console.log("Initializing");
  gapi.client.load("storage", "v0.01", function() {
    console.log("Storage API loaded");
    gapi.client.load("oauth2", "v2", function() {
      console.log("Authorizing");
      gapi.auth
        .authorize({client_id: CLIENT, scope: SCOPE, immediate: false} ,authCallback);
    });
  }, ROOT);

  function authCallback(resp) {
    responseId = document.getElementById("response");
    storageAPIFilesCountId = document.getElementById("storageAPIFilesCount");
    storageAPIFoldersCountId = document.getElementById("storageAPIFoldersCount");
    googleAPIFilesCountId = document.getElementById("googleAPIFilesCount");
    googleAPIFoldersCountId = document.getElementById("googleAPIFoldersCount");
    document.getElementById("bucketPath").innerHTML = 
    "https://www.googleapis.com/storage/v1/b/risemedialibrary-" + jenkinsCompany;

    if (resp.error) {
      responseId.innerHTML = "not authorized: " + resp.error;
    } else {
      responseId.innerHTML = "authorized";
    }
  }
}

function createBucket() {
  storageApiCall("createBucket", {"companyId": jenkinsCompany});
}

function createFolder() {
  storageApiCall("createFolder", {"companyId": jenkinsCompany,
                                  "folder": "test folder"});
}

function deleteFolder() {
  storageApiCall("files.delete", {"companyId": jenkinsCompany,
                                  "files": ["test folder/"]});
}

function deleteBucket() {
  storageApiCall("deleteBucket", {"companyId": jenkinsCompany});
}

function createBucketWrongCompany() {
  storageApiCall("createBucket", {"companyId": "this-company-shant-exist"});
}

function createFolderMissingCompany() {
  storageApiCall("createFolder", {"folder": "test folder"});
}

function createFolderMissingFolder() {
  storageApiCall("createFolder", {"companyId": jenkinsCompany});
}

function getUploadToken(fileName, cb) {
  storageApiCall("getResumableUploadURI", {"companyId": jenkinsCompany, "fileName": fileName, "fileType": "", "origin": window.location.origin}, cb, true);
}

function deleteFiles(fileNames) {
  storageApiCall("files.delete", {"companyId": jenkinsCompany, "files": fileNames});
}

function refreshRootFolder() {
  storageApiCall("files.get", {"companyId": jenkinsCompany});
}

function refreshSubFolder() {
  storageApiCall("files.get", {"companyId": jenkinsCompany, "folder": "test folder"});
}

function refreshGoogleAPIFolder(folder) {
  googleAPIFilesCountId.style.display="none";
  googleAPIFoldersCountId.style.display="none";
  var req = new XMLHttpRequest;
  var url= "https://www.googleapis.com/storage/v1/b/risemedialibrary-" + encodeURIComponent(jenkinsCompany) + "/o?delimiter=%2F";
  url = (folder) ? url + "&prefix=" + encodeURIComponent(folder) + "%2F": url;
  req.open("GET", url, true);
  req.send();
  req.onreadystatechange=function(){
    if(req.readyState === 4 && req.status === 200){
      var json = JSON.parse(req.responseText);
      var lengthOfFiles = (json.items) ? json.items.length : "0";
      var lengthOfFolders = (json.prefixes) ? json.prefixes.length : "0";
      googleAPIFilesCountId.innerHTML= lengthOfFiles;
      googleAPIFilesCountId.style.display="inline";
      googleAPIFoldersCountId.innerHTML= lengthOfFolders;
      googleAPIFoldersCountId.style.display="inline";
    }
  };
}

function createTagDefinition(name, type, values){
    storageApiCall("tagdef.put", 
      { "companyId": jenkinsCompany,
        "name": "TagName",
        "type": "Lookup",
        "values": ["value1", "value2", "value3"] },
      createTagDefinitionCallback);
}

function createTagDefinitionCallback(response){
    var result = response.result;
    if(result !== undefined){
        tagDefinitionId = result.item.id;
    }
}

//function updateTagDefinition(id, name, type, values){
//  storageApiCall("tagdef.patch",{"id": id,"name": name, "type": type, "values": values});
//}

function deleteTagDefinition() {
  storageApiCall("tagdef.delete", { "id": tagDefinitionId });
}

function notExistingDeleteTagDefinition() {
  storageApiCall("tagdef.delete", { "id": "doesNotExist" });
}

function getTagDefinition() {
  storageApiCall("tagdef.get", { "id": tagDefinitionId });
}

function notExistingGetTagDefinition() {
  storageApiCall("tagdef.get", { "id": "doesNotExist" });
}

function listTagDefinition() {
  storageApiCall("tagdef.list", { "companyId": jenkinsCompany });
}

function createFileTagEntry(name, type, values) {
    storageApiCall("filetag.put", 
      { "companyId": jenkinsCompany,
        "name": "TagName",
        "objectId": "filename",
        "type": "Lookup",
        "values": ["value1", "value2", "value3"] },
      createFileTagEntryCallback);
}

function createFileTagEntryCallback(response) {
    var result = response.result;
    if(result !== undefined){
        fileTagEntryId = result.item.id;
    }
}

//function updateFileTagEntry(id, name, type, values){
//  storageApiCall("filetag.patch",{"id": id,"name": name, "type": type, "values": values});
//}

function deleteFileTagEntry() {
  storageApiCall("filetag.delete", { "id": fileTagEntryId });
}

function notExistingDeleteFileTagEntry() {
  storageApiCall("filetag.delete", { "id": "doesNotExist" });
}

function getFileTagEntry() {
  storageApiCall("filetag.get", { "id": fileTagEntryId });
}

function notExistingGetFileTagEntry() {
  storageApiCall("filetag.get", { "id": "doesNotExist" });
}

function listFileTagEntry() {
  storageApiCall("filetag.list", { "companyId": jenkinsCompany });
}

function listFileTagEntry() {
  storageApiCall("filetag.list", { "companyId": jenkinsCompany });
}

function listFilesByTag(tags, returnTags) {
  storageApiCall("files.listbytags", { "companyId": jenkinsCompany, 
                                       "tags": tags,
                                       "returnTags": returnTags !== null ? returnTags : false });
}

function storageApiCall(commandString, paramObj, callback, doNotUpdateResponse) {
  var commandObject, commandArray;
  responseId.style.display="none";
  storageAPIFilesCountId.style.display="none";
  storageAPIFoldersCountId.style.display="none";

  commandArray = commandString.split(".");

  commandObject = gapi.client.storage;
  commandArray.forEach(function(val) {
    commandObject = commandObject[val];
  });
 
  commandObject(paramObj).execute(function(resp) {
    responseId.innerHTML=JSON.stringify(resp);
    var lengthOfFiles = 0;
    var lengthOfFolders = 0;
    if(resp.files){
      for(var i=0; i < resp.files.length; i++) {
        (resp.files[i].kind === "folder") ? lengthOfFolders++ : lengthOfFiles++;
      }
    }
    storageAPIFilesCountId.innerHTML= lengthOfFiles;
    storageAPIFoldersCountId.innerHTML= lengthOfFolders;
    if (!doNotUpdateResponse) {
      responseId.style.display="inline";
      storageAPIFilesCountId.style.display="inline";
      storageAPIFoldersCountId.style.display="inline";
    }
    if (callback) {callback(resp);}
  });
}

function createFiles(fileNames) {
  if (fileNames.length === 0) {return;}
  responseId.style.display="none";
  createFile(fileNames.shift());

  function createFile(fileName) {
    getUploadToken(encodeURIComponent(fileName), uploadFile);

    function uploadFile(tokenResponse) {

      var xhr = new XMLHttpRequest();
      xhr.open("PUT", tokenResponse.message, true);
      xhr.onload = function() {processNextFile(fileUploaded(encodeURIComponent(fileName)));};
      xhr.onerror = function() {processNextFile(fileUploaded(encodeURIComponent(fileName)));};
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
             jenkinsCompany + "/o/" + fileName, false);
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
  {bucket: "risemedialibrary-" + jenkinsCompany, object: "test1"});
}

function removePublicReadOneFile() {
  initiateServerTask("RemovePublicReadObject",
  {bucket: "risemedialibrary-" + jenkinsCompany, object: "test1"});
}

function addPublicReadBucket() {
  initiateServerTask("AddPublicReadBucket",
  {bucket: "risemedialibrary-" + jenkinsCompany});
}

function removePublicReadBucket() {
  initiateServerTask("RemovePublicReadBucket",
  {bucket: "risemedialibrary-" + jenkinsCompany});
}

function checkPublicReadObject(obj) {
  var uri = document.getElementById("bucketPath").innerHTML + "/o/" + obj + "/acl/allUsers";
  var requestObj = {"path": uri};

  responseId.style.display = "none";

  gapi.client.request(requestObj)
  .then(function(resp) {
    responseId.innerHTML = JSON.stringify(resp);
    responseId.style.display = "inline";
  }, function(resp) {
    responseId.innerHTML = JSON.stringify(resp);
    responseId.style.display = "inline";
  });
}
