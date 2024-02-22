package com.mawen.agent.plugin.api;

import java.io.Closeable;

/**
 * A Cleaner for Context
 * It must be called after your business.
 * <p>
 * example 1:
 * <pre>{@code
 * 		Cleaner cleaner = context.importAsync(snapshot);
 * 		try {
 * 		    // do business
 *      } finally {
 * 			cleaner.close();
 *      }
 * }</pre>
 * </p>
 * <p>
 * example 2:
 * <pre>{@code
 * 		void before(...){
 * 		 	Cleaner cleaner = context.importForwardedHeaders(getter);
 *        }
 * 		void after(...){
 * 		 	try {
 * 		 	   // do business
 *            } finally {
 * 		 	  cleaner.close();
 *            }
 *        }
 * }</pre>
 * </p>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface Cleaner extends Closeable {

	/**
	 * No exceptions are thrown when unbinding a Context.
	 * It must be called after your business.
	 *
	 * <pre>{@code
	 * 	try {
	 * 		......
	 *  } finally {
	 * 		cleaner.close();
	 *  }
	 * }</pre>
	 */
	@Override
	void close();
}
