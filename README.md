wotcompare
==========

This is a tiny proof-of-concept (desktop) application to visually compare attributes of tanks from the same tier and class.

Currently I'm using Swing to render the UI but I might turn this into a webapp when I'm satisfied with the results.

Compiling & running
-------------------

This project requires JDK8 1.8u5+ and Maven 3.2.2+ to build.

To compile the project, run

    mvn clean package
    
To run the project you first need to create a file named '.wotcompare' in the current directory.
This file must have the following contents

    application_id=<application ID associated with your wargaming account>


You can generate an application ID by visiting https://eu.wargaming.net/developers/applications (or https://na.wargaming.net/developers/applications) respectively.

After creating this file, execute the following command from the same directory:

    mvn package exec:java
