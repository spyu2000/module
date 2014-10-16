package server.flow;

public class FlowOperator implements IFlow{
	private int id = 0;
	private int depId = 0;
	private String code = null;
	private String name = null;

	public FlowOperator(){
		
	}
	public FlowOperator(int id,String code,String name){
		this.id = id;
		this.code = code;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getDepId() {
		return depId;
	}
	public void setDepId(int depId) {
		this.depId = depId;
	}
	
}
