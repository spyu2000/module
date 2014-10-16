import java.util.HashMap;


public class ExportDataAndRules {

	private String[][] data;
	private HashMap ruleMap = new HashMap();

	public void setData(String[][] data){
		this.data = data;
	}
	
	public String[][] getData(){
		return this.data;
	}
	
	public void setCellSpan(int row,int col,int colspan,int rowspan){
		RuleCell cell = (RuleCell) ruleMap.get(row+"-"+col);
		if(cell == null){
			cell = new RuleCell();
		}
		cell.setColspan(colspan);
		cell.setRowspan(rowspan);
		ruleMap.put(row+"-"+col, cell);
	}	
	
	public HashMap getRuleMap(){
		return ruleMap;
	}
}
