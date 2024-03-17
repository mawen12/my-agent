# Agent

## Announce

This module is copy from [easeagent](https://github.com/megaease/easeagent).

## Build from source

You need Jdk 17:

```shell
$ cd my-agent
$ mvn clean package -am -pl :build -Dmaven.test.skip
```

Then you can find jar in path: `six-agent/build/target/agent-dep.jar`

The `six-agent/build/target/agent-dep.jar` is the agent jar with all the dependencies.

## Get Configuration file

Extracting the default configuration file.

```shell
$ cd $AGNET_PATH
$ jar xf agent-dep.jar agent.properties agent-log4j2.xml
```

