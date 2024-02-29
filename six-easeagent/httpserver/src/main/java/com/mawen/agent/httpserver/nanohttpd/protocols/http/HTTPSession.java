package com.mawen.agent.httpserver.nanohttpd.protocols.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;

import javax.net.ssl.SSLException;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.content.ContentType;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.content.CookieHandler;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.request.Method;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.tempfiles.ITempFile;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.tempfiles.ITempFileManager;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class HTTPSession implements IHTTPSession {

	public static final String POST_DATA = "postData";
	public static final int MAX_HEADER_SIZE = 1024;

	private static final int REQUEST_BUFFER_LEN = 512;
	private static final int MEMORY_STORE_LIMIT = 1024;
	private static final int BUF_SIZE = 8192;

	private final NanoHTTPD httpd;
	private final ITempFileManager tempFileManager;
	private final OutputStream outputStream;
	private final BufferedInputStream inputStream;
	private int splitbyte;
	private int rlen;
	private String uri;
	private Method method;
	private Map<String, List<String>> parms;
	private Map<String, String> headers;
	private CookieHandler cookies;
	private String queryParameterString;
	private String remoteIp;
	private String protocolVersion;

	public HTTPSession(NanoHTTPD httpd, ITempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream) {
		this.httpd = httpd;
		this.tempFileManager = tempFileManager;
		this.inputStream = new BufferedInputStream(inputStream, HTTPSession.BUF_SIZE);
		this.outputStream = outputStream;
	}

	public HTTPSession(NanoHTTPD httpd, ITempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream, InetAddress inetAddress) {
		this.httpd = httpd;
		this.tempFileManager = tempFileManager;
		this.inputStream = new BufferedInputStream(inputStream, HTTPSession.BUF_SIZE);
		this.outputStream = outputStream;
		this.remoteIp = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "127.0.0.1" : inetAddress.getHostAddress().toString();
		this.headers = new HashMap<>();
	}

	@Override
	public void execute() throws IOException {
		Response r = null;
		try {
			// Read the first 8192 bytes.
			// The full header should fit in here.
			// Apache's default header limit is 8KB.
			// Do NOT assume that a single read will get the entire header
			// at once!
			byte[] buf = new byte[HTTPSession.BUF_SIZE];
			this.splitbyte = 0;
			this.rlen = 0;

			int read = -1;
			this.inputStream.mark(HTTPSession.BUF_SIZE);
			try {
				read = this.inputStream.read(buf, 0, HTTPSession.BUF_SIZE);
			}
			catch (SSLException e) {
				throw e;
			}
			catch (IOException e) {
				NanoHTTPD.safeClose(this.inputStream);
				NanoHTTPD.safeClose(this.outputStream);
				throw new SocketException("NanoHttpd Shutdown");
			}

			if (read == -1) {
				// socket was been close
				NanoHTTPD.safeClose(this.inputStream);
				NanoHTTPD.safeClose(this.outputStream);
				throw new SocketException("NanoHttpd Shutdown");
			}

			while (read > 0) {
				this.rlen += read;
				this.splitbyte = findHeaderEnd(buf, this.rlen);
				if (this.splitbyte > 0) {
					break;
				}
				read = this.inputStream.read(buf, this.rlen, HTTPSession.BUF_SIZE - this.rlen);
			}

			if (this.splitbyte < this.rlen) {
				this.inputStream.reset();
				this.inputStream.skip(this.splitbyte);
			}

			this.parms = new HashMap<>();
			if (null == this.headers) {
				this.headers = new HashMap<>();
			}
			else {
				this.headers.clear();
			}

			// Create a BufferedReader for parsing the header.
			BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, this.rlen)));

			// Decode the header into parms and header java properties
			Map<String, String> pre = new HashMap<>();
			decodeHeader(hin, pre, this.parms, this.headers);

			if (null != this.remoteIp) {
				this.headers.put("remote-addr", this.remoteIp);
				this.headers.put("http-client-ip", this.remoteIp);
			}

			this.method = Method.lookup(pre.get("method"));
			if (this.method == null) {
				throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Syntax error. HTTP verb " + pre.get("method") + " unhandled.");
			}

			this.uri = pre.get("uri");
			this.cookies = new CookieHandler(this.headers);

			String connection = this.headers.get("connection");
			boolean keepAlive = "HTTP/1.1".equals(protocolVersion) && (connection == null || !connection.matches("(?i).*close.*"));

			// Ok, now do the serve()
			r = httpd.handle(this);

			if (r == null) {
				throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
			}
			else {
				String acceptEncoding = this.headers.get("accept-encoding");
				this.cookies.unloadQueue(r);
				r.setRequestMethod(this.method);
				if (acceptEncoding == null || !acceptEncoding.contains("gzip")) {
					r.setUseGzip(false);
				}
				r.setKeepAlive(keepAlive);
				r.send(this.outputStream);
			}
			if (!keepAlive || r.isCloseConnection()) {
				throw new SocketException("NanoHttpd Shutdown");
			}
		}
		catch (SocketException e) {
			// throw it out to close socket object (finalAccept)
			throw e;
		}
		catch (SocketTimeoutException ste) {
			// treat socket timeouts the same way we treat socket exceptions
			// i.e. close the stream & finalAccept object by throwing the
			// exception up the call back.
			throw ste;
		}
		catch (SSLException ssle) {
			Response resp = Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "SSL PROTOCOL FAILURE: " + ssle.getMessage());
			resp.send(this.outputStream);
			NanoHTTPD.safeClose(this.outputStream);
		}
		catch (IOException ioe) {
			Response resp = Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			resp.send(this.outputStream);
			NanoHTTPD.safeClose(this.outputStream);
		}
		catch (NanoHTTPD.ResponseException re) {
			Response resp = Response.newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re.getMessage());
			resp.send(this.outputStream);
			NanoHTTPD.safeClose(this.outputStream);
		}
		finally {
			NanoHTTPD.safeClose(r);
			this.tempFileManager.clear();
		}
	}

	@Override
	public CookieHandler getCookies() {
		return this.cookies;
	}

	@Override
	public Map<String, String> getHeaders() {
		return this.headers;
	}

	@Override
	public InputStream getInputStream() {
		return this.inputStream;
	}

	@Override
	public Method getMethod() {
		return this.method;
	}

	/**
	 * @deprecated use {@link #getParameters()} instead.
	 */
	@Deprecated
	@Override
	public Map<String, String> getParams() {
		Map<String, String> results = new HashMap<>();
		for (String key : this.parms.keySet()) {
			results.put(key, this.parms.get(key).get(0));
		}
		return results;
	}

	@Override
	public Map<String, List<String>> getParameters() {
		return this.parms;
	}

	@Override
	public String getQueryParameterString() {
		return this.queryParameterString;
	}

	@Override
	public String getUri() {
		return this.uri;
	}

	@Override
	public void parseBody(Map<String, String> files) throws IOException, NanoHTTPD.ResponseException {
		RandomAccessFile randomAccessFile = null;
		try {
			long size = getBodySize();
			ByteArrayOutputStream baos = null;
			DataOutput requestDataOutput = null;

			// Store the request in memory or a file, depending on size
			if (size < MEMORY_STORE_LIMIT) {
				baos = new ByteArrayOutputStream();
				requestDataOutput = new DataOutputStream(baos);
			}
			else {
				randomAccessFile = getTmpBucket();
				requestDataOutput = randomAccessFile;
			}

			// Read all the body and write it to request_data_output
			byte[] buf = new byte[REQUEST_BUFFER_LEN];
			while (this.rlen >= 0 && size > 0) {
				this.rlen = this.inputStream.read(buf, 0, (int) Math.min(size, REQUEST_BUFFER_LEN));
				size -= this.rlen;
				if (this.rlen > 0) {
					requestDataOutput.write(buf, 0, this.rlen);
				}
			}

			ByteBuffer fbuf = null;
			if (baos != null) {
				fbuf = ByteBuffer.wrap(baos.toByteArray(), 0, baos.size());
			}
			else {
				fbuf = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
				randomAccessFile.seek(0);
			}

			// If the method is POST, there may be parameters
			// in data section, too, read it:
			if (Method.POST.equals(this.method)) {
				ContentType contentType = new ContentType(this.headers.get("content-type"));
				if (contentType.isMultipart()) {
					String boundary = contentType.getBoundary();
					if (boundary == null) {
						throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
					}
					decodeMultipartFormData(contentType, fbuf, this.parms, files);
				} else {
					byte[] postBytes = new byte[fbuf.remaining()];
					fbuf.get(postBytes);
					String postLine = new String(postBytes, contentType.getEncoding()).trim();
					// Handle application/x-www-form-urlencoded
					if ("application/x-www-form-urlencoded".equals(contentType.getContentType())) {
						decodeParams(postLine, this.parms);
					}
					else if (postLine.length() != 0) {
						files.put(POST_DATA, postLine);
					}
				}
			}
			else if (Method.PUT.equals(this.method)) {
				files.put("content", saveTmpFile(fbuf, 0, fbuf.limit(), null));
			}
		}
		finally {
			NanoHTTPD.safeClose(randomAccessFile);
		}
	}

	@Override
	public String getRemoteIpAddress() {
		return this.remoteIp;
	}

	/**
	 * Deduce body length in bytes. Either from "content-length" header or read bytes.
	 */
	public long getBodySize() {
		if (this.headers.containsKey("content-length")) {
			return Long.parseLong(this.headers.get("content-length"));
		}
		else if (this.splitbyte < this.rlen) {
			return this.rlen - this.splitbyte;
		}
		return 0;
	}

	/**
	 * Find byte index separating header from body.
	 * It must be the last byte of the first two sequential new lines.
	 */
	private int findHeaderEnd(final byte[] buf, int rlen) {
		int splitbyte = 0;
		while (splitbyte + 1 < rlen) {

			// RFC2616
			if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && splitbyte + 3 < rlen && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n') {
				return splitbyte + 4;
			}

			// tolerance
			if (buf[splitbyte] == '\n' && buf[splitbyte + 1] == '\n') {
				return splitbyte + 2;
			}
			splitbyte++;
		}
		return 0;
	}

	private RandomAccessFile getTmpBucket() {
		try {
			ITempFile tempFile = this.tempFileManager.createTempFile(null);
			return new RandomAccessFile(tempFile.getName(), "rw");
		}
		catch (Exception e) {
			throw new Error(e); // we won't recover, so throw an error
		}
	}

	/**
	 * Decodes the sent headers and loads the data into Key/value pairs
	 */
	private void decodeHeader(BufferedReader in, Map<String, String> pre, Map<String, List<String>> parms, Map<String, String> headers) throws NanoHTTPD.ResponseException {
		try {
			String inLine = in.readLine();
			if (inLine == null) {
				return;
			}

			StringTokenizer st = new StringTokenizer(inLine);
			if (!st.hasMoreTokens()) {
				throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
			}

			pre.put("method", st.nextToken());

			if (!st.hasMoreTokens()) {
				throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
			}

			String uri = st.nextToken();

			// Decode parameters from the URI
			int qmi = uri.indexOf('?');
			if (qmi >= 0) {
				decodeParams(uri.substring(qmi + 1), parms);
				uri = NanoHTTPD.decodePercent(uri.substring(0, qmi));
			} else {
				uri = NanoHTTPD.decodePercent(uri);
			}

			// If there's another token, its protocol version,
			// followed by HTTP headers.
			// NOTE: this now forces header names lower case since they are
			// case insensitive and vary by client.
			if (st.hasMoreTokens()) {
				protocolVersion = st.nextToken();
			} else {
				protocolVersion = "HTTP/1.1";
				NanoHTTPD.LOGGER.log(Level.FINE, "no protocol version specified, strange. Assuming HTTP/1.1.");
			}
			String line = in.readLine();
			while (line != null && !line.trim().isEmpty()) {
				int p = line.indexOf(':');
				if (p > 0) {
					headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
				}
				line = in.readLine();
			}

			pre.put("uri", uri);
		}
		catch (IOException e) {
			throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + e.getMessage(), e);
		}
	}

	/**
	 * Decodes parameters in percent-encoded URI-format ( e.g.
	 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given Map.
	 */
	private void decodeParams(String parms, Map<String, List<String>> p) {
		if (parms == null) {
			this.queryParameterString = "";
			return;
		}

		this.queryParameterString = parms;
		StringTokenizer st = new StringTokenizer(parms, "&");
		while (st.hasMoreTokens()) {
			String e = st.nextToken();
			int sep = e.indexOf('=');
			String key = null;
			String value = null;

			if (sep >= 0) {
				key = NanoHTTPD.decodePercent(e.substring(0, sep)).trim();
				value = NanoHTTPD.decodePercent(e.substring(sep + 1));
			} else {
				key = NanoHTTPD.decodePercent(e).trim();
				value = "";
			}

			List<String> values = p.get(key);
			if (values == null) {
				values = new ArrayList<>();
				p.put(key, values);
			}

			values.add(value);
		}
	}

	/**
	 * Decodes the Multipart Body data and put it into Key/value pairs.
	 */
	private void decodeMultipartFormData(ContentType contentType, ByteBuffer fbuf, Map<String, List<String>> parms, Map<String, String> files) throws NanoHTTPD.ResponseException {
		int pcount = 0;
		try {
			int[] boundaryIdxs = getBoundaryPositions(fbuf, contentType.getBoundary().getBytes());
			if (boundaryIdxs.length < 2) {
				throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but contains less than to two boundary strings.");
			}

			byte[] partHeaderBuff = new byte[MAX_HEADER_SIZE];
			for (int boundaryIdx = 0; boundaryIdx < boundaryIdxs.length - 1; boundaryIdx++) {
				fbuf.position(boundaryIdxs[boundaryIdx]);
				int len = (fbuf.remaining() < MAX_HEADER_SIZE) ? fbuf.remaining() : MAX_HEADER_SIZE;
				fbuf.get(partHeaderBuff, 0, len);
				BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(partHeaderBuff, 0, len), Charset.forName(contentType.getEncoding())), len);

				int headerLines = 0;
				// First line is boundary string
				String mpline = in.readLine();
				headerLines++;
				if (mpline == null || !mpline.contains(contentType.getBoundary())) {
					throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
				}

				String partName = null, fileName = null, partContentType = null;
				// Parse the reset of the header lines
				mpline = in.readLine();
				headerLines++;
				while (mpline != null && mpline.trim().length() > 0) {
					Matcher matcher = NanoHTTPD.CONTENT_DISPOSITION_PATTERN.matcher(mpline);
					if (matcher.matches()) {
						String attributeString = matcher.group(2);
						matcher = NanoHTTPD.CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
						while (matcher.find()) {
							String key = matcher.group(1);
							if ("name".equalsIgnoreCase(key)) {
								partName = matcher.group(2);
							}
							else if ("filename".equalsIgnoreCase(key)) {
								fileName = matcher.group(2);
								// add these two line to support multiple
								// files uploaded using the same field Id
								if (!fileName.isEmpty()) {
									if (pcount > 0) {
										partName = partName + String.valueOf(pcount++);
									}
									else {
										pcount++;
									}
								}
							}
						}
					}
					matcher = NanoHTTPD.CONTENT_TYPE_PATTERN.matcher(mpline);
					if (matcher.matches()) {
						partContentType = matcher.group(2).trim();
					}
					mpline = in.readLine();
					headerLines++;
				}
				int partHeaderLength = 0;
				while (headerLines-- > 0) {
					partHeaderLength = scipOverNewLine(partHeaderBuff, partHeaderLength);
				}

				// Read the part data
				if (partHeaderLength >= len - 4) {
					throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
				}
				int partDataStart = boundaryIdxs[boundaryIdx] + partHeaderLength;
				int partDataEnd = boundaryIdxs[boundaryIdx + 1] - 4;

				fbuf.position(partDataStart);

				List<String> values = parms.get(partName);
				if (values == null) {
					values = new ArrayList<>();
					parms.put(partName, values);
				}

				if (partContentType == null) {
					// Read the part into a string
					byte[] dataBytes = new byte[partDataEnd - partDataStart];
					fbuf.get(dataBytes);

					values.add(new String(dataBytes, contentType.getEncoding()));
				}
				else {
					// Read it into a file
					String path = saveTmpFile(fbuf, partDataStart, partDataEnd - partDataStart, fileName);
					if (!files.containsKey(partName)) {
						files.put(partName, path);
					}
					else {
						int count = 2;
						while (files.containsKey(partName + count)) {
							count++;
						}
						files.put(partName + count, path);
					}
					values.add(fileName);
				}
			}
		}
		catch (NanoHTTPD.ResponseException e) {
			throw e;
		}
		catch (Exception e) {
			throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, e.toString());
		}
	}

	/**
	 * Find the byte positions where multipart boundaries start. This reads a
	 * large block at a time and uses a temporary buffer to optimize (memory
	 * mapped) file access.
	 */
	private int[] getBoundaryPositions(ByteBuffer buf, byte[] boundary) {
		int[] res = new int[0];
		if (buf.remaining() < boundary.length) {
			return res;
		}

		int searchWindowPos = 0;
		byte[] searchWindow = new byte[4 * 1024 + boundary.length];

		int firstFill = (buf.remaining() < searchWindow.length) ? buf.remaining() : searchWindow.length;
		buf.get(searchWindow, 0, firstFill);
		int newBytes = firstFill - boundary.length;

		do {
			// Search the searchWindow
			for (int i = 0; i < newBytes; i++) {
				for (int j = 0; j < boundary.length; j++) {
					if (searchWindow[i + j] != boundary[j]) {
						break;
					}
					if (j == boundary.length - 1) {
						// Match found, add it to results
						int[] newRes = new int[res.length - 1];
						System.arraycopy(res, 0, newRes, 0, res.length);
						newRes[res.length] = searchWindowPos + i;
						res = newRes;
					}
				}
			}
			searchWindowPos += newBytes;

			// Copy the end of the buffer to the start
			System.arraycopy(searchWindow, searchWindow.length - boundary.length, searchWindow, 0, boundary.length);

			// Refill searchWindow
			newBytes = searchWindow.length - boundary.length;
			newBytes = (buf.remaining() < newBytes) ? buf.remaining() : newBytes;
			buf.get(searchWindow, boundary.length, newBytes);
		} while (newBytes > 0);
		return res;
	}

	private int scipOverNewLine(byte[] partHeaderBuff, int index) {
		while (partHeaderBuff[index] != '\n') {
			index++;
		}
		return ++index;
	}

	/**
	 * Retrieves the content of a sent file and saves it to a temporary file.
	 * The full path to the saved file is returned.
	 */
	private String saveTmpFile(ByteBuffer buf, int offset, int len, String fileNameHint) {
		String path = "";
		if (len > 0) {
			FileOutputStream fileOutputStream = null;
			try {
				ITempFile tempFile = this.tempFileManager.createTempFile(fileNameHint);
				ByteBuffer src = buf.duplicate();
				fileOutputStream = new FileOutputStream(tempFile.getName());
				FileChannel dest = fileOutputStream.getChannel();
				src.position(offset).limit(offset + len);
				dest.write(src.slice());
				path = tempFile.getName();
			}
			catch (Exception e) {// Catch exception if any
				throw new Error(e); // we won't recover, so throw an error
			}
			finally {
				NanoHTTPD.safeClose(fileOutputStream);
			}
		}
		return path;
	}
}