
## Agent Plugin Debug

### Develop Environment Configuration

- Download the Agent source code to the localhost and add it to the workspace if the IDE, build and the output located in the source code directory build/target/agent-dep.jar, and then create the directory `build/target/plugins`.
- Add the plugin project (e.g. simple-plugin) to the same workspace and copy the compiled and packaged JAR file to the `build/target/plugins` directory created in the previous step so that the plugin can be loaded.
- Add the source code of the application (e.g. spring-gateway/employee) to the workspace and configure the JVM options in the Debug menu to start the application with agent-dep.jar for debugging later.

  ```
  -javaagent:/path-to-agent/build/target/agent-dep.jar 
    -Dagent.config.path=/my-own-if-changed-or-add/agent.properties 
    -Dagent.log.conf=/my-own-if-changed/agent-log4j2.xml 
    -Dnet.bytebuddy.dump=/path-to-dump/
  ```
  The `path` above need to be replaced with the user's actual environment path.
- Set breakpoints, launch debug session.

### Enhancement Debug

- How can I determine whether target classes and methods are enhanced?
  The following debug options were set in step 3 of the previous section of environment configuration:
  ```
  -Dnet.bytebuddy.dump=/path-to-dump/
  ```
  
  The class files of all the enhanced classes will be printed in this directory. Decompile the class files (IDEA can pull them in and open them directly) to see if the corresponding method has the enhanced bytecode to call the Agent method.

- If the check confirms that the target method is not enhanced, how do I debug it?
  There are three key checkpoints: ClassMatchers, MethodMatchers and all other issues.
  1. All classes that are matched will run into the `ForAdviceTransformer::transform(..)` method, where conditional breakpoints can be added, then checking ClassMatchers if the breakpoint is not interrupted.
  2. All methods matched will run into the `AdviceRegistry::check(..)` method, where conditional breakpoints can be added, then checking MethodMatchers if the breakpoint is not interrupted.
  3. Set a breakpoint by going back through the breakpoint stack in step 1 and navigating to the **ByteBuddy** source code where throw exception when enhance fail, check the cause of the exception.

### 