package server.socket.socket;


public class Test {
	public static void main(String[] args) throws Exception{
		PoolSelectorCreator creator = new PoolSelectorCreator();
		creator.addPara("selector_num", "5");
		creator.startServer();
		
		FleetySocket fleetySocket = new FleetySocket();
		fleetySocket.addPara("ip", "192.168.0.72");
		fleetySocket.addPara("port", "1235");
		fleetySocket.addPara("ip_bak", "192.168.0.72");
		fleetySocket.addPara("port_bak", "1236");
		fleetySocket.addPara("reader", "server.socket.help.PrintCmdReader");
		fleetySocket.addPara("releaser", "server.socket.help.DefaultCmdReleaser");
		fleetySocket.addPara(FleetySocket.SELECTOR_FLAG, creator.getSelector());
		fleetySocket.startServer();
	
		while(true){
			try{
				fleetySocket.sendData(new byte[]{1,2,3,4,5,6,7,8,9}, 0, 9);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			Thread.sleep(5000);
		}
	}

}
