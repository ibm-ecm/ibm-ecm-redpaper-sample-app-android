#Welcome

This document will provide you information about how to install and use the source code provided with the IBM Redpaper : "IBM Enterprise Content Management Mobile Application Implementation".

#Overview

The sample source code provided with this document is a collection of examples of what can be achieved with the adoption of the IBM ECM Mobile SDKs mentioned in the Redpaper.

The kit is composed by two sample apps for Android : the IBM Case Manager Nearby Sample and the IBM Edit Task Sample.

The IBM Case Manager Nearby Sample is intended to demonstrate the usage of the Case Manager SDK in the use case of an Inspector working remotely with his Android mobile phone. The app starts with a fullscreen map of the area around the user and shows a pin for each task located nearby.By tapping on the pin the user is able to lock or complete the task.
<br/>
More details on how to setup the location of a task can be found in chapter 3.3.2 of the Redpaper.

The IBM Edit Task Sample allows the user to browse the tasks for a specific in-basket.<br/>
This app models the scenario where the user is required to provide information about a task, update its properties and complete it if needed.

#Prerequisites

This document assumes the reader has a working knowledge of IBM Case Manager, the Java language and the Android platform.

#### SDK Prerequisites
Here are the prerequisites for using the SDK:

- An IBM Case Manager server that runs IBM Case Manager 5.2.1 or later
- A mobile phone running Android 4.4 KitKat 
- A computer running Android Studio version 1.5
- The IBM Case Manager Mobile SDK 1.0.0.1 or later package
- The IBM Case Manager Mobile Configurator plugin 1.0.0.1 or later installed on the server

#Package
The package contains :
- sample\_edit\_tasks : a folder containing the source code for the Case Manager Edit Task Sample app
- sample\_nearby\_tasks : a folder containing the source code for the Case Manager Nearby Sample app


#Installation
Copy the archive on the computer where Andorid Studio is installed.<br/>
From Android Studio select "Open ..." > "File" and select the folder of one of the sample. <br/>
Repete the previous step for the other sample.


