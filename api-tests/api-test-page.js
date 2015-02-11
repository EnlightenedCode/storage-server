"use strict";
/* jslint node: true */
/* global gapi: true, jenkinsCompany, document, console, window, XMLHttpRequest */
var responseId;
var storageAPIFilesCountId;
var storageAPIFoldersCountId;
var googleAPIFilesCountId;
var googleAPIFoldersCountId;
var tagDefinitionId = "", storageObjectId = "";

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

function createRvStorageObject(name, type, values) {
  var tag1 = { name: "TagName", type: "Lookup", value: "value1" };
  var tag2 = { name: "TagName", type: "Lookup", value: "value2" };
  var tag3 = { name: "TagName", type: "Lookup", value: "value3" };
  var timeline = {
    timeDefined: "true", duration: "60", pud: "false", trash: "true", carryon: "false",
    startDate: "02/02/15 12:00 AM", endDate:"02/03/15 12:00 AM",
    startTime: null, endTime: null, recurrenceOptions: null };

  storageApiCall("filetags.put", {
      "companyId": jenkinsCompany,
      "objectId": "filename",
      "tags": [ tag1, tag2, tag3 ],
      "timeline": JSON.stringify(timeline)
    },
    createStorageObjectCallback);
}

function createStorageObjectCallback(response) {
  var result = response.result;
  if(result !== undefined) {
    storageObjectId = result.item.id;
  }
}

function getRvStorageObject() {
  storageApiCall("filetags.get", { "id": storageObjectId });
}

function notExistingGetRvStorageObject() {
  storageApiCall("filetags.get", { "id": "doesNotExist" });
}

function listRvStorageObject() {
  listFilesByTag(null);
}

function listFilesByTag(tags) {
  storageApiCall("files.listbytags", { "companyId": jenkinsCompany, 
                                       "tags": tags });
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
    var items = resp.files || resp.items;
    
    if(items){
      for(var i=0; i < items.length; i++) {
        (items[i].kind === "folder") ? lengthOfFolders++ : lengthOfFiles++;
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
