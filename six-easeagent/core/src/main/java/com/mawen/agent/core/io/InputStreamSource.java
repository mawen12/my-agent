package com.mawen.agent.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public interface InputStreamSource {

	InputStream getInputStream() throws IOException;

}
