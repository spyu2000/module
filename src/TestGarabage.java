import java.io.FileInputStream;

import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;
import com.fleety.base.Util;
import com.fleety.track.TrackFilter;
import com.fleety.track.TrackIO;
import com.fleety.util.pool.db.DbConnPool.DbHandle;

import server.db.DbServer;


public class TestGarabage {
	private static int count = 0;
	public static void main(String[] args) throws Exception{
		System.out.println(new String(Util.bcdStr2ByteArr("31333034313131303436303730373337383334300a443a5c5a697046696c65735c31333034313131303436303730373337383334302e7a6970")));
	}

}
