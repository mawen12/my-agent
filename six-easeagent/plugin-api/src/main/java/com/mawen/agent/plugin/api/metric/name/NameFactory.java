package com.mawen.agent.plugin.api.metric.name;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface NameFactory {

	class Tuple<X, Y> {
		private X x;
		private Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}

		public X getX() {
			return x;
		}

		public void setX(X x) {
			this.x = x;
		}

		public Y getY() {
			return y;
		}

		public void setY(Y y) {
			this.y = y;
		}
	}

	class Builder {
		private final List<Tuple>
	}
}
