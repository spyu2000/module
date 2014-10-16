package server.flow.task.sql;

import java.sql.ResultSet;
import java.sql.Types;

import com.fleety.base.InfoContainer;

import server.flow.IFlow;

public class SqlParaInfo implements IFlow{
	public static final int TYPE_BOOLEAN = Types.BOOLEAN;
	public static final int TYPE_INT = Types.INTEGER;
	public static final int TYPE_DOUBLE = Types.DOUBLE;
	public static final int TYPE_NUMERIC = Types.NUMERIC;
	public static final int TYPE_FLOAT = Types.FLOAT;
	public static final int TYPE_TIMESTAMP = Types.TIMESTAMP;
	public static final int TYPE_DATE = Types.DATE;
	public static final int TYPE_STRING_1 = Types.VARCHAR;
	public static final int TYPE_STRING_2 = Types.CHAR;
	private int type = -1;
	private Object value = null;
	
	public SqlParaInfo(int type,Object value){
		this.type = type;
		this.value = value;
	}
	
	public Object getValue(){
		return this.value;
	}
	
	public boolean isInt(){
		return this.type == TYPE_INT;
	}
	public boolean isTimestamp(){
		return this.type == TYPE_TIMESTAMP || this.type == TYPE_DATE;
	}
	public boolean isString(){
		return this.type == TYPE_STRING_1 || this.type == TYPE_STRING_2;
	}
	public boolean isBoolean(){
		return this.type == TYPE_BOOLEAN;
	}
	public boolean isDouble(){
		return this.type == TYPE_DOUBLE || this.type == TYPE_FLOAT || this.type == TYPE_NUMERIC;
	}
	
	public static boolean isInt(int type){
		return type == TYPE_INT;
	}
	public static boolean isTimestamp(int type){
		return type == TYPE_TIMESTAMP || type == TYPE_DATE;
	}
	public static boolean isString(int type){
		return type == TYPE_STRING_1 || type == TYPE_STRING_2;
	}
	public static boolean isBoolean(int type){
		return type == TYPE_BOOLEAN;
	}
	public static boolean isDouble(int type){
		return type == TYPE_DOUBLE || type == TYPE_FLOAT || type == TYPE_NUMERIC;
	}
	
	public static int[] getDataType(ResultSet sets) throws Exception{
		int[] typeArr = new int[sets.getMetaData().getColumnCount()];
		
		for(int i=0;i<typeArr.length;i++){
			typeArr[i] = sets.getMetaData().getColumnType(i+1);
			
			if(typeArr[i] == TYPE_NUMERIC){
				if(sets.getMetaData().getScale(i+1) == 0){
					typeArr[i] = TYPE_INT;
				}
			}
		}
		
		return typeArr;
	}
	
	public static void initInfo(ResultSet sets,InfoContainer info,int[] fieldTypeArr) throws Exception{
		Object value;
		for(int i=0;i<fieldTypeArr.length;i++){
			if(SqlParaInfo.isInt(fieldTypeArr[i])){
				value = new Integer(sets.getInt(i+1));
			}else if(SqlParaInfo.isBoolean(fieldTypeArr[i])){
				value = new Boolean(sets.getBoolean(i+1));
			}else if(SqlParaInfo.isDouble(fieldTypeArr[i])){
				value = new Double(sets.getDouble(i+1));
			}else if(SqlParaInfo.isTimestamp(fieldTypeArr[i])){
				value = sets.getTimestamp(i+1);
			}else{
				value = sets.getString(i+1);
			}
			
			info.setInfo(sets.getMetaData().getColumnName(i+1), value);
		}
	}
}
