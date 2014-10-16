/**
 * ¾øÃÜ Created on 2008-5-15 by edmund
 */
package server.socket.serversocket;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import com.fleety.util.pool.thread.BasicTask;

public class AcceptSelectorThread extends BasicTask{
	private FleetySocketServer fleetySocketServer = null;
	private Selector selector = null;
	private boolean isStop = false;
	
	public AcceptSelectorThread(Selector selector,FleetySocketServer fleetySocketServer){
		this.selector = selector;
		this.fleetySocketServer = fleetySocketServer;
	}
	
	public boolean execute() throws Exception{
		this.run();
		
		return true;
	}
	
	public void run(){
		Iterator keyIterator = null;
		SelectionKey sKey = null;
		SocketChannel channel = null;
		while(!isStop){
			try{
				int readyNum = this.selector.select();
				if(readyNum > 0){
					keyIterator = this.selector.selectedKeys().iterator();
					while(keyIterator.hasNext()){
						sKey = (SelectionKey)keyIterator.next();
						
						if(sKey.isAcceptable()){
							channel = ((ServerSocketChannel)sKey.channel()).accept();
							if(channel != null){
								this.fleetySocketServer.registerSocketChannel(channel);
							}
						}

						keyIterator.remove();
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				this.stopWork();
			}
		}
	}
	
	public void stopWork(){
		this.isStop = true;
	}
}
