package server.data.sync.container;

import java.io.Serializable;
import java.util.HashMap;

public class BasicDataContainer implements IDataContainer {
	private HashMap<Serializable,ISyncInfo> mapping = new HashMap<Serializable,ISyncInfo>();
	protected String[] flagArr = null;
	
	public BasicDataContainer(){
		
	}
	public BasicDataContainer(String[] flagArr){
		this.flagArr = flagArr;
	}
	
	private IDataContainer getDataContainer(Serializable key){
		IDataContainer container = (IDataContainer)this.mapping.get(key);
		return container;
	}
	
	public ISyncInfo getInfo(Serializable[] flagArr,int cLevel){
		if(cLevel < -1){
			return null;
		}
		if(flagArr == null){
			return null;
		}
		int nextLevel = cLevel + 1;
		if(flagArr.length <= nextLevel){
			return null;
		}
		Serializable nextFlag = flagArr[nextLevel];
		if(flagArr.length == cLevel+2){
			return (ISyncInfo)this.mapping.get(nextFlag);
		}else{
			IDataContainer container = this.getDataContainer(nextFlag);
			if(container != null){
				return container.getInfo(flagArr, nextLevel);
			}else{
				return null;
			}
		}
	}
	
	public boolean removeInfo(ISyncInfo info, int cLevel) {
		if(info == null || cLevel < -1){
			return false;
		}
		Serializable[] flagArr = info.getFlagArr();
		if(flagArr == null){
			return false;
		}
		int nextLevel = cLevel + 1;
		if(flagArr.length <= nextLevel){
			return false;
		}

		Serializable nextFlag = flagArr[nextLevel];
		if(flagArr.length == cLevel+2){
			this.mapping.remove(nextFlag);
			return true;
		}else{
			IDataContainer container = this.getDataContainer(nextFlag);
			if(container != null){
				return container.removeInfo(info, nextLevel);
			}else{
				return true;
			}
		}
	}

	public boolean updateInfo(ISyncInfo info, int cLevel) {
		if(info == null || cLevel < -1){
			return false;
		}
		Serializable[] flagArr = info.getFlagArr();
		if(flagArr == null){
			return false;
		}
		int nextLevel = cLevel + 1;
		if(flagArr.length <= nextLevel){
			return false;
		}

		Serializable nextFlag = flagArr[nextLevel];
		if(flagArr.length == cLevel+2){
			this.mapping.put(nextFlag,info);
			return true;
		}else{
			IDataContainer container = this.getDataContainer(nextFlag);
			if(container == null){
				int keyLen = nextLevel + 1;
				String[] arr = new String[keyLen];
				System.arraycopy(flagArr, 0, arr, 0, keyLen);
				container = new BasicDataContainer(arr);
				this.mapping.put(nextFlag, container);
			}
			return container.updateInfo(info, nextLevel);
		}
	}
	public ISyncInfo getInfo(Serializable key){
		return (ISyncInfo)this.mapping.get(key);
	}
	
	public int size(){
		return this.mapping.size();
	}
	
	public Serializable[] keys(){
		Serializable[] keys = new Serializable[this.size()];
		this.mapping.keySet().toArray(keys);
		return keys;
	}
	
	public Serializable[] getFlagArr(){
		return this.flagArr;
	}
	
	public String toString(){
		StringBuffer buff = new StringBuffer(128);
		for(int i=0;i<flagArr.length;i++){
			buff.append(this.flagArr[i]);
			buff.append("->");
		}
		buff.append(this.getClass().getName()+"["+this.hashCode()+"]");
		buff.append(" size="+this.mapping.size());
		return buff.toString();
	}
}
