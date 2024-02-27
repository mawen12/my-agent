package com.mawen.agent.httpserver.nanohttpd.protocols.http.content;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
@Getter
public class ContentType {

	private static final String ASCII_ENCODING = "US-ASCII";
	private static final String MULTIPART_FORM_DATA_HEADER = "multipart/form-data";
	private static final String CONTEXT_REGEX = "[ |\t]*([^/^ ^;^,]+/[^ ^;^,]+)";
	private static final Pattern MIME_PATTERN = Pattern.compile(CONTEXT_REGEX, Pattern.CASE_INSENSITIVE);
	private static final String CHARSET_REGEX = "[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
	private static final Pattern CHARSET_PATTERN = Pattern.compile(CHARSET_REGEX, Pattern.CASE_INSENSITIVE);
	private static final String BOUNDARY_REGEX = "[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
	private static final Pattern BOUNDARY_PATTERN = Pattern.compile(BOUNDARY_REGEX, Pattern.CASE_INSENSITIVE);

	private final String contentTypeHeader;
	private final String contentType;
	private final String encoding;
	private final String boundary;

	public ContentType(String contentTypeHeader) {
		this.contentTypeHeader = contentTypeHeader;
		if (contentTypeHeader != null) {
			contentType = getDetailFromContentHeader(contentTypeHeader, MIME_PATTERN, "", 1);
			encoding = getDetailFromContentHeader(contentTypeHeader, CHARSET_PATTERN, null, 2);
		} else {
			contentType = "";
			encoding = "UTF-8";
		}
		if (MULTIPART_FORM_DATA_HEADER.equalsIgnoreCase(contentType)) {
			boundary = getDetailFromContentHeader(contentTypeHeader, BOUNDARY_PATTERN, null, 2);
		} else {
			boundary = null;
		}
	}

	public String getEncoding() {
		return encoding == null ? ASCII_ENCODING : encoding;
	}

	public boolean isMultipart() {
		return MULTIPART_FORM_DATA_HEADER.equalsIgnoreCase(contentType);
	}

	public ContentType tryUTF8() {
		if (encoding == null) {
			new ContentType(this.contentTypeHeader + "; charset=utf-8");
		}
		return this;
	}

	private String getDetailFromContentHeader(String contentTypeHeader, Pattern pattern, String defaultValue, int group) {
		Matcher matcher = pattern.matcher(contentTypeHeader);
		return matcher.find() ? matcher.group(group) : defaultValue;
	}

}
