
## Class not found

### Exception in thread "main" java.lang.NoClassDefFoundError: org/springframework/boot/loader/LaunchedURLClassLoader

use `maven-shade-plugin` set `org.springframework` to `springframework` in `loader/pom.xml`.

## Exception in thread "main" java.lang.NoSuchMethodException: com.mawen.agent.Main.premain(java.lang.String,java.lang.instrument.Instrumentation)

rename `permain` to `premain` in Main class.

## Caused by: java.lang.ClassNotFoundException: com.mawen.agent.log4j2.FinalClassloaderSupplier

`agent-dep.jar` don't contains `log4j2` dir.

repair `build/src/assembly/src.xml`, `fileSets/fileSet/directory` must end with `/`.

the class name typo, should copy from `com.mawen.agent.log4j2.FinalClassLoaderSupplier` className.

## Exception in thread "main" java.lang.IllegalArgumentException: LoggerFactory is not a Logback LoggerContext but Logback is on the classpath. Either remove Logback or the competing implementation (class org.slf4j.helpers.NOPLoggerFactory loaded from file:/Users/mawen/.m2/repository/org/slf4j/slf4j-api/1.7.21/slf4j-api-1.7.21.jar). If you are using WebLogic you will need to add 'org.slf4j' to prefer-application-packages in WEB-INF/weblogic.xml: org.slf4j.helpers.NOPLoggerFactory

exclude `logback-classic` dependency.

## Caused by: java.lang.NullPointerException in com.mawen.agent.core.info.AgentInfoFactory.loadVersion

add `version.txt` in `core/src/main/resources`

## Caused by: java.lang.NoClassDefFoundError: com/fasterxml/jackson/databind/ObjectMapper

use `maven-shade-plugin` set `com.fasterxml` to `com.mawen.agent.plugin.utils` in `plugin-api/pom.xml`