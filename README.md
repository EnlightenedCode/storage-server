![Build Status](http://107.170.153.135:8080/job/Storage-Server-BranchPush/badge/icon)

Storage Server API 
=================

Run locally via
``` bash
mvn clean install
mvn appengine:devserver
```

> Private p12 and client_secret files should go into src/private-keys

Deploy via
``` bash
mvn appengine:update -Dappengine.version=your-module-version \
-Dappengine.appId=your-app-id
```

### The RV dependencies must be added to a local maven repository (lib/) as follows:

Create local maven repository in root project directory/lib via (once for each file):

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
