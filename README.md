CloudUp
=====================

A private beta version of a CloudApp client for Android.

Uses following libraries:
-------------------------
- AndroidSherlockBar ~ http://actionbarsherlock.com
- CloudAppJava ~ https://github.com/simong/CloudAppJava
- Apache commons-lang3-3.1 ~ http://commons.apache.org/lang/
- aFileChooser ~ https://github.com/iPaulPro/aFileChooser
- image-chooser-library ~ https://github.com/coomar2841/image-chooser-library
- Crashlytics ~ http://try.crashlytics.com/sdk-android/

How to build and run the project:
---------------------------------
1. Download **[AndroidSherlockBar](http://actionbarsherlock.com)**, import the library project to **Eclipse**. In **CloudApp for Android** project go to *Properties -> Android -> Library*, add a reference to the library project.

2. Download **[Apache commons-lang3-3.1](http://commons.apache.org/lang/)** and **[CloudAppJava](https://github.com/simong/CloudAppJava)** binaries, save them somewhere on your computer and then in **Eclipse** go to *Properties -> Java Build Path -> Libraries*, add both JAR binaries. Make sure that under *Order and Export* needed binaries are before the **CloudApp for Android** binary.

3. Download **[aFileChooser](https://github.com/iPaulPro/aFileChooser)** and import the library project. Just like how it was done with Android Sherlock Bar.

4. Download **[image-chooser-library](https://github.com/coomar2841/image-chooser-library)** and import the library project. Just like how it was done with Android Sherlock Bar.

5. Follow the instructions on Crashlytics to install the SDK from Eclipse. It is pretty easy. (https://www.crashlytics.com/downloads/plugins)

6. Build and run!
