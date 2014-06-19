###Storage Server  ![Build Status](http://devtools1.risevision.com:8080/job/Storage-Server-BranchPush/badge/icon)

**Copyright © 2010 - May 2014 Rise Vision Incorporated.**

*Use of this software is governed by the GPLv3 license (available in the LICENSE file).*

Storage server is the server side module for the storage client.  Together they make up the storage module which is part of the [RVA](http://rva.risevision.com) digital signage management application.  


[RVA](http://rva.risevision.com) runs on Google App Engine and as such requires GAE to operate. It also uses Google Cloud Storage as a datastore.

Storage Server Usage 
=================

Run locally via
``` bash
mvn clean install [-Pprod]
mvn appengine:devserver
```

> Private p12 and client_secret files should go into src/private-keys.  These allow server to server authentication for google cloud storage.

Deploy via
``` bash
mvn appengine:update -Dappengine.version=your-module-version \
-Dappengine.appId=your-app-id
```

### Rise Vision Core Dependencies

The [core](https://github.com/Rise-Vision/core) dependencies must be added to a local maven repository *(lib/)* as follows:

Create local maven repository in root project *directory/lib* via (once for each file):

``` bash
mvn install:install-file -DlocalRepositoryPath=lib/ \
  -DcreateChecksum=true -Dpackaging=jar \
  -Dfile=lib/jars/com.risevision.core.api -DgroupId=com.risevision.core \
  -DartifactId=api -Dversion=1.0

mvn install:install-file -DlocalRepositoryPath=lib/ \
  -DcreateChecksum=true -Dpackaging=jar \
  -Dfile=lib/jars/com.risevision.directory -DgroupId=com.risevision \
  -DartifactId=directory -Dversion=1.0
```

The local repository is referenced in pom.xml:
``` xml
<repositories>
  <repository>
    <id>lib</id>
    <url>file://${basedir}/lib</url>
  </repository>
</repositories>
```

Each jar file is specified as a dependency in pom.xml:
``` xml
<dependency>
  <groupId>com.risevision</groupId>
  <artifactId>directory</artifactId>
  <version>1.0</version>
</dependency>
<dependency>
  <groupId>com.risevision.core</groupId>
  <artifactId>api</artifactId>
  <version>1.0</version>
</dependency>
```

Source code for the jar files can be found at the following two urls:
 * http://risevision.googlecode.com/svn/!svn/bc/890/trunk/coreAPIjava/src/com/risevision/core/api/
 * https://github.com/Rise-Vision/core/tree/master/core/src/com/risevision/directory

---------
If you have any questions or problems please don't hesitate to join our lively and responsive community at http://community.risevision.com.

If you are looking for user documentation on Rise Vision please see http://www.risevision.com/help/users/

If you would like more information on developing applications for Rise Vision please visit http://www.risevision.com/help/developers/. 
