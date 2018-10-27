# MessageEvaluator
MessageEvaluator is an app to analyze social media chats using offline, exported data.

## Building
To test and build the project, run
````
./gradlew clean build
````
in the project root directory (skip the ./ if not on a linux machine).

## Running
The application is deployed as an executable jar file, located under build/libs. To start it, type
````
java -jar build/libs/messageEvaluator-<VERSION>.jar
````
and substitute VERSION for the current project version.