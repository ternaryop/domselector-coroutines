DomSelector
=============

Extract galleries and single images from HTML text using CSS3 rules

Local Maven distribution
========================

    ./gradlew publishToMavenLocal

Maven Central distribution
========================

    ./gradlew publish --no-daemon --no-parallel
    ./gradlew closeAndReleaseRepository

Android Studio configuration
============================

1. Run -> Edit Configuration
2. Add a `Gradle` task

|      Name      | Gradle Project      | Tasks               |
|:--------------:|---------------------|---------------------|
| Local Install  | ../app/build.gradle | publishToMavenLocal |