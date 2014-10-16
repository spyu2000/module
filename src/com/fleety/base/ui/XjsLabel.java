package com.fleety.base.ui;

import java.awt.Font;

import javax.swing.JLabel;

public class XjsLabel extends JLabel {
	public XjsLabel(String text){
		super(text);
		this.setFont(new Font("Dialog",Font.PLAIN,18));
		this.setHorizontalAlignment(JLabel.RIGHT);
	}
}
