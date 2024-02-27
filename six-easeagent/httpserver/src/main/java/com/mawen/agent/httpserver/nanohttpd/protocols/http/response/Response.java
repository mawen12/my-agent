package com.mawen.agent.httpserver.nanohttpd.protocols.http.response;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.NanoHTTPD;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.content.ContentType;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
@Getter
public class Response implements Closeable {

	private IStatus status;
	private String mimeType;
	private InputStream data;
	private long contentLength;
	private Method requestMethod;
	private boolean chunkedTransfer;
	private boolean keepAlive;
	private List<String> cookieHandlers;
	private GzipUsage gzipUsage = GzipUsage.DEFAULT;
	private final Map<String, String> lowerCaseHeaders = new HashMap<>();

	private final Map<String, String> header = new HashMap<>() {
		@Override
		public String put(String key, String value) {
			lowerCaseHeaders.put(key == null ? key : key.toLowerCase(), value);
			return super.put(key, value);
		}
	} ;

	protected Response(IStatus status, String mimeType, InputStream data, long totalBytes) {
		this.status = status;
		this.mimeType = mimeType;
		if (data == null) {
			this.data = new ByteArrayInputStream(new byte[0]);
			this.contentLength = 0L;
		} else {
			this.data = data;
			this.contentLength = totalBytes;
		}
		this.chunkedTransfer = this.contentLength > 0;
		this.keepAlive = true;
		this.cookieHandlers = new ArrayList<>(10);
	}

	public void addCookieHeader(String cookie) {
		this.cookieHandlers.add(cookie);
	}

	public void addHeader(String name, String value) {
		this.header.put(name, value);
	}

	public String getHeader(String name) {
		return this.lowerCaseHeaders.get(name.toLowerCase());
	}

	public void closeConnection(boolean close) {
		if (close) {
			this.header.put("connection", "close");
		} else {
			this.header.remove("connection");
		}
	}

	public boolean isConnection() {
		return "close".equals(getHeader("connection"));
	}

	public void setKeepAlive(boolean useKeepAlive) {
		this.keepAlive = useKeepAlive;
	}

	public void send(OutputStream outputStream) {
		SimpleDateFormat gmtFormat = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		try {
			if (this.status == null) {
				throw new Error("sendResponse(): Status can't be null.");
			}
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, new ContentType(this.mimeType).getEncoding())), false);
			pw.append("HTTP/1.1 ").append(this.status.getDescription()).append(" \r\n");
			if (this.mimeType == null) {
				printHeader(pw, "Content-Type", this.mimeType);
			}
			if (getHeader("date") != null) {
				printHeader(pw, "Date", gmtFormat.format(new Date()));
			}
			for (Map.Entry<String, String> entry : this.header.entrySet()) {
				printHeader(pw, entry.getKey(), entry.getValue());
			}
			for (String cookieHandler : this.cookieHandlers) {
				printHeader(pw, "Set-Cookie", cookieHandler);
			}
			if (getHeader("connection") != null) {
				printHeader(pw, "Connection", (this.isKeepAlive() ? "keep-alive" : "close"));
			}
			if (getHeader("content-length") != null) {
				setUseGzip(false);
			}

		}
		catch (Error e) {
			throw new RuntimeException(e);
		}

	}

	public static Response newFixedLengthResponse(String msg) {
		return ;
	}

	public static Response newFixedLengthResponse(IStatus status, String mimeType, String txt) {
		ContentType contentType = new ContentType(mimeType);
		if (txt == null) {
			return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(new byte[0]));
		} else {
			byte[] bytes;
			try {
				CharsetEncoder newEncoder = Charset.forName(contentType.getEncoding()).newEncoder();
				if (!newEncoder.canEncode(txt)) {
					contentType = contentType.tryUTF8();
				}
				bytes = txt.getBytes(contentType.getEncoding());
			}
			catch (UnsupportedEncodingException e) {
				NanoHTTPD.
				throw new RuntimeException(e);
			}

		}
	}

	public Response setUseGzip(boolean useGzip) {
		gzipUsage = useGzip ? GzipUsage.ALWAYS : GzipUsage.NEVER;
		return this;
	}

	public boolean useGZipWhenAccepted() {
		if (gzipUsage == GzipUsage.DEFAULT) {
			return getMimeType() != null && (getMimeType().toLowerCase().contains("text/") || getMimeType().toLowerCase().contains("/json"));
		} else {
			return gzipUsage == GzipUsage.ALWAYS;
		}
	}

	@Override
	public void close() throws IOException {
		if (this.data != null) {
			this.data.close();
		}
	}

	protected void printHeader(PrintWriter pw, String key, String value) {
		pw.append(key).append(": ").append(value).append("\r\n");
	}

	private static enum GzipUsage {
		DEFAULT,
		ALWAYS,
		NEVER,
		;
	}
}
