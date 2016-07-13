Recipex
=======
.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/logo_wide.jpg
   :align: center
Your personal caregiver and lifestyle makeover.

RecipeX is an android application that has two main goals:

- Helping patients to follow their therapies, simplifying the communication with the health care assistants
- Giving health care assistants a simple and useful tool to keep track of the therapies of their assisted

Main functionalities
--------------------
The main functionalities offered by the application are:

- Record vital signs and nursing prescriptions, keeping track of your progress within long term treatments
- Manage a shared Google Calendar with your caregiver (and all those you would like to monitor your health), with all your current treatments and performed measurements
- Send mail notifications through Google calendar, to remind you about your therapeutic requirements
- The possibility to contact, directly from the application, your caregivers and family members

Architecture
------------

.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/architecture.png
   :align: center

The frontend is a mobile application (Android).
The backend is a Python application running on Google App Engine.
The frontend interacts with a Web Service API offered by the GAE backend through Google Cloud Endpoints
Through the GAE backend, the app access to:
Google + API, to register the user
Google Calendar API, to store the history of measurement and therapies and to notify the user about its therapeutical requirements.

Demo
----

Here you can find a demo of the application:
https://www.youtube.com/watch?v=LHL-k30ffdg


Screenshot
----------
These are the screenshot of the most important application sections

Home
----

.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/home.jpg
   :align: left
   
.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/drawer.jpg
   :align: right
   
Profile
-------

.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/profile_pic.jpg
   :align: left
   
.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/profile_open.jpg
   :align: center

.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/profile_button.jpg
   :align: right

Add measurement
---------------

.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/home_button.jpg
   :align: left
   
.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/add_measurement.jpg
   :align: right
   
Therapies and add new therapy
-----------------------------

.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/therapies.jpg
   :align: left
   
.. image:: https://github.com/FabFari/recipex/blob/master/app/screenshot/add_therapy.jpg
   :align: right
   
Additional info about the project
---------------------------------

You can find personal information about us at:
https://it.linkedin.com/in/fabrizio-farinacci-496679116
https://it.linkedin.com/in/sara-veterini-667684116

The project was developed and has been presented within the course of "Pervasive Systems", 
held by Prof. Ioannis Chatzigiannakis within the Master of Science in Computer Science (MSE-CS),
at University if Rome "La Sapienza". Informations about the course are available in the following page:
http://ichatz.me/index.php/Site/PervasiveSystems2016.

Additional informations about the project can be found in the following Slideshare presentations:
http://www.slideshare.net/FabrizioFarinacci1/recipex-your-personal-caregiver-and-lifestyle-makeover.

http://www.slideshare.net/FabrizioFarinacci1/recipex-your-personal-caregiver-and-lifestyle-makeover-62091050

