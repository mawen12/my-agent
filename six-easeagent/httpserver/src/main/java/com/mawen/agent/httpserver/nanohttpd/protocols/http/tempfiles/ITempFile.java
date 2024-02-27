package com.mawen.agent.httpserver.nanohttpd.protocols.http.tempfiles;

import java.io.OutputStream;

/**
 * A temp file.
 *
 * <p>Temp files are responsible for managing the actual temporary storage and
 * cleaning themselves up when no longer needed.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface ITempFile {

	void delete() throws Exception;

	String getName();

	OutputStream open() throws Exception;
}
