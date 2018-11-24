# MessageEvaluator
MessageEvaluator is an app to analyze social media chats offline, using exported data.

## Building
To test and build the project, run
````
./gradlew clean build
````
in the project root directory (skip the ./ if not on a linux system).

## Running
The application is deployed as an executable jar file, located under build/libs. To start it, type
````
java -jar build/libs/messageEvaluator-<VERSION>.jar
````
and substitute VERSION for the current project version.

## Importing data
MessageEvaluator operates on data, which has to be exported from other applications. This is how you can export your
data from all supported data sources:

### Telegram (Client: Telegram Desktop)
This was tested under a linux system with version 1.4.3  of the [client](https://desktop.telegram.org/ "Download the
official Telegram Desktop Client").

To export the data:
- Go to <code>Menu -> Settings -> Advanced -> Export&nbsp;Telegram&nbsp;data</code>. There select all
groups and channels you wish to analyse. 'Account information', 'Contact list' and media are not required, since
analysis of these data is not supported. You may wish to specify a custom Download path.
- Make sure to select 'Machine-readable JSON' otherwise the import won't work!

This will create a directory containing all the data you exported, including a ```result.json```. This is the file you
need to import into MessageEvaluator.
