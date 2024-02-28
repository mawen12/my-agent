package com.mawen.agent.httpserver.nanohttpd.protocols.http.tempfiles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.NanoHTTPD;

/**
 * Default strategy for creating and cleaning up temporary files.
 *
 * <p>By default, files are created by {@link  File#createTempFile} in the directory specified.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class DefaultTempFile implements ITempFile{

	private final File file;
	private final OutputStream fstream;

	public DefaultTempFile(File tempdir) throws IOException {
		this.file = File.createTempFile("NanoHTTPD-", "", tempdir);
		this.fstream = new FileOutputStream(this.file);
	}

	@Override
	public void delete() throws Exception {
		NanoHTTPD.safeClose(this.fstream);
		if (!this.file.delete()) {
			throw new Exception("could not delete temporary file: " + file.getAbsolutePath());
		}
	}

	@Override
	public String getName() {
		return this.file.getAbsolutePath();
	}

	@Override
	public OutputStream open() throws Exception {
		return this.fstream;
	}
}
