package com.mawen.agent.report.encoder;

import java.util.ArrayList;
import java.util.List;

import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Packer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface PackedMessage {

	List<EncodedData> getMessages();

	int packSize();

	int calculateAppendSize(int size);

	void addMessage(EncodedData msg);

	class DefaultPackedMessage implements PackedMessage {
		ArrayList<EncodedData> items;
		int packSize;
		Packer packer;

		public DefaultPackedMessage(int count, Packer packer) {
			this.items = new ArrayList<>(count);
			this.packSize = 0;
			this.packer = packer;
		}

		@Override
		public List<EncodedData> getMessages() {
			return this.items;
		}

		@Override
		public int packSize() {
			return packSize;
		}

		@Override
		public int calculateAppendSize(int size) {
			return this.packSize + this.packer.appendSizeInBytes(size);
		}

		@Override
		public void addMessage(EncodedData msg) {
			this.items.add(msg);
			if (packSize == 0) {
				this.packSize = this.packer.messageSizeInBytes(items);
			} else {
				this.packSize += this.packer.appendSizeInBytes(msg.size());
			}
		}
	}
}
