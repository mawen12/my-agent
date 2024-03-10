package com.mawen.agent.mock.log4j2;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class JarUrlsSupplier implements UrlSupplier{
	private final UrlSupplier[] suppliers;

	public JarUrlsSupplier(UrlSupplier[] suppliers) {
		this.suppliers = suppliers;
	}

	@Override
	public URL[] get() {
		var list = new ArrayList<>();
		for (var supplier : suppliers) {
			var urls = supplier.get();
			if (urls != null && urls.length > 0) {
				list.addAll(Arrays.asList(urls));
			}
		}
		var result = new URL[list.size()];
		list.toArray(result);
		return result;
	}
}
