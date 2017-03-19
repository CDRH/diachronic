ulysses-p4-to-p5
================

This application converts the [TEI](http://www.tei-c.org/) P4 encoding of Gabler's Ulysses edition to TEI P5.

Requirements
------------

- Java SE Development Kit v8
- [Apache Maven](http://maven.apache.org/)

Build instructions
------------------

With Java and Maven installed, build the application from the source directory via

    mvn clean package
    
A JAR file including all dependencies will be available under `target/`.

Running the Converter
---------------------

The built JAR file is executable via the command line:

    java -jar ulysses-p4-to-p5-<version>.jar <P4-file> <P5-file>
    
It expects two command line arguments: the path to the P4-encoded input document and the path where 
the converter should write the conversion result.
 
Validating the Result
---------------------

After each conversion, the application also validates the results against a
[customized TEI P5 schema](src/main/resources/tei_all_enhanced_app.xml) and outputs validation errors 
to the console, should there be any.