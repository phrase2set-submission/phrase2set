Project to collect Android API GROUMS.

# Objective

The scope of this java project is to 

- Retrieve the list of android java projects from the github and store it in a local mongodb.
 
For each project from the mongoDB performs
  - check out
  - run GROUMINER
  - udpate the graph database with API GROUMS
  - update the status in the mongodb for the project

Requires a local instance of mongodb.

Follow the guide at https://www.howtoforge.com/tutorial/install-mongodb-on-ubuntu-16.04/ to install and configure mongodb on Ubunutu server. Use https://robomongo.org/ to install and manage the mongo database with a graphical user interface.

After you install and configure the mongodb, create a database by name 't2api' and a collection named 'android_projects'

# Setup in Eclipse

This project has compilation dependency on the following modules in the T2APICode. Add them as dependent projects in the project properties under the 'Java Build Path'.
 - CodeGeneration
 - Recoder
 - RefLearner



