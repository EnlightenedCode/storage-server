"use strict";
/* global gapi: true */
function init() {
  console.log("Initializing");
  var ROOT = "http://localhost:8888/_ah/api";
  gapi.client.load("storage", "v0.01", function() {
    console.log("storage API loaded");
    gapi.client.load("oauth2", "v2", function() {
      gapi.auth
          .authorize({client_id: "614513768474.apps.googleusercontent.com"
                      ,scope: "https://www.googleapis.com/auth/userinfo.email"
                      ,immediate: false}, authCallback);
    });
  }, ROOT);

  function authCallback() {
    document.getElementById("response").innerHTML = "logged-in";
  }
}

var randomId = Math.floor(Math.random()*90000) + 10000;
randomId = "api-test-" + randomId;

function createBucket() {
  storageApiCall("createBucket", {"companyId": randomId});
}

function deleteBucket() {
  storageApiCall("deleteBucket", {"companyId": randomId});
}

function storageApiCall(apiCommand, paramObj) {
  document.getElementById("response").style.display="none";
  gapi.client.storage[apiCommand](paramObj)
      .execute(function(resp) {
        console.log(resp);
        document.getElementById("response").innerHTML=JSON.stringify(resp);
        document.getElementById("response").style.display="block";
      });
}
