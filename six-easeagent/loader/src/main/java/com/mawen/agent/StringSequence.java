package com.mawen.agent;

import java.util.Objects;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/21
 */
public class StringSequence implements CharSequence {
	private final String source;

	private final int start;

	private final int end;

	private int hash;

	public StringSequence(String source) {
		this(source, 0, (source != null) ? source.length() : -1);
	}

	public StringSequence(String source, int start, int end) {
		Objects.requireNonNull(source, "Source must not be null");
		if (start < 0) {
			throw new StringIndexOutOfBoundsException(start);
		}
		if (end > source.length()) {
			throw new StringIndexOutOfBoundsException(end);
		}
		this.source = source;
		this.start = start;
		this.end = end;
	}

	StringSequence subSequence(int start) {
		return subSequence(start, length());
	}

	@Override
	public StringSequence subSequence(int start, int end) {
		int subSequenceStart = this.start + start;
		int subSequenceEnd = this.start + end;
		if (subSequenceStart > this.end) {
			throw new StringIndexOutOfBoundsException(subSequenceStart);
		}
		if (subSequenceEnd > this.end) {
			throw new StringIndexOutOfBoundsException(subSequenceEnd);
		}
		if (start == 0 && subSequenceEnd == this.end) {
			return this;
		}
		return new StringSequence(this.source, subSequenceStart, subSequenceEnd);
	}

	public boolean isEmpty() {
		return length() == 0;
	}

	@Override
	public int length() {
		return this.end - this.start;
	}

	@Override
	public char charAt(int index) {
		return this.source.charAt(this.start + index);
	}

	int indexOf(char ch) {
		return this.source.indexOf(ch, this.start) - this.start;
	}

	int indexOf(String str) {
		return this.source.indexOf(str, this.start) - this.start;
	}

	int indexOf(String str, int fromIndex) {
		return this.source.indexOf(str, this.start + fromIndex) - this.start;
	}

	boolean startsWith(String prefix) {
		return startsWith(prefix, 0);
	}

	boolean startsWith(String prefix, int offset) {
		int prefixLength = prefix.length();
		int length = length();
		if (length - prefixLength - offset < 0) {
			return false;
		}
		return this.source.startsWith(prefix, this.start + offset);
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StringSequence that)) return false;

		CharSequence other = (CharSequence) o;
		int n = length();
		if (n != other.length()) {
			return false;
		}
		int i = 0;
		while (n-- != 0) {
			if (charAt(i) != other.charAt(i)) {
				return false;
			}
			i++;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashVal = this.hash;
		if (hashVal == 0 && length() > 0) {
			for (int i = this.start; i < this.end; i++) {
				hashVal = 31 * hashVal + this.source.charAt(i);
			}
			this.hash = hashVal;
		}
		return hashVal;
	}

	@Override
	public String toString() {
		return this.source.substring(this.start, this.end);
	}
}
