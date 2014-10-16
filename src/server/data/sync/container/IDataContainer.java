package server.data.sync.container;

import java.io.Serializable;

public interface IDataContainer extends ISyncInfo{
	public boolean updateInfo(ISyncInfo info, int cLevel);
	
	public boolean removeInfo(ISyncInfo info, int cLevel);
	
	public ISyncInfo getInfo(Serializable[] flagArr,int cLevel);
	
	
	public int size();
	
	public Serializable[] keys();
	
	public ISyncInfo getInfo(Serializable key);
}
