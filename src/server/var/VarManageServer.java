/**
 * ¾øÃÜ Created on 2010-7-2 by edmund
 */
package server.var;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.fleety.base.Util;
import com.fleety.base.xml.XmlParser;
import com.fleety.server.BasicServer;

public class VarManageServer extends BasicServer {
	private static VarManageServer singleInstance = null;

	public static VarManageServer getSingleInstance() {
		if (singleInstance == null) {
			synchronized (VarManageServer.class) {
				if (singleInstance == null) {
					singleInstance = new VarManageServer();
				}
			}
		}
		return singleInstance;
	}

	public VarManageServer() {
		this(null);
	}

	private VarManageServer parent = null;

	public VarManageServer(VarManageServer parent) {
		this.parent = parent;
	}

	private String varFlag = "$";

	public boolean startServer() {
		String tempStr = this.getStringPara("var_flag");
		if (tempStr != null && tempStr.trim().length() > 0) {
			this.varFlag = tempStr;
		}

		this.isRunning = true;
		this.loadCfg();
		return this.isRunning;
	}

	private synchronized boolean loadCfg() {
		String cfgPath = this.getStringPara("cfg_path");
		if (cfgPath == null || cfgPath.trim().length() == 0) {
			return true;
		}

		boolean isOk = true;
		String[] cfgPathArr = cfgPath.split(",");
		boolean isModified = false;
		for (int i = 0; i < cfgPathArr.length; i++) {
			isModified = this.isCfgFileModified(cfgPathArr[i]);
			if (isModified) {
				break;
			}
		}
		if (isModified) {
			Hashtable tempVarMapping = new Hashtable();
			for (int i = 0; i < cfgPathArr.length; i++) {
				isOk = isOk & this._loadCfg(cfgPathArr[i], tempVarMapping);
			}
			if (isOk) {
				Iterator itr = this.userVarMapping.keySet().iterator();
				Object key = null;
				while (itr.hasNext()) {
					key = itr.next();
					tempVarMapping.put(key, this.userVarMapping.get(key));
				}
				this.parseVarValue(tempVarMapping);
				this.varMapping = tempVarMapping;
			}
		}
		return isOk;
	}

	private boolean isCfgFileModified(String cfgPath) {
		File f = new File(cfgPath);
		if (!f.exists()) {
			return true;
		}
		long lastModifyTime = 0;
		Object obj = this.cfgLastModifyTimeMap.get(cfgPath);
		if (obj != null) {
			lastModifyTime = ((Long) obj).longValue();
		}
		if (f.lastModified() == lastModifyTime) {
			return false;
		} else {
			this.cfgLastModifyTimeMap.put(cfgPath, new Long(f.lastModified()));
			return true;
		}
	}

	private boolean _loadCfg(String cfgPath, Hashtable tempVarMapping) {
		File f = new File(cfgPath);
		if (!f.exists()) {
			return true;
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(cfgPath);
			Element root = XmlParser.parse(in);

			Node[] varNodeArr = Util.getElementsByTagName(root, "var");
			for (int i = 0; i < varNodeArr.length; i++) {
				this.loadOneVar(varNodeArr[i], tempVarMapping);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}

		return true;
	}

	public void loadOneVar(Node varNode, Hashtable tempVarMapping)
			throws Exception {
		String name = Util.getNodeAttr(varNode, "name");
		String systemProperty = Util.getNodeAttr(varNode, "system_property");
		Object value = Util.getNodeAttr(varNode, "value");
		if (name == null || name.trim().length() == 0) {
			return;
		}

		IValueCreator creator;
		Node cNode = Util.getSingleElementByTagName(varNode, "creator");
		String clsName = Util.getNodeAttr(cNode, "clsname");
		if (clsName != null) {
			creator = (IValueCreator) Class.forName(clsName).newInstance();
			Node[] nodeArr = Util.getElementsByTagName(cNode, "para");
			Node tempNode;
			for (int i = 0; i < nodeArr.length; i++) {
				tempNode = nodeArr[i];
				creator.addPara(Util.getNodeAttr(tempNode, "key"),
						Util.getNodeAttr(tempNode, "value"));
			}
			Object _value = creator.createValue();
			if (_value != null) {
				value = _value;
			}
		}

		if (value != null) {
			tempVarMapping.put(name, new VarValueInfo(systemProperty, value));
		}
	}

	private void parseVarValue(Hashtable tempVarMapping) {
		Object varName, varValue;
		VarValueInfo varInfo;
		for (Iterator itr = tempVarMapping.keySet().iterator(); itr.hasNext();) {
			varName = itr.next();

			varInfo = (VarValueInfo) tempVarMapping.get(varName);
			varValue = varInfo.getVarValue();
			varValue = this.updateValueByVar(varValue);

			tempVarMapping
					.put(varName, new VarValueInfo(varInfo.isSystemProperty()
							+ "", varValue));

			if (varInfo.isSystemProperty()) {
				System.setProperty(varName.toString(), varValue.toString());
			}
		}
	}

	public void stopServer() {
		this.isRunning = false;
		this.userVarMapping.clear();
		this.varMapping.clear();
	}

	private Hashtable varMapping = new Hashtable();
	private Hashtable userVarMapping = new Hashtable();
	private Hashtable cfgLastModifyTimeMap = new Hashtable();

	public VarManageServer setVar(Object varName, Object varValue) {
		if (!this.isRunning()) {
			return this;
		}
		if (varValue == null) {
			varValue = "";
		}
		this.userVarMapping.put(varName, new VarValueInfo(varValue));
		this.varMapping.put(varName, new VarValueInfo(varValue));
		return this;
	}

	public String getVarStringValue(Object varName) {
		if (!this.isRunning()) {
			return null;
		}
		this.loadCfg();
		VarValueInfo value = null;
		if (this.userVarMapping.containsKey(varName)) {
			value = (VarValueInfo) this.userVarMapping.get(varName);
			if (value != null) {
				return value.toString();
			}
		}

		value = (VarValueInfo) this.varMapping.get(varName);
		if (value == null) {
			if (this.parent != null) {
				return this.parent.getVarStringValue(varName);
			}
			return null;
		}
		return value.toString();
	}

	public Object getVarValue(Object varName) {
		if (!this.isRunning()) {
			return null;
		}
		this.loadCfg();
		VarValueInfo value = null;
		if (this.userVarMapping.containsKey(varName)) {
			value = (VarValueInfo) this.userVarMapping.get(varName);
			if (value != null) {
				return value.toString();
			}
		}
		value = (VarValueInfo) this.varMapping.get(varName);
		if (value == null) {
			if (this.parent != null) {
				return this.parent.getVarValue(varName);
			}
			return null;
		}
		return value.getVarValue();
	}

	public boolean existVar(Object varName) {
		if (!this.isRunning()) {
			return false;
		}

		boolean isExist = this.varMapping.containsKey(varName)
				|| this.userVarMapping.containsKey(varName);
		if (isExist) {
			return true;
		}
		if (this.parent != null) {
			return this.parent.existVar(varName);
		}
		return false;
	}

	public Object updateValueByVar(Object _value) {
		return this.updateValueByVar(_value, null);
	}

	public Object updateValueByVar(Object _value, IVarNameGetter getter) {
		if (!this.isRunning()) {
			return null;
		}
		if (!(_value instanceof String)) {
			return _value;
		}
		String value = (String) _value;
		if (_value == null) {
			return value;
		}

		int flagLen = this.varFlag.length();

		String varName, varValue;
		int index1, index2;
		index1 = value.indexOf(varFlag);
		String[] varArr;
		while (index1 >= 0) {
			index2 = value.indexOf(varFlag, index1 + flagLen);
			if (index2 > 0) {
				varName = value.substring(index1 + flagLen, index2);
				varValue = null;
				if (getter != null) {
					varArr = getter.getVarName(varName);
					for (int i = 0; i < varArr.length; i++) {
						if (this.varMapping.containsKey(varArr[i])) {
							varValue = this.getVarStringValue(varArr[i]);
							break;
						}
					}
				} else {
					varValue = this.getVarStringValue(varName);
				}

				if (varValue == null) {
					index1 = index1 + flagLen;
				} else {
					varValue = (String) this.updateValueByVar(varValue);
					value = value.substring(0, index1) + varValue
							+ value.substring(index2 + flagLen);
					index1 = index1 + varValue.length();
				}
			} else {
				break;
			}
			index1 = value.indexOf(varFlag, index1);
		}
		if (this.parent != null) {
			return this.parent.updateValueByVar(value);
		} else {
			return value;
		}
	}

	private class VarValueInfo {
		private boolean isSystemProperty = false;
		private Object varValue = null;

		public VarValueInfo(Object varValue) {
			this.varValue = varValue;
		}

		public VarValueInfo(String systemProperty, Object varValue) {
			this.varValue = varValue;
			if (systemProperty != null) {
				this.isSystemProperty = systemProperty.trim().equalsIgnoreCase(
						"true");
			}
		}

		public boolean isSystemProperty() {
			return this.isSystemProperty;
		}

		public Object getVarValue() {
			return this.varValue;
		}

		public String toString() {
			if (this.varValue == null) {
				return null;
			}
			return this.varValue.toString();
		}
	}
}
