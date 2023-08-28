SonarLint IntelliJ Plugin
=========================

[![Build Status](https://api.cirrus-ci.com/github/SonarSource/sonarlint-intellij.svg?branch=master)](https://cirrus-ci.com/github/SonarSource/sonarlint-intellij)
[![Quality Gate](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.sonarlint.intellij%3Asonarlint-intellij&metric=alert_status)](https://next.sonarqube.com/sonarqube/dashboard?id=org.sonarsource.sonarlint.intellij%3Asonarlint-intellij)

SonarLint is an IDE extension that helps you detect and fix quality issues as you write code.
Like a spell checker, SonarLint squiggles flaws so they can be fixed before committing code.

Useful links
------------

- [SonarLint website](https://www.sonarlint.org)
- [Features](https://www.sonarlint.org/features/)
- [SonarLint documentation](https://docs.sonarsource.com/sonarlint/intellij/)
    - A full list of supported programming languages and links to the static code analysis rules associated with each language is available on the [Rules page](https://docs.sonarsource.com/sonarlint/intellij/using-sonarlint/rules/).
- [SonarLint Community](https://community.sonarsource.com/c/help/sl)

How to install
--------------

You can install SonarLint from the [JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/7973-sonarlint), directly available in the IDE preferences.

Node.js >= 14.20 is required to perform JavaScript or TypeScript analysis (Node.js >= 16 is recommended).

Full up-to-date details are available on the [Requirements](https://docs.sonarsource.com/sonarlint/intellij/getting-started/requirements/) and [Installation](https://docs.sonarsource.com/sonarlint/intellij/getting-started/installation/) pages.

Questions and Feedback?
--------------------------

For SonarLint support questions ("How do I?", "I got this error, why?", ...), please first read the [FAQ](https://community.sonarsource.com/t/frequently-asked-questions/7204) to learn how to get your logs, and then head to the [SonarSource forum](https://community.sonarsource.com/c/help/sl). Before creating a new topic, please check if your question has already been answered because there is a chance that someone has already had the same issue. 

Be aware that this forum is a community, and the standard pleasantries are expected (_Hello, Thank you, I appreciate the reply, etc_). If you don't get an answer to your thread, you should sit on your hands for at least three days before bumping it. Operators are not standing b, but the Teams and Community Managers know that your questions are important. :-)

Contributing
------------

If you would like to see a new feature, check out the [PM for a Day](https://community.sonarsource.com/c/sl/pm-for-a-day-sl/41) page! There we provide a forum to discuss your needs and offer you a chance to engage the Product Manager and development teams directly. Feel free to add to an ongoing discussion or create a new thread if you have something new to bring up.

Please be aware that we are not actively looking for feature contributions. The truth is that it's extremely difficult for someone outside SonarSource to comply with our roadmap and expectations. Therefore, we typically only accept minor cosmetic changes and typo fixes.

With that in mind, if you would like to submit a code contribution, please create a pull request for this repository. Please explain your motives to contribute this change: what problem you are trying to fix, what improvement you are trying to make.

Make sure that you follow our [code style](https://github.com/SonarSource/sonar-developer-toolset#code-style-configuration-for-intellij) and that all tests are passing.

How to build
------------

    ./gradlew buildPlugin

Note that the above won't run tests and checks. To do that too, run:

    ./gradlew check buildPlugin

For the complete list of tasks, see:

    ./gradlew tasks

How to run ITs
------------

    ./gradlew :its:runIdeForUiTests &

The above will start an IDE instance with the SonarLint plugin. Wait for the UI robot server to start, then run the ITs:

    ./gradlew :its:check

Finally close the IDE.

To test against a specific version of IntelliJ, the `ijVersion` property can be used, e.g.:

    ./gradlew :its:runIdeForUiTests  -PijVersion=IC-2019.3 &

To test against a specific IDE, the `runIdeDirectory` property can be used as such:

    ./gradlew :its:runIdeForUiTests -PrunIdeDirectory=<IDE_PATH> &

Please note that the IDE must be in foreground while tests are executed.

How to develop in IntelliJ
--------------------------

Import the project as a Gradle project.

Note: whenever you change a Gradle setting (for example in `build.gradle.kts`),
don't forget to **Refresh all Gradle projects** in the **Gradle** toolbar.

To run an IntelliJ instance with the plugin installed, execute the Gradle task `runIde` using the command line,
or the **Gradle** toolbar in IntelliJ, under `Tasks/intellij`.
The instance files are stored under `build/idea-sandbox`.

To run against a specific IDE, the `runIdeDirectory` property can be used as such:

    ./gradlew :runIde -PrunIdeDirectory=<IDE_PATH>

Keep in mind that the `clean` task will wipe out the content of `build/idea-sandbox`,
so you will need to repeat some setup steps for that instance, such as configuring the JDK.

Whenever you change dependency version, the previous versions are not deleted from the sandbox, and the JVM might not load the version that you expect.
As the `clean` task may be inconvenient, an easier workaround is to delete the jars in the sandbox, for example with:

    find build/idea-sandbox/ -name '*.jar' -delete

How to release
--------------

See the [release pipeline on GitHub](https://github.com/SonarSource/sonarlint-intellij/actions/workflows/release.yml).

License
-------

Copyright 2015-2023 SonarSource.

Licensed under the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt)
