# 编译
{
    ./gradlew clean --console plain
    ./gradlew check buildPlugin --console plain

    ./gradlew buildPlugin --console plain

    alias clean='./gradlew clean --console plain'
    alias build-all="cd ~/github.com/qstesiro/sonarlint-core &&
                     mvn install -pl core -am -Dmaven.test.skip=true &&
                     cd - &&
                     ./gradlew buildPlugin --console plain --daemon &&
                     ./gradlew :its:runIdeForUiTests --console plain --daemon"
    alias build="./gradlew buildPlugin --console plain --daemon &&
                 ./gradlew :its:runIdeForUiTests --console plain --daemon"
    alias log='tail -f ./its/build/idea-sandbox/system/log/idea.log'

    mvn install:install-file \
        -Dfile=/home/qstesiro/sonar-findbugs-plugin-4.0.3.jar \
        -DgroupId=com.github.spotbugs \
        -DartifactId=sonar-findbugs-plugin \
        -Dversion=4.0.3 \
        -Dpackaging=jar
}

# 测试
{
    ./gradlew :its:runIdeForUiTests --console plain
    ./gradlew :its:check --console plain
}
