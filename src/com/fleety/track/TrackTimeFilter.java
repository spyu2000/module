/**
 * 绝密 Created on 2008-4-25 by edmund
 */
package com.fleety.track;

import java.util.Date;
import com.fleety.base.InfoContainer;

public class TrackTimeFilter implements TrackFilter{
	private Date startDate = null,endDate = null;
	//用于计数已经发现在最后时间之后的记录数。
	private int count = 0;
	//发现多少个在最后时间之后的记录数后返回
	private int afterCount = 3;
	protected Object optionalInfo = null;
	private boolean isBreak = false;
	
	public TrackTimeFilter(Date startDate,Date endDate){
		this.startDate = startDate;
		this.endDate = endDate;
	}
	public TrackTimeFilter(Date startDate,Date endDate,int afterCount){
		this.startDate = startDate;
		this.endDate = endDate;
		this.afterCount = afterCount;
	}
	
	public void setOptional(Object optionalInfo){
		this.optionalInfo = optionalInfo;
	}
	
	public Object getOptional(){
		return this.optionalInfo;
	}
	
	public int filterTrack(InfoContainer info){
		Date recordDate = info.getDate(TrackIO.DEST_TIME_FLAG);
		
		if(startDate != null){
			if(recordDate.before(startDate)){
				return IGNORE_FLAG;
			}
		}
		
		if(endDate != null){
			if(recordDate.after(endDate)){
				if(this.count >= this.afterCount){
					this.isBreak = true;
					return BREAK_FLAG;
				}else{
					this.count ++;
					return IGNORE_FLAG;
				}
			}
		}
		
		return CONTINUE_FLAG;
	}
	public boolean isBreak() {
		return isBreak;
	}
	public void setBreak(boolean isBreak) {
		this.isBreak = isBreak;
	}
}
