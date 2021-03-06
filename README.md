# Storage Server 

## Introduction

Storage server is the server side module for the storage client.  Together they make up the storage module which is part of the [Rise Vision](http://rva.risevision.com) digital signage management application.  

[Rise Vision](http://rva.risevision.com) runs on Google App Engine and as such requires GAE to operate. It also uses Google Cloud Storage as a datastore.

Chrome is the only supported browser to use to view the Storage Server API Explorer.

## Built With

- Maven (3.2.1 or greater)
- Java (1.7.0_65 SDK or greater)
- GAE (Google App Engine)

## Development 

### Local Development Environment Setup and Installation

#### Linux

* Maven 3 is required so you need to do some things to make sure your apt-get doesn't install an older version of maven.

* clone the repo using Git to your local:
```bash
git clone https://github.com/Rise-Vision/storage-server.git
```

* cd into the repo directory
```bash
cd storage-server
```


* Run this command to start the server locally
``` bash
mvn clean install
mvn appengine:devserver
```

* Open a Chrome browser and go to localhost:8888 then click the "API Explorer" link

### Windows
(this walkthrough uses Windows 64 bit 8.1)
* Requires Java SDK which can be downloade and installed via [Oracle Download Page](http://www.oracle.com/technetwork/java/javaee/downloads/java-ee-sdk-7-downloads-1956236.html "Oracle Download Page")
* Requires Apache Maven 3.2.3 or greater from the [Maven Download Page](http://maven.apache.org/download.cgi "Maven Download Page")
* The Zip file you get for maven extract it to your main C: drive or wherever you want to put it.
* Make sure to update your Environment Variables to add Java sdk and Maven location to your PATH under System Variables.  This is in Control Panel > System > Advance Properties > Environment Variables.  by Default Java in Windows 8 64 bit is located C:\Program Files (x86)\Java\jdk1.7.0_67\bin so that should be put in your PATH.  Maven should be C:\apache-maven-3.2.3\bin if you installed it to your C: drive. Once both are added to your PATH enviroment variable, open a command prompt as system adminstrator and run this command:

``` bash
mvn -v
Outputs: 
Maven home: C:\apache-maven-3.2.3\bin\..
Java Version: 1.7.0_67, vendor: Oracle Corporation
Java home: C:\Program Files (x86)\Java\jdk1.7.0_67\jre
```

* this will confirm that maven and java paths are correct and you have both installed correctly.

* With Git Bash for Windows open git bash in the directory that you want to clone the repo to 

* in git bash run this cmd:
```bash
git clone https://github.com/Rise-Vision/storage-server.git
```

* pull up a command prompt and navigate to the directory where you cloned the Storage-Client repo and cd into the root of the repository.

* Run this cmd to start the server locally
``` bash
mvn clean install
mvn appengine:devserver
```

* Open a Chrome browser and go to localhost:8888, click the "API Explorer" link


### Dependencies

* Junit for testing 
* Mockito for mocking and testing
* Google App Engine SDK

#### Rise Vision Core

The local repository is referenced in pom.xml:
``` xml
<repositories>
  <repository>
    <id>lib</id>
    <url>file://${basedir}/lib</url>
  </repository>
</repositories>
```

### External Registrations and Requirements
* Private p12 and client_secret files should go into src/private-keys.  These allow server to server authentication for google cloud storage.
* P12 file in the src/private-keys directory also allows you to run the api-tests by running ./run-tests.sh --password=(use your google account's password registered with risevision here)
* To register your own p12 for google app engine please refer to Google's help page on this topic: 
https://developers.google.com/storage/docs/authentication

### Build and Deployment Process

#### Linux

##### To Run Our Api-tests Locally

You can run our api-tests by running this command from the api-tests folder
```
./run-tests.sh --password=(password for google account)
```
Our api-tests will not run without a company-id to test with. You can configure this by placing a file called "jenkins-company.js".

The file consists of one line which defines a variable containing the companyId that will be used during the api tests.

The file name is not important but it must be consistent with line 3 in the api-test-page.html file.

The api-tests will run.  If you receive an error that says "file not found" then your P12 key is not setup correctly.

#### Windows

* open a cmd prompt and run this command

```
call run-tests.bat (google account password registered with risevision)
```

* after the command is run you can close the cmd windows that opened.

Deploy via
``` bash
mvn appengine:update -Dappengine.version=your-module-version \
-Dappengine.appId=your-app-id
```

## Submitting Issues 

Issues should be reported in the github issue list at https://github.com/Rise-Vision/storage-server/issues  

Issues should be reported with the template format as follows:

**Reproduction Steps**
(list of steps)
1. step 1
2. step 2

**Expected Results**
(what you expected the steps to produce)

**Actual Results**
(what actually was produced by the app)

Screenshots are always helpful with issues. 

## Contributing

All contributions greatly appreciated and welcome! If you would first like to sound your contribution ideas please post your thoughts to our community (http://community.risevision.com), otherwise submit a pull request and we will do our best to incorporate it

### Languages

In order to support languages i18n needs to be added to this repository.  Please refer to our Suggested Contributions.

### Suggested Contributions

* i18n Language Support

## Resources

Source code for the jar files can be found at the following two urls:
 * http://risevision.googlecode.com/svn/!svn/bc/890/trunk/coreAPIjava/src/com/risevision/core/api/
 * https://github.com/Rise-Vision/core/tree/master/core/src/com/risevision/directory

If you have any questions or problems please don't hesitate to join our lively and responsive community at http://community.risevision.com.

If you are looking for user documentation on Rise Vision please see http://www.risevision.com/help/users/

If you would like more information on developing applications for Rise Vision please visit http://www.risevision.com/help/developers/. 

## Facilitator
[Tyler Johnson](https://github.com/tejohnso "Tyler Johnson")
