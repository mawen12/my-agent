package com.mawen.agent.httpserver.nanohttpd.protocols.http.tempfiles;

/**
 * Temp file manager.
 *
 * <p>Temp file managers are created 1-to-1 with incoming requests, to create and
 * cleanup temporary files created as a result of handling the request.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface ITempFileManager {

	void clear();

	ITempFile createTempFile(String fileName) throws Exception;
}
