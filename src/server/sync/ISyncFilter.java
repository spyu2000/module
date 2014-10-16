package server.sync;

import com.fleety.base.InfoContainer;

public interface ISyncFilter {
	public boolean isSync(String mvLogName,InfoContainer record);
}
