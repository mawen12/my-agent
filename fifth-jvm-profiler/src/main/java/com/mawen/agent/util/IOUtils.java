package com.mawen.agent.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class IOUtils {
	public static byte[] toByteArray(InputStream input) {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			int readByteCount;
			byte[] data = new byte[16 * 1024];
			while ((readByteCount = input.read(data, 0, data.length)) != -1) {
				byteArrayOutputStream.write(data, 0, readByteCount);
			}
			byteArrayOutputStream.flush();
			return byteArrayOutputStream.toByteArray();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
