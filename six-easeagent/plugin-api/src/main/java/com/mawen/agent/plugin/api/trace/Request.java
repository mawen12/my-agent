package com.mawen.agent.plugin.api.trace;

/**
 * Interface Request type used for parsing and sampling.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface Request extends Setter, Getter{

	/**
	 * The remote {@link Span.Kind} describing the direction and type of the request
	 */
	Span.Kind kind();

	/**
	 * The header get of the request for Span and Agent Context.
	 * <pre>{@code
	 *  String traceId = request.header("X-B3-TraceId");
	 *  String spanId = request.header("X-B3-SpanId");
	 *  String parentSpanId = request.header("X-B3-ParentSpanId");
	 *  String rootSource = request.header("root-source");
	 *  ......
	 * }</pre>
	 * <p>
	 * It is usually called on the server side when collaboration between multiple processed is required.
	 * {@code client -> <spanId,root-source...>server}
	 * </p>
	 * The class that implements this method needs to provide the name: value passed by the previous process.
	 * It can be passed by using http or tcp.
	 *
	 * <pre>{@code
	 *  class IRequest implements Request {
	 *      HttpRequest httpRequest;
	 *      String header(String name){
	 *          return httpRequest.getHeaders(name);
	 *      }
	 *  }
	 * }</pre>
	 *
	 * @param name header name
	 * @return header value
	 *
	 * @see Context#serverReceive(Request)
	 */
	String header(String name);

	/**
	 * The remote name describing the direction of the request.
	 * {@code span.name(request.name())}
	 */
	String name();

	/**
	 * When true, cache the scope in span.
	 * {@code span.cacheScope();}
	 * @return boolean
	 * @see {@link Span#cacheScope()}
	 */
	boolean cacheScope();

	/**
	 * The header set of the span and Agent for request. It is
	 * usually called on the client when collaboration between
	 * multiple processes is required. {@code client<spanId,root-source...> --> server}
	 * <p>
	 * The class that implements this method needs to pass the name: value
	 * of the method to the next process, It can be passed by using http or tcp.
	 *
	 * <p>
	 * <pre>{@code
	 *  request.setHeader("X-B3-TraceId", span.traceIdString());
	 *  request.setHeader("X-B3-SpanId", span.spanIdString());
	 *  request.setHeader("root-source", context.get("root-source"));
	 *  ......
	 * }</pre>
	 * <p>
	 * <pre>{@code
	 *  class IRequest implements Request {
	 *      HttpRequest httpRequest;
	 *      void setHeader(String name, String value){
	 *          httpRequest.setHeader(name, value);
	 *      }
	 *  }
	 * }</pre>
	 *
	 * @param name header name
	 * @param value header value
	 * @see Context#clientRequest(Request)
	 */
	void setHeader(String name, String value);
}
