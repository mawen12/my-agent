package com.mawen.agent.reporters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.mawen.agent.util.ArgumentUtils;
import com.mawen.agent.Reporter;
import com.mawen.agent.util.AgentLogger;
import com.mawen.agent.util.JsonUtils;
import com.mawen.agent.util.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class FileOutputReporter implements Reporter {
	private static final AgentLogger logger = AgentLogger.getLogger(FileOutputReporter.class.getName());

	private static final String ARG_OUTPUT_DIR = "outputDir";
	private static final String ARG_ENABLE_ROLLING = "enableRolling";
	private static final String ARG_ROLLING_SIZE = "rollingSize";

	private String directory;
	private volatile boolean closed = true;
	private boolean enableRolling = false;
	private Long rollingSize = StringUtils.getBytesValueOrNull("128mb");

	public FileOutputReporter() {
	}

	public String getDirectory() {
		return directory;
	}

	// This method sets the output directory. By default, this reporter will create a temporary directory
	// and use it as output directory. User could set the output director if want to use another one. But
	// the output directory can only be set at most once. Setting it again will throw exception.
	public void setDirectory(String directory) {
		synchronized (this) {
			if (this.directory == null || this.directory.isEmpty()) {
				Path path = Paths.get(directory);
				try {
					if (!Files.exists(path)) {
						Files.createDirectory(path);
					}
				}
				catch (IOException e) {
					throw new RuntimeException("Failed to create direction: " + path, e);
				}

				this.directory = directory;
			} else {
				throw new RuntimeException(String.format("Cannot set directory to %s because it is already has value %s", directory, this.directory));
			}
		}
	}

	@Override
	public void updateArguments(Map<String, List<String>> parsedArgs) {
		String outputDir = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_OUTPUT_DIR);
		String enableRolling = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_ENABLE_ROLLING);
		String rollingSize = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_ROLLING_SIZE);
		if (ArgumentUtils.needToUpdateArg(outputDir)) {
			setDirectory(outputDir);
			logger.info("Got argument value for outputDir: " + outputDir);
		}

		if (ArgumentUtils.needToUpdateRollingArg(enableRolling)) {
			setAndCheckRollingArg(rollingSize);
			logger.info("Got argument value for rollingSize: " + rollingSize);
		}
	}

	private void setAndCheckRollingArg(String rollingSize) {
		synchronized (this) {
			this.enableRolling = true;
			if (rollingSize != null && !rollingSize.isEmpty()) {
				this.rollingSize = StringUtils.getBytesValueOrNull(rollingSize);
			} else {
				logger.info("Rolling size is default value: 128mb");
			}
		}
	}

	@Override
	public void report(String profilerName, Map<String, Object> metrics) {
		if (closed) {
			logger.info("Report already closed, do not report metrics");
			return;
		}
		ensureFile();
		try (FileWriter writer = createFileWriter(profilerName, needRolling(profilerName))) {
			writer.write(JsonUtils.serialize(metrics));
			writer.write(System.lineSeparator());
			writer.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean needRolling(String profilerName) {
		synchronized (this) {
			File file = new File(Paths.get(directory, profilerName + ".json").toString());
			return enableRolling && file.length() > rollingSize;
		}
	}

	private void ensureFile() {
		synchronized (this) {
			if (directory == null || directory.isEmpty()) {
				try {
					directory = Files.createTempDirectory("jvm_profiler_").toString();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private FileWriter createFileWriter(String profileName, boolean needRolling) {
		String path = Paths.get(directory, profileName + ".json").toString();
		try {
			return new FileWriter(path, !needRolling);
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to create file writer: " + path, e);
		}
	}

	@Override
	public void close() {

	}
}
