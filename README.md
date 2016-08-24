# Android Mirror Dashboard
A smart mirror Android app, inspired by [Max Braun](https://medium.com/@maxbraun/my-bathroom-mirror-is-smarter-than-yours-94b21c6671ba)'s project.

![Photo](https://raw.githubusercontent.com/Steppschuh/AndroidMirrorDashboard/master/Media/Photos/photo_01.jpg)

You can find more photos in [this gallery](https://goo.gl/photos/88zoz4xj4PNtdVeh6).

## Customize it
The app can easily be extended through modular [content](https://github.com/Steppschuh/AndroidMirrorDashboard/tree/master/Source/MirrorDashboard/app/src/main/java/com/steppschuh/mirrordashboard/content) packages. The existing architecture uses [ContentProviders](https://github.com/Steppschuh/AndroidMirrorDashboard/blob/master/Source/MirrorDashboard/app/src/main/java/com/steppschuh/mirrordashboard/content/ContentProvider.java) to regulary fetch data, represented as [Content](https://github.com/Steppschuh/AndroidMirrorDashboard/blob/master/Source/MirrorDashboard/app/src/main/java/com/steppschuh/mirrordashboard/content/Content.java) objects. These content objects have different types (e.g. [Weather](https://github.com/Steppschuh/AndroidMirrorDashboard/blob/master/Source/MirrorDashboard/app/src/main/java/com/steppschuh/mirrordashboard/content/weather/Weather.java)) and are rendered in the [DashboardActivity](https://github.com/Steppschuh/AndroidMirrorDashboard/blob/master/Source/MirrorDashboard/app/src/main/java/com/steppschuh/mirrordashboard/DashboardActivity.java) once they updated.
