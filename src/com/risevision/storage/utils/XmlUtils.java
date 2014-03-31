package com.risevision.storage.utils;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtils {
	//find the first occurrence of the nodeName in the list of immediate children
	public static Node findFirstNode(Node parentNode, String nodeName) {
		NodeList nodeList = parentNode.getChildNodes();
		if ((nodeList != null) && (nodeName != null))
			for (int i = 0; i < nodeList.getLength(); i++)
				if (nodeName.equals(nodeList.item(i).getNodeName()))
					return nodeList.item(i);
		return null;
	}
	
	//Optimised version of the getNode(Element myElement, String attribute) implementation
	public static String getNodeValue(Node parentNode, String nodeName) {
			return getNodeValue(parentNode, nodeName, "");
	}

	//Optimised version of the getNode(Element myElement, String attribute, int location) implementation
	public static String getNodeValue(Node parentNode, String nodeName, String defaultValue) {
		Node node = findFirstNode(parentNode, nodeName);
		if (node == null)
			return null;
		else
			return node.getTextContent();
	}

	public static String getNode(Element myElement, String attribute) {
		return getNode(myElement, attribute, 0);
	}

	public static String getNode(Element myElement, String attribute, int location) {
		return getNode(myElement, attribute, location, null);
	}
	
	public static String getNode(Element myElement, String attribute, String defaultValue) {
		return getNode(myElement, attribute, 0, defaultValue);
	}
	
	public static String getNode(Element myElement, String attribute, int location, String defaultValue) {
		String s = null;

		NodeList nodeList = myElement.getElementsByTagName(attribute);
		if (nodeList.item(location) != null
				&& nodeList.item(location).getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) nodeList.item(location);
			NodeList nodeList2 = element.getChildNodes();
			if (nodeList2.item(location) != null
					&& nodeList2.item(location).getNodeType() == Node.TEXT_NODE)
				s = ((Node) nodeList2.item(location)).getNodeValue();
		}
		return s;
	}
	
	public static ArrayList<String> getNodeListElements(NodeList nodeList) {
		ArrayList<String> resultList = new ArrayList<String>();
		if (nodeList != null) {
			for (int s = 0; s < nodeList.getLength(); s++) {
				Node node = nodeList.item(s);
	
				if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {
					String element = node.getFirstChild().getNodeValue();
	
					resultList.add(element);
				}
			}
		}
		
		return resultList;
	}
}
