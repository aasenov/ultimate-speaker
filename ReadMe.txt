Ultimate Speaker graduation work.

Description:
  Platform independent and free system, that transform various types of text files into speech.

Prerequisites:
  Application is written on Java 1.7 - so you will be required latest JRE 1.7.79 or 1.7.80.
  To use the application you need to have "ESpeak" (http://espeak.sourceforge.net/) installed). Install it with "en" and "bg" languages. Include ESpeak bin directory to system PATH environment.
  Installing the server certificate to the web browser, from where the client will be used:
    Open https://<server_ip>:8181/api-docs and instruct the browser to accept this certificate

Supported OS:
  Application is tested under following operation systems:
    OpenSuse 13.2
    Fedora 19
    Windows XP
    Windows 2008 R2
    Windows 7
    Windwos 8

Supported Web Browsers:
  Firefox 41.0+
  Chrome 44.0+
  
Installation:
No installation needed. Just download the archive, extract it in some directory and execute the UltimateSpeaker.jar file. This will start the administration UI, from where you can start/stop/configure the application. Under UltimateSpeaker directory you can find the User UI, which is simple HTML+JavaScript app, that can be used from any computer. From User UI, you can upload,download and search for specific files.

Supported functionality:
  Multiple users - users are distinguished by email.
  All common file formats are supported.
  Searching in file name and content.
  Downloading generated speech or the original file.
  Share files among users in the system.
  Visualize presentation files in HTML presenter.
  Files rating.
  All operation are performed using Rest API and are available to clients. Documentation for the API can be obtained from https://<server_ip>:8181/api-docs
  
