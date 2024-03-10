package com.mawen.agent.httpserver.nanohttpd.protocols.http.tempfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.NanoHTTPD;

/**
 * Default strategy for creating and cleaning up temporary files.
 *
 * <p>This class stores its files in the standard location (that is,
 * wherever {@code java.io.tmpdir} points to). Files are added to an internal list,
 * and deleted when no longer needed (that is, when {@link #clear()} is invoked at the end of processing a request).
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class DefaultTempFileManager implements ITempFileManager{

	private final File tmpdir;
	private final List<ITempFile> tempFiles;

	public DefaultTempFileManager() {
		this.tmpdir = new File(System.getProperty("java.io.tmpdir"));
		if (!tmpdir.exists()) {
			tmpdir.mkdirs();
		}
		this.tempFiles = new ArrayList<>();
	}

	@Override
	public void clear() {
		for (ITempFile file : this.tempFiles) {
			try {
				file.delete();
			}
			catch (Exception e) {
				NanoHTTPD.LOGGER.log(Level.WARNING, "could not delete file ", e);
			}
		}
		this.tempFiles.clear();
	}

	@Override
	public ITempFile createTempFile(String fileName) throws Exception {
		DefaultTempFile tempFile = new DefaultTempFile(this.tmpdir);
		this.tempFiles.add(tempFile);
		return tempFile;
	}
}
