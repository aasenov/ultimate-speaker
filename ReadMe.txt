Ultimate Speaker graduation work.

Description:
  Platform independent and free system, that transform various types of text files into speech.

Prerequisites:
  To use the application you need to have "ESpeak" (http://espeak.sourceforge.net/) installed). Install it with "en" and "bg" languages.
  Installing the server certificate to the web browser, from where the client will be used:
    Open http://<server_ip>:8181/api-docs and instruct the browser to accept this certificate

Installation:
No installation needed. Just download the archive, extract it in some directory and execute the UltimateSpeaker.jar file. This will start the administration UI, from where you can start/stop/configure the application. Under UltimateSpeaker directory you can find the User UI, which is simple HTML+JavaScript app, that can be used from any computer. From User UI, you can upload,download and search for specific files.

Supported functionality:
  Multiple users - users are distinguished by email.
  All common used file formats are supported.
  Searching in file content.
  Downloading generated speech or the original file.
  Share files among users in the system.
  Visualize presentation files in HTML presenter.
  All operation are performed using Rest API and are available to clients. Documentation for the API can be obtained from http://<server_ip>:8181/api-docs