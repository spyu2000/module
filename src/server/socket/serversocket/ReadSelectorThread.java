/**
 * 绝密 Created on 2008-5-15 by edmund
 */
package server.socket.serversocket;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

public class ReadSelectorThread extends BasicTask{
	private FleetySocketServer fleetySocketServer = null;
	private Selector selector = null;
	private boolean isStop = false;
	
	public ReadSelectorThread(Selector selector,FleetySocketServer fleetySocketServer){
		this.selector = selector;
		this.fleetySocketServer = fleetySocketServer;
	}
	
	private List channelPendingList = new LinkedList();
	public void addSocketChannel(RegistInfo connInfo){
		synchronized(channelPendingList){
			channelPendingList.add(connInfo);
		}
		selector.wakeup();
	}
	
	private String poolName = null;
	public synchronized void start() throws Exception{
		if(this.poolName != null){
			return ;
		}
		
		this.poolName = this.fleetySocketServer.getServerName()+"["+this.fleetySocketServer.hashCode()+"]Socket Accept";
		PoolInfo pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,1,1,false);
		ThreadPool pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(poolName, pInfo);
		pool.addTask(this);
	}
	
	public boolean execute() throws Exception{
		this.run();
		
		return true;
	}
	
	public void run(){
		Iterator keyIterator = null;
		SelectionKey sKey = null;
		ConnectSocketInfo connInfo = null;
		while(!isStop){
			try{
				synchronized(channelPendingList){
					if(channelPendingList.size() > 0){
						RegistInfo rInfo;
						for(Iterator itr = channelPendingList.iterator();itr.hasNext();){
							rInfo = (RegistInfo)itr.next();
							itr.remove();
							fleetySocketServer._registerSocketChannel(rInfo,this.selector);
						}
					}
				}
				int readyNum = this.selector.select(10000);
				if(readyNum > 0){
					keyIterator = this.selector.selectedKeys().iterator();
					while(keyIterator.hasNext()){
						sKey = (SelectionKey)keyIterator.next();

						connInfo = null;
						try{
							if(sKey.isReadable()){
								connInfo = (ConnectSocketInfo)sKey.attachment();
								this.fleetySocketServer.readCmd(connInfo);
							}
						}catch(ClosedChannelException e1){
							e1.printStackTrace();
							this.fleetySocketServer.destroySocketChannel(connInfo,CmdInfo.CLOSE_OK_CODE);
						}catch(IOException e3){
							e3.printStackTrace();
							this.fleetySocketServer.destroySocketChannel(connInfo,CmdInfo.CLOSE_IO_ERROR_CODE);
						}catch(Exception e2){
							e2.printStackTrace();
							this.fleetySocketServer.destroySocketChannel(connInfo,CmdInfo.CLOSE_OTHER_CODE);
						}catch(Error er){
							//错误的客户端连接或者恶意攻击，可能导致协议不正确而开启很大的空间而导致内存错误。只需关闭该Socket即可。
							er.printStackTrace();
							System.out.println("Info should't print,if printed,please contact developer!Thanks!");
							this.fleetySocketServer.destroySocketChannel(connInfo,CmdInfo.CLOSE_IO_ERROR_CODE);
						}
						
						keyIterator.remove();
					}
				}
			}catch(CancelledKeyException ce){
				ce.printStackTrace();
			}catch(NullPointerException ne){
				ne.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void stopWork(){
		this.isStop = true;
		if(this.poolName != null){
			ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
		}
		try{
			Iterator iterator = this.selector.keys().iterator();
			SelectionKey key;
			while(iterator.hasNext()){
				key = (SelectionKey)iterator.next();
				try{
					key.channel().close();
				}catch(Exception e){}
			}
		}catch(Exception e){}
		try{
			if(this.selector != null){
				this.selector.close();
			}
		}catch(Exception e){}
	}
	
	public class RegistInfo{
		public ConnectSocketInfo connInfo = null;
		public SocketChannel channel = null;
		public RegistInfo(SocketChannel channel,ConnectSocketInfo connInfo){
			this.channel = channel;
			this.connInfo = connInfo;
		}
	}
}
