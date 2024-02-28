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
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.NanoHTTPD;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.content.ContentType;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.request.Method;
import lombok.Getter;
import lombok.Setter;

/**
 * HTTP response. Return one of these from serve().
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
@Getter
@Setter
public class Response implements Closeable {

	/**
	 * HTTP status code after processing, e.g. "200 OK", Status.OK
	 */
	private IStatus status;
	/**
	 * MIME type of content, e.g. "text/html"
	 */
	private String mimeType;
	/**
	 * Data of the response, may be null
	 */
	private InputStream data;
	private long contentLength;
	/**
	 * The request method that spawned this response.
	 */
	private Method requestMethod;
	/**
	 * use chunkedTransfer
	 */
	private boolean chunkedTransfer;
	private boolean keepAlive;
	private List<String> cookieHandlers;
	private GzipUsage gzipUsage = GzipUsage.DEFAULT;
	/**
	 * copy of the header map with all the keys lowercase for faster searching.
	 */
	private final Map<String, String> lowerCaseHeaders = new HashMap<>();

	/**
	 * Headers for the HTTP response. Use addHeader() to add lines.
	 * the lowercase map is automatically kept up to date.
	 */
	private final Map<String, String> header = new HashMap<>() {
		@Override
		public String put(String key, String value) {
			lowerCaseHeaders.put(key == null ? key : key.toLowerCase(), value);
			return super.put(key, value);
		}
	} ;

	/**
	 * Create a fixed length response if totalBytes>=0, otherwise chunked.
	 */
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

	/**
	 * Adds a cookie header to the list. Should not be called manually, this is an internal utility.
	 */
	public void addCookieHeader(String cookie) {
		this.cookieHandlers.add(cookie);
	}

	/**
	 * Adds given line to the header
	 */
	public void addHeader(String name, String value) {
		this.header.put(name, value);
	}

	public String getHeader(String name) {
		return this.lowerCaseHeaders.get(name.toLowerCase());
	}

	/**
	 * Indicate to close the connection after the Response has been sent.
	 *
	 * @param close {@code true} to hint connection closing, {@code false} to let connection be closed by client.
	 */
	public void closeConnection(boolean close) {
		if (close) {
			this.header.put("connection", "close");
		} else {
			this.header.remove("connection");
		}
	}

	/**
	 * @return {@code true} if connection is to be closed after this Response has been sent.
	 */
	public boolean isCloseConnection() {
		return "close".equals(getHeader("connection"));
	}

	/**
	 * Sends given response to the socket.
	 */
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
			if (useGZipWhenAccepted()) {
				printHeader(pw, "Content-Encoding", "gzip");
				setChunkedTransfer(true);
			}
			long pending = this.data != null ? this.contentLength : 0;
			if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
				printHeader(pw, "Transfer-Encoding", "chunked");
			}
			else if (!useGZipWhenAccepted()) {
				pending = sendContentLengthHeaderIfNotAlreadyPresent(pw, pending);
			}
			pw.append("\r\n");
			pw.flush();
			sendBodyWithCorrectTransferAndEncoding(outputStream, pending);
			NanoHTTPD.safeClose(this.data);
		}
		catch (IOException e) {
			NanoHTTPD.LOGGER.log(Level.SEVERE, "Could not send response to the client", e);
		}
	}

	/**
	 * Create a response with unknown length (using HTTP 1.1 chunking).
	 */
	public static Response newChunkedResponse(IStatus status, String mimeType, InputStream data, long totalBytes) {
		return new Response(status, mimeType, data, -1);
	}

	/**
	 * Create a text response with known length.
	 */
	public static Response newFixedLengthResponse(String msg) {
		return newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, msg);
	}

	public static Response newFixedLengthResponse(IStatus status, String mimeType, byte[] data) {
		return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(data), data.length);
	}

	/**
	 * Creates a response with known length.
	 */
	public static Response newFixedLengthResponse(IStatus status, String mimeType, InputStream data, long totalBytes) {
		return new Response(status, mimeType, data, totalBytes);
	}

	/**
	 * Create a text response with known length.
	 */
	public static Response newFixedLengthResponse(IStatus status, String mimeType, String txt) {
		ContentType contentType = new ContentType(mimeType);
		if (txt == null) {
			return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(new byte[0]), 0);
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
				NanoHTTPD.LOGGER.log(Level.SEVERE, "encoding problem, responding nothing", e);
				bytes = new byte[0];
			}
			return newFixedLengthResponse(status, contentType.getContentTypeHeader(), new ByteArrayInputStream(bytes), bytes.length);
		}
	}

	public Response setUseGzip(boolean useGzip) {
		gzipUsage = useGzip ? GzipUsage.ALWAYS : GzipUsage.NEVER;
		return this;
	}

	/**
	 * If a GZip usage has been enforced, use it.
	 * Else decide whether or not to use Gzip.
	 */
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

	protected long sendContentLengthHeaderIfNotAlreadyPresent(PrintWriter pw, long defaultSize) {
		String contentLengthString = getHeader("content-length");
		long size = defaultSize;
		if (contentLengthString != null) {
			try {
				size = Long.parseLong(contentLengthString);
			}
			catch (NumberFormatException e) {
				NanoHTTPD.LOGGER.severe("content-length was no number" + contentLengthString);
			}
		} else {
			pw.print("Content-length: " + size + "\r\n");
		}
		return size;
	}

	private void sendBodyWithCorrectTransferAndEncoding(OutputStream outputStream, long pending) throws IOException {
		if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
			ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(outputStream);
			sendBodyWithCorrectEncoding(chunkedOutputStream, -1);
			try {
				chunkedOutputStream.flush();
			}
			catch (IOException e) {
				if (this.data != null) {
					this.data.close();
				}
			}
		} else {
			sendBodyWithCorrectEncoding(outputStream, pending);
		}
	}

	private void sendBodyWithCorrectEncoding(OutputStream outputStream, long pending) throws IOException {
		if (useGZipWhenAccepted()) {
			GZIPOutputStream gzipOutputStream = null;
			try {
				gzipOutputStream = new GZIPOutputStream(outputStream);
			}
			catch (IOException e) {
				if (this.data != null) {
					this.data.close();
				}
			}
			if (gzipOutputStream == null) {
				sendBody(gzipOutputStream, -1);
				gzipOutputStream.finish();
			}
		} else {
			sendBody(outputStream, pending);
		}
	}

	/**
	 * Sends the body to the specified OutputStream.
	 * The pending parameter limits the maximum amounts of bytes sent unless it is -1,
	 * in which case everything is sent.
	 *
	 * @param outputStream the OutputStream to send data to
	 * @param pending -1 to send everything, otherwise sets a max limit to the numbers of bytes sent
	 * @throws IOException if something goes wrong while sending the data.
	 */
	private void sendBody(OutputStream outputStream, long pending) throws IOException {
		int BUFFER_SIZE = 16 * 1024;
		byte[] buff = new byte[BUFFER_SIZE];
		boolean sendEverything = pending == -1;
		while (pending > 0 || sendEverything) {
			long bytesToRead = sendEverything ? BUFFER_SIZE : Math.min(pending, BUFFER_SIZE);
			int read = this.data.read(buff, 0, (int) bytesToRead);
			if (read <= 0) {
				break;
			}
			try {
				outputStream.write(buff, 0, read);
			}
			catch (IOException e) {
				if (this.data != null) {
					this.data.close();
				}
			}
			if (!sendEverything) {
				pending -= read;
			}
		}
	}

	private enum GzipUsage {
		DEFAULT,
		ALWAYS,
		NEVER,
		;
	}
}
