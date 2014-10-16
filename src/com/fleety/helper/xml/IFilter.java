package com.fleety.helper.xml;

import org.w3c.dom.Node;

import com.fleety.base.xml.XmlNode;

public interface IFilter {
	public boolean isAccept(Node node);
	public boolean isAccept(XmlNode node);
}
