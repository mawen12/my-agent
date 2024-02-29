package zipkin2.reporter;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public interface TracerConverter {

	List<byte[]> converter(List<byte[]> nextMessage);
}
