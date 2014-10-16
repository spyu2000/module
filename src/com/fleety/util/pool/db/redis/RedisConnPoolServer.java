package com.fleety.util.pool.db.redis;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.Util;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.db.redis.test.XjsTestBean;
import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;

/**
 * 包装Redis的连接，封装了Jedis、Transaction、Pipeline等对象
 * @author fleety
 *
 */
public class RedisConnPoolServer extends BasicServer{
	private JedisPool pool = null;
	private HashMap jedisMethodMapping = new HashMap();
	private HashMap transactionMethodMapping = new HashMap();
	private HashMap pipelineMethodMapping = new HashMap();
	
	private static RedisConnPoolServer singleInstance = null;
	public static RedisConnPoolServer getSingleInstance(){
		if(singleInstance == null){
			synchronized(RedisConnPoolServer.class){
				if(singleInstance == null){
					singleInstance = new RedisConnPoolServer();
				}
			}
		}
		return singleInstance;
	}
	
	private String subscribeThreadName = null;
	private int defaultDbIndex = 0;
	public boolean startServer(){
		JedisPoolConfig config = new JedisPoolConfig();
		String tempStr = null;
		
		tempStr = this.getStringPara("maxActive");
		if(tempStr != null && tempStr.trim().length() > 0){
			config.setMaxActive(Integer.parseInt(tempStr.trim()));
		}
		tempStr = this.getStringPara("maxIdle");
		if(tempStr != null && tempStr.trim().length() > 0){
			config.setMaxIdle(Integer.parseInt(tempStr.trim()));
		}
		tempStr = this.getStringPara("minIdle");
		if(tempStr != null && tempStr.trim().length() > 0){
			config.setMinIdle(Integer.parseInt(tempStr.trim()));
		}
		tempStr = this.getStringPara("maxWait");
		if(tempStr != null && tempStr.trim().length() > 0){
			config.setMaxWait(Long.parseLong(tempStr.trim()));
		}
		tempStr = this.getStringPara("testOnBorrow");
		if(tempStr != null && tempStr.trim().length() > 0){
			config.setTestOnBorrow(Boolean.parseBoolean(tempStr.trim()));
		}
		tempStr = this.getStringPara("testWhileIdle");
		if(tempStr != null && tempStr.trim().length() > 0){
			config.setTestWhileIdle(!tempStr.trim().equals("false"));
		}
		tempStr = this.getStringPara("default_db_index");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.defaultDbIndex = Integer.parseInt(tempStr.trim());
		}

		int timeout = 60000;
		if(this.getIntegerPara("timeout")!=null){
			timeout = this.getIntegerPara("timeout").intValue();
		}
		String passwd = this.getStringPara("passwd");
		if(passwd != null && passwd.length() > 0){
			this.pool = new JedisPool(config,this.getStringPara("ip"),this.getIntegerPara("port").intValue(),timeout,passwd);
		}else{
			this.pool = new JedisPool(config,this.getStringPara("ip"),this.getIntegerPara("port").intValue(),timeout);
		}
		this.subscribeThreadName = "subscribeThreadName_"+this.getServerName();
		
		
		HashMap pNameMapping = new HashMap();
		pNameMapping.put("int", "Integer");
		pNameMapping.put("short", "Short");
		pNameMapping.put("float", "Float");
		pNameMapping.put("double", "Double");
		pNameMapping.put("boolean", "Boolean");
		pNameMapping.put("long", "Long");
		pNameMapping.put("byte", "Byte");
		pNameMapping.put("char", "Character");
		this.initMethodMapping(Jedis.class, this.jedisMethodMapping, pNameMapping);
		this.initMethodMapping(Transaction.class, this.transactionMethodMapping, pNameMapping);
		this.initMethodMapping(Pipeline.class, this.pipelineMethodMapping, pNameMapping);
		
		this.isRunning = true;
		return this.isRunning();
	}
	
	private void initMethodMapping(Class cls,HashMap mapping,HashMap pNameMapping){
		String msName,pName,pName1;
		Class[] clsArr;
		Method[] ms = cls.getMethods();
		for(int i=0;i<ms.length;i++){
			msName = ms[i].getName();
			clsArr = ms[i].getParameterTypes();
			if(clsArr.length > 0){
				pName = clsArr[0].getSimpleName();
				pName1 = (String)pNameMapping.get(pName);
				if(pName1 != null){
					pName = pName1; 
				}
				msName += "."+pName;
			}
			mapping.put(msName, ms[i]);
		}
	}
	
	public JedisHandle getJedisConnection(){
		if(!this.isRunning()){
			return null;
		}
		if(!this.isRunning()){
			return null;
		}
		Jedis jedis = this.pool.getResource();
		if(jedis == null){
			return null;
		}
		return new JedisHandle(jedis);
	}

	private ArrayList observerList = new ArrayList(4);
	public void addListener(IRedisObserver observer){
		if(!this.isRunning()){
			return ;
		}
		if(observer == null){
			return ;
		}
		for(Iterator itr = observer.getPatternList().iterator();itr.hasNext();){
			if(itr.next() == null){
				itr.remove();
			}
		}
		if(observer.getPatternList().size() == 0){
			return ;
		}
		if(this.observerList.contains(observer)){
			return ;
		}
		this.observerList.add(observer);
		
		this.rebuildSubscribe();
	}
	public void removeListener(IRedisObserver observer){
		if(!this.isRunning()){
			return ;
		}
		if(observer == null){
			return ;
		}
		if(this.observerList.remove(observer)){
			this.rebuildSubscribe();
		}
	}
	private SubscribeTask curTask = null;
	private synchronized void rebuildSubscribe(){
		ThreadPool tPool = ThreadPoolGroupServer.getSingleInstance().getThreadPool(this.subscribeThreadName);
		if(tPool == null){
			PoolInfo pInfo = new PoolInfo();
			pInfo.isDaemo = true;
			pInfo.taskCapacity = 10;
			try {
				tPool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.subscribeThreadName, pInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		IRedisObserver observer;
		String pattern;
		List patternList;
		ArrayList tempList = new ArrayList();
		for(Iterator itr = this.observerList.iterator();itr.hasNext();){
			observer = (IRedisObserver)itr.next();
			patternList = observer.getPatternList();
			for(Iterator itr1 = patternList.iterator();itr1.hasNext();){
				pattern = (String)itr1.next();
				if(tempList.contains(pattern)){
					continue;
				}
				tempList.add(pattern);
			}
		}
		if(tempList.size() > 0){
			if(this.curTask == null){
				this.curTask = new SubscribeTask();
				tPool.addTask(curTask);
				synchronized(curTask){
					try{
						this.curTask.wait(5000);
					}catch(Exception e){}
				}
			}

			this.curTask.rebuildPattern(tempList);
		}
	}
	private synchronized void triggerMsg(String pattern,String msg,String content){
		IRedisObserver observer;
		for(Iterator itr = this.observerList.iterator();itr.hasNext();){
			observer = (IRedisObserver)itr.next();
			if(observer.isObserve(pattern)){
				observer.msgArrived(pattern, msg, content);
			}
		}
	}
	
	public void publish(String msg,String content){
		if(!this.isRunning()){
			return ;
		}
		JedisHandle conn = this.getJedisConnection();
		try{
			conn.call("publish", new String[]{msg,content});
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			this.releaseJedisConnection(conn);
		}
	}


	public Set<String> getAllIdsForTable(RedisTableBean record) throws Exception{
		return this.getAllIdsForTable(record, this.defaultDbIndex);
	}
	public Set<String> getAllIdsForTable(RedisTableBean record,int dbIndex) throws Exception{
		if(record == null){
			return null;
		}
		JedisHandle conn = this.getJedisConnection();
		try{
			conn.select(dbIndex);
			return (Set<String>)conn.call("hkeys", new Object[]{record.getTableName()});
		}catch(Exception e){
			throw e;
		}finally{
			this.releaseJedisConnection(conn);
		}
	}
	public void clearTableRecord(RedisTableBean record) throws Exception{
		this.clearTableRecord(record,this.defaultDbIndex);
	}
	public void clearTableRecord(RedisTableBean record,int dbIndex) throws Exception{
		if(record == null){
			return ;
		}
		JedisHandle conn = this.getJedisConnection();
		try{
			conn.select(dbIndex);
			conn.call("del", new Object[]{new String[]{record.getTableName()}});
		}catch(Exception e){
			throw e;
		}finally{
			this.releaseJedisConnection(conn);
		}
	}
	public void deleteTableRecord(RedisTableBean[] recordArr) throws Exception{
		this.deleteTableRecord(recordArr,this.defaultDbIndex);
	}
	public void deleteTableRecord(RedisTableBean[] recordArr,int dbIndex) throws Exception{
		if(recordArr == null || recordArr.length == 0){
			return ;
		}
		boolean hasData = false;
		JedisHandle conn = this.getJedisConnection();
		try{
			conn.select(dbIndex);
			RedisTableBean record;
			PipelineHandle pipe = conn.pipelined();
			for(int i=0;i<recordArr.length;i++){
				record = recordArr[i];
				if(record == null){
					continue;
				}
				hasData = true;
				pipe.call("hdel", new Object[]{record.getTableName(),record.getUid()});
			}
			if(hasData){
				List rList = pipe.syncAndReturnAll();
			}
		}catch(Exception e){
			throw e;
		}finally{
			this.releaseJedisConnection(conn);
		}
	}
	public void saveTableRecord(RedisTableBean[] recordArr) throws Exception{
		this.saveTableRecord(recordArr, this.defaultDbIndex);
	}
	public void saveTableRecord(RedisTableBean[] recordArr,int dbIndex) throws Exception{
		if(recordArr == null || recordArr.length == 0){
			return ;
		}
		boolean hasData = false;
		JedisHandle conn = this.getJedisConnection();
		try{
			conn.select(dbIndex);
			RedisTableBean record;
			PipelineHandle pipe = conn.pipelined();
			for(int i=0;i<recordArr.length;i++){
				record = recordArr[i];
				if(record == null){
					continue;
				}
				hasData = true;
				pipe.call("hset", new Object[]{record.getTableName(),record.getUid(),record.toJSONString()});
			}
			if(hasData){
				pipe.sync();
			}
		}catch(Exception e){
			throw e;
		}finally{
			this.releaseJedisConnection(conn);
		}
	}
	

	public List queryTableRecord(RedisTableBean[] recordArr) throws Exception{
		return this.queryTableRecord(recordArr,this.defaultDbIndex);
	}
	public List queryTableRecord(RedisTableBean[] recordArr,int dbIndex) throws Exception{
		if(recordArr == null || recordArr.length == 0){
			return null;
		}

		LinkedList tempList = new LinkedList();
		JedisHandle conn = this.getJedisConnection();
		try{
			conn.select(dbIndex);
			boolean isAll = false;
			boolean hasData = false;
			RedisTableBean record,tRecord = null;
			PipelineHandle pipe = conn.pipelined();
			for(int i=0;i<recordArr.length;i++){
				record = recordArr[i];
				if(record == null){
					continue;
				}
				hasData = true;
				tRecord = record;
				if(record.getUid() == null){
					isAll = true;
					pipe.call("hgetAll", new Object[]{record.getTableName()});
					break;
				}else{
					tempList.add(record);
					pipe.call("hget", new Object[]{record.getTableName(),record.getUid()});
				}
			}
			if(hasData){
				List<Object> rList = pipe.syncAndReturnAll();
				if(isAll){
					tempList.clear();
					Map<String,String> map = (Map<String,String>)rList.get(0);
					for(Iterator itr = map.values().iterator();itr.hasNext();){
						record = (RedisTableBean)tRecord.clone();
						record.parseJSONString((String)itr.next());
						tempList.add(record);
					}
				}else{
					if(rList.size() != tempList.size()){
						throw new Exception("Error Result Num!");
					}
					String str;
					for(Iterator itr = rList.iterator(),itr1 = tempList.iterator();itr.hasNext();){
						record = (RedisTableBean)itr1.next();
						str = (String)itr.next();
						if(str == null){
							itr1.remove();
						}else{
							record.parseJSONString(str);
						}
					}
				}
			}
		}catch(Exception e){
			throw e;
		}finally{
			this.releaseJedisConnection(conn);
		}
		return tempList;
	}
	
	public void releaseJedisConnection(JedisHandle jedisHandle){
		if(!this.isRunning()){
			return ;
		}
		if(jedisHandle == null || jedisHandle.jedis == null){
			return ;
		}

		Jedis jedis = jedisHandle.jedis;
		jedisHandle.jedis = null;
		if(jedisHandle.isError){
			this.pool.returnBrokenResource(jedis);
		}else{
			this.pool.returnResource(jedis);
		}
	}
	
	public void stopServer(){
		if(this.curTask != null){
			this.curTask.stop();
		}
		
		if(this.pool != null){
			this.pool.destroy();
		}
		this.pool = null;
		
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.subscribeThreadName);
		super.stopServer();
	}
	
	public class JedisHandle{
		private Jedis jedis = null;
		private Exception e = null;
		private boolean isError = false;
		public JedisHandle(Jedis jedis){
			this.jedis = jedis;
			this.e = new Exception();
		}
		
		public void select(int dbIndex){
			this.jedis.select(dbIndex);
		}
		public TransactionHandle multi(){
			return new TransactionHandle(this,this.jedis.multi());
		}
		public PipelineHandle pipelined(){
			return new PipelineHandle(this,this.jedis.pipelined());
		}
		
		public Object call(String methodName,Object[] args) throws Exception{
			if(methodName == null){
				throw new Exception("Fleety Not Such Method:"+methodName);
			}
			Class cls = null;
			if(args != null && args.length > 0){
				cls = args[0].getClass();
			}
			Method ms = null;
			String newMethodName = methodName;
			do{
				if(cls != null){
					newMethodName = methodName + "."+cls.getSimpleName();
					cls = cls.getSuperclass();
				}
				ms = (Method)jedisMethodMapping.get(newMethodName);
			}while(cls != null && ms == null);
			
			if(ms == null){
				throw new Exception("Fleety Not Such Method:"+methodName);
			}
			try{
				return ms.invoke(this.jedis, args);
			}catch(Exception e){
				this.isError = true;
				throw e;
			}
		}
		
		public void finalize() throws Throwable{
			super.finalize();
			if(this.jedis != null){
				System.out.println("GC Release RedisConn!"+Util.getStackStr(this.e));
				releaseJedisConnection(this);
			}
		}
	}
	
	public class TransactionHandle{
		private JedisHandle jedisHandle = null;
		private Transaction tx = null;
		public TransactionHandle(JedisHandle jedisHandle, Transaction tx){
			this.jedisHandle = jedisHandle;
			this.tx = tx;
		}

		public void exec(){
			this.tx.exec();
		}
		public List<Response<?>> execGetResponse(){
			return this.tx.execGetResponse();
		}
		
		public Object call(String methodName,Object[] args) throws Exception{
			if(methodName == null){
				throw new Exception("Fleety Not Such Method:"+methodName);
			}
			if(args != null && args.length > 0){
				methodName += "."+args[0].getClass().getSimpleName();
			}
			Method ms = (Method)transactionMethodMapping.get(methodName);
			if(ms == null){
				throw new Exception("Fleety Not Such Method");
			}
			try{
				return ms.invoke(this.tx, args);
			}catch(Exception e){
				jedisHandle.isError = true;
				throw e;
			}
		}
	}
	
	public class PipelineHandle{
		private JedisHandle jedisHandle = null;
		private Pipeline tx = null;
		public PipelineHandle(JedisHandle jedisHandle, Pipeline tx){
			this.jedisHandle = jedisHandle;
			this.tx = tx;
		}

		public void sync(){
			this.tx.sync();
		}
		public List<Object> syncAndReturnAll(){
			return this.tx.syncAndReturnAll();
		}
		public void discard(){
			this.tx.discard();
		}
		
		public Object call(String methodName,Object[] args) throws Exception{
			if(methodName == null){
				throw new Exception("Fleety Not Such Method:"+methodName);
			}
			if(args != null && args.length > 0){
				methodName += "."+args[0].getClass().getSimpleName();
			}
			Method ms = (Method)pipelineMethodMapping.get(methodName);
			if(ms == null){
				throw new Exception("Fleety Not Such Method");
			}
			try{
				return ms.invoke(this.tx, args);
			}catch(Exception e){
				jedisHandle.isError = true;
				throw e;
			}
		}
	}
	
	private String firstConnectSubscribeName = "fleety_nothing_nothing_nothing";
	private class SubscribeTask extends BasicTask{
		private List patternList = null;
		private JedisPubSub invoker = null;
		public SubscribeTask(){
			
		}
		
		public void rebuildPattern(List newPatternList){
			if(this.invoker == null){
				return;
			}
			String[] patternArr ;
			if(this.patternList == null){
				patternArr = new String[newPatternList.size()];
				newPatternList.toArray(patternArr);
				this.invoker.psubscribe(patternArr);
			}else{
				List tempList = new ArrayList(newPatternList);
				for(Iterator itr = tempList.iterator();itr.hasNext();){
					if(this.patternList.remove(itr.next())){
						itr.remove();
					}
				}
				
				if(this.patternList.size() > 0){
					patternArr = new String[this.patternList.size()];
					this.patternList.toArray(patternArr);
					this.invoker.punsubscribe(patternArr);
				}
				if(tempList.size() > 0){
					patternArr = new String[tempList.size()];
					tempList.toArray(patternArr);
					this.invoker.psubscribe(patternArr);
				}
			}

			this.patternList = newPatternList;
		}
		
		private boolean isStop = false;
		public boolean execute(){
			while(!this.isStop){
				JedisHandle jedis = RedisConnPoolServer.this.getJedisConnection();
				if(jedis == null){
					try{
						Thread.sleep(5000);
						continue;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				this.invoker = new JedisPubSub(){
					public void onMessage(String arg0, String arg1) {
						
					}
					public void onPMessage(String pattern, String msg, String content) {
						RedisConnPoolServer.this.triggerMsg(pattern, msg, content);
					}
					public void onPSubscribe(String arg0, int arg1) {
						if(arg0.equals(firstConnectSubscribeName)){
							synchronized(SubscribeTask.this){
								SubscribeTask.this.notifyAll();
							}
						}
						System.out.println("PSubscribe:"+arg0+" "+arg1);
					}
					public void onPUnsubscribe(String arg0, int arg1) {
						System.out.println("PUnsubscribe:"+arg0+" "+arg1);
					}
					public void onSubscribe(String arg0, int arg1) {
						System.out.println("Subscribe:"+arg0+" "+arg1);
					}
					public void onUnsubscribe(String arg0, int arg1) {
						System.out.println("Unsubscribe:"+arg0+" "+arg1);
					}
				};
				try{
					jedis.call("psubscribe", new Object[]{invoker,new String[]{firstConnectSubscribeName}});
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					RedisConnPoolServer.this.releaseJedisConnection(jedis);
				}
			}
			return true;
		}

		public void stop(){
			this.isStop = true;
			if(this.invoker != null){
				this.invoker.punsubscribe();
			}
		}
	}

	public static void main(String[] argv) throws Exception{
		RedisConnPoolServer server = RedisConnPoolServer.getSingleInstance();
		server.addPara("ip", "192.168.0.218");
		server.addPara("port", "6379");
		server.startServer();
		
		ArrayList list = new ArrayList();
		list.add("111111111");
		server.addListener(new BasicRedisObserver(list){
			public void msgArrived(String pattern,String msg,String content){
				System.out.println(pattern+"  "+msg+" "+content);
			}
		});
		
		
		System.out.println("endend");
		Thread.sleep(10000);
//		long t = System.currentTimeMillis();
//		int testNum = 10000;
//		XjsTestBean bean = null;
//		RedisTableBean[] arr = new RedisTableBean[testNum];
//		for(int i=0;i<testNum;i++){
//			arr[i] = bean = new XjsTestBean();
//			bean.setUid(""+i);
//			bean.setName("徐新"+i);
//			bean.setMale(true);
//		}
//		
//		server.deleteTableRecord(arr);
//		System.out.println("delete="+(System.currentTimeMillis()-t));
//		t = System.currentTimeMillis();
		
//		server.saveTableRecord(arr);
//		System.out.println("save="+(System.currentTimeMillis()-t));
//		t = System.currentTimeMillis();
		
//		bean.setUid(null);
//		List tempList = server.queryTableRecord(new RedisTableBean[]{bean});
//		System.out.println("read="+(System.currentTimeMillis()-t));
//		t = System.currentTimeMillis();
//		System.out.println(tempList.size());
//		int count = 0;
//		for(Iterator itr = tempList.iterator();itr.hasNext();){
//			if(count ++ >= 100){
//				break;
//			}
//			System.out.println(itr.next());
//		}
//		System.out.println("print="+(System.currentTimeMillis()-t));
	}
}
