package server.net.transfer;

public interface IDataTask {
	public boolean isAlive(long limitTime);
	public void stop();
}
