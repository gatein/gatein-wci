# GateIn Web Container Integration

The Web Container Integration component is in charge of the abstraction on various servers such as JBoss Application Server
and Apache Tomcat. Deployment of applications are dependent on the underlying server.

# Instructions

## JBoss7 tests ##

JBoss7 integration tests require two configurations steps.

* The environment variable JBOSS_HOME must point to JBoss AS 7.1.1
* The JBoss 7 installation must contain an application user named 'foo' with password 'bar' :
run "add-user.sh"
use option b) for the user type (Application User)
leave default realm option (just press enter)
username : 'foo'
password : 'bar' (twice)
role : 'myrole'
