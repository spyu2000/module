package com.fleety.base.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class XjsTable extends JTable {
	private static Font defaultHeaderFont = null;
	private static Color defaultHeaderBackground = null;
	private static Color defaultHeaderColor = null;
	private static Font defaultBodyFont = null;
	private static Color defaultBodyBackground = null;
	private static Color defaultBodyColor = null;
	private static Color defaultBodySelectionColor = null;
	static{
		defaultHeaderFont = new Font("Dialog",Font.BOLD,20);
		defaultHeaderBackground = new Color(200,221,242);
		defaultHeaderColor = Color.BLACK;
		defaultBodyFont = new Font("Dialog",Font.PLAIN,18);
		defaultBodyBackground = Color.white;
		defaultBodyColor = Color.black;
		defaultBodySelectionColor = Color.yellow;
	}
	
	private boolean isEditable = false;
	public XjsTable(String[] tableColumnText){
		DefaultTableModel tableModel = new DefaultTableModel(){
			public boolean isCellEditable(int row,int col){
				return isEditable;
			}
		};
		tableModel.setColumnIdentifiers(tableColumnText);
		this.setModel(tableModel);
		
		this.getTableHeader().setReorderingAllowed(false);
		this.getTableHeader().setFont(defaultHeaderFont);
		this.getTableHeader().setBackground(defaultHeaderBackground);
		this.getTableHeader().setForeground(defaultHeaderColor);

		this.setFont(defaultBodyFont);
		this.setBackground(defaultBodyBackground);
		this.setSelectionBackground(defaultBodySelectionColor);
		this.setForeground(defaultBodyColor);
		this.setRowHeight(30);
	}

	public void setEditable(boolean isEditable){
		this.isEditable = isEditable;
	}

	public void clearTable(){
		DefaultTableModel model = (DefaultTableModel)this.getModel();
		while(model.getRowCount() > 0){
			model.removeRow(0);
		}
	}
	
	public void addRow(String[] rowData){
		DefaultTableModel model = (DefaultTableModel)this.getModel();
		model.addRow(rowData);
	}
}
