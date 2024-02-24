package com.mawen.agent.plugin.api.trace;

/**
 * Used to send the trace context downstream. For example, as http headers.
 *
 * <p>For example, to put the context on an {@link java.net.HttpURLConnection}, you can do this:
 * <pre>{@code
 *  // in your constructor
 *  producerInjector = tracing.messagingTracing().producerInjector();
 *
 *  // later in your code, reuse the function you created above to add trace headers
 *  HttpURLConnection connection = (HttpURLConnection)new URL("http://myserver").openConnection();
 *  producerInjector.consumerInject(span, new MessagingRequest(){
 *      public void setHeader(k, v){
 *          connection.setRequestProperty(k, v);
 *      }
 *  });
 * }</pre>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface Injector<R extends MessagingRequest> {

	/**
	 * Usually calls a {@link Request#setHeader(String, String)} for each propagation field to send downstream.
	 *
	 * @param span possibly unsampled.
	 * @param request holds propagation fields. For example, an outgoing message or http request.
	 */
	void inject(Span span, R request);
}
