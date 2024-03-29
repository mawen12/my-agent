package com.mawen.agent.httpserver.nanohttpd.protocols.http.response;

/**
 * Some HTTP response status codes
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">Status code</a>
 */
public enum Status implements IStatus {

	SWITCH_PROTOCOL(101, "Switching Protocols"),

	OK(200, "OK"),
	CREATED(201, "Created"),
	ACCEPTED(202, "Accepted"),
	NO_CONTENT(204, "No Content"),
	PARTIAL_CONTENT(206, "Partial Content"),
	MULTI_STATUS(207, "Multi-Status"),

	REDIRECT(301, "Moved Permanently"),
	/**
	 * Many user agents mishandle 302 in ways that violate the RFC1945 spec
	 * (i.e., redirect a POST to GET). 303 and 307 were added in RFC2616 to
	 * address this. You should prefer 303 and 307 unless the calling user agent
	 * does not support 303 and 307 functionality
	 */
	@Deprecated
	FOUND(302, "Found"),
	REDIRECT_SETT_OTHER(303, "See Other"),
	NOT_MODIFIED(304, "Not Modified"),
	TEMPORARY_REDIRECT(307, "Temporary Redirect"),

	BAD_REQUEST(400, "Bad Request"),
	UNAUTHORIZED(401, "Unauthorized"),
	FORBIDDEN(403, "Forbidden"),
	NOT_FOUND(404, "Not Found"),
	METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	NOT_ACCEPTABLE(406, "Not Acceptable"),
	REQUEST_TIMEOUT(408, "Request Timeout"),
	CONFLICT(409, "Conflict"),
	GONE(410, "Gone"),
	LENGTH_REQUIRED(411, "Length Required"),
	PRECONDITION_FAILED(412, "Precondition Failed"),
	PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
	UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
	RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
	EXPECTATION_FAILED(417, "Expectation Failed"),
	TOO_MANY_REQUESTS(429, "Too Many Requests"),

	INTERNAL_ERROR(500, "Internal Server Error"),
	NOT_IMPLEMENTED(501, "Not Implemented"),
	SERVICE_UNAVAILABLE(503, "Service Unavailable"),
	UNSUPPORTED_HTTP_VERSION(505, "HTTP Version Not Supported"),
	;

	private final int requestStatus;
	private final String description;

	Status(int requestStatus, String description) {
		this.requestStatus = requestStatus;
		this.description = description;
	}

	public static Status lookup(int requestStatus) {
		for (var status : Status.values()) {
			if (status.requestStatus == requestStatus) {
				return status;
			}
		}
		return null;
	}

	public String getDescription() {
		return "" + this.requestStatus + " " + this.description;
	}

	@Override
	public int getRequestStatus() {
		return requestStatus;
	}
}
