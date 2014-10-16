package com.fleety.base;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FleetyDateFormat extends SimpleDateFormat {
	public FleetyDateFormat(String pattern){
		super(pattern);
	}
	public synchronized Date parse(String source, ParsePosition pos){
		return super.parse(source, pos);
	}
	public synchronized StringBuffer format(Date date, StringBuffer toAppendTo,
            FieldPosition pos){
		return super.format(date, toAppendTo, pos);
	}
}
