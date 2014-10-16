package server.socket.inter;

import java.nio.ByteBuffer;

public interface IDataFilter {
	public ByteBuffer filter(ByteBuffer data);
}
