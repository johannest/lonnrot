Experimental deep learning (RNN) demonstration for generating traditional Finnish poems
========

Tech stack
----------
* Vaadin 8 (https://vaadin.com/framework)
* Vaadin Designer (https://vaadin.com/designer)
* DL4J (https://deeplearning4j.org)

RNN Model
---------
Used RNN model is copied and modified from (https://github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/recurrent/character/GravesLSTMCharModellingExample.java)

It is trained with poem collected by Elias Lönnrot. All the texts are downloaded from Project Gutenberg (http://www.gutenberg.org/ebooks/author/4153). Texts are manually pre-processed before training. 

Workflow
--------

To compile the entire project, run "mvn install".

To run the application, run "mvn jetty:run" and open http://localhost:8080/ .

To produce a deployable production mode WAR:
- change productionMode to true in the servlet class configuration (nested in the UI class)
- run "mvn clean package"
- test the war file with "mvn jetty:run-war"

