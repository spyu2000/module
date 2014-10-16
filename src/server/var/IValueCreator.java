/**
 * ¾øÃÜ Created on 2010-7-2 by edmund
 */
package server.var;

public interface IValueCreator{
	public void addPara(Object key,Object value);
	public Object getPara(Object key);
	
	public Object createValue();
}
