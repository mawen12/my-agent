package com.mawen.agent.httpserver.nanohttpd.protocols.http.tempfiles;

import com.mawen.agent.httpserver.nanohttpd.util.IFactory;

/**
 * Default strategy for creating and cleaning up temporary files.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class DefaultTempFileManagerFactory implements IFactory<ITempFileManager> {

	@Override
	public ITempFileManager create() {
		return new DefaultTempFileManager();
	}
}
