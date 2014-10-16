package server.data.sync;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class FleetyByteArrayOutputStream extends ByteArrayOutputStream{
	public FleetyByteArrayOutputStream(int size){
		super(size);
	}
	public ByteBuffer toByteBuffer(){
		return ByteBuffer.wrap(this.buf, 0, this.count);
	}
}
