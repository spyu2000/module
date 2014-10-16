package com.fleety.base.ui;

import java.awt.Cursor;

import javax.swing.JButton;

public class XjsButton extends JButton {
	public XjsButton(String text){
		super(text);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
}
