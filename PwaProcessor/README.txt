This file succinctly describes the steps necessary to implement, package and deploy the code to run over the Portuguese Web Archive infrastruture.
More information about the project is detailed at http://arquivo-web.fccn.pt/.

Dependencies:
 - This code was prepared for hadoop 0.12.4-dev, compiled by the project.
 - J2SE 1.5 or above (http://java.sun.com/javase/).
 - Ant 1.7 or above (http://ant.apache.org/).

Steps:
1 - Implement the src/main/java/pt/arquivo/processor/ProcessFile class methods.
2 - Edit the src/main/java/pt/arquivo/processor/ProcessArcs class only if the implementation on the ProcessFile methods isn't enough to support your code.
3 - Add all the dependencies/libraries to the lib directory.
4 - Type "ant all" to create the deploy jar file at the dist directory.
5 - Send the jar file to the arquivo-web.fccn.pt team.

Run: (Assuming the Hadoop instance is configured. See http://hadoop.apache.org/ for more information.)
 - The input directory inside the HDFS (Hadoop Distributed FS) must have an index file (named arcs.txt) listing all arcs for analysis. 
   The arcs could be located by absolute paths or URLs. Example of an arcs.txt file:
     http://<server>:8080/browser/files/AWP-20090510220409-00001.arc.gz
     http://<server>:8080/browser/files/AWP-20090510220409-00002.arc.gz
 - ${HADOOP_HOME}/bin/hadoop jar pwaprocessor-0.1.jar inputs outputs

Note: 
  - The project can be imported to eclipse.


