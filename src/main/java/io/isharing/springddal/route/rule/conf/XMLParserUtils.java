/*
* Copyright (C) 2017 ChenFei, All Rights Reserved
*
* This program is free software; you can redistribute it and/or modify it 
* under the terms of the GNU General Public License as published by the Free 
* Software Foundation; either version 3 of the License, or (at your option) 
* any later version.
*
* This program is distributed in the hope that it will be useful, but 
* WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
* or FITNESS FOR A PARTICULAR PURPOSE. 
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, see <http://www.gnu.org/licenses>.
*
* This code is available under licenses for commercial use. Please contact
* ChenFei for more information.
*
* http://www.gplgpu.com
* http://www.chenfei.me
*
* Title       :  Spring DDAL
* Author      :  Chen Fei
* Email       :  cn.fei.chen@qq.com
*
*/
package io.isharing.springddal.route.rule.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import io.isharing.springddal.route.exception.ConfigurationException;

public class XMLParserUtils {

	public static Document getDocument(final InputStream dtd, InputStream xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(String publicId, String systemId) {
				return new InputSource(dtd);
			}
		});
		builder.setErrorHandler(new ErrorHandler() {
			@Override
			public void warning(SAXParseException e) {
			}

			@Override
			public void error(SAXParseException e) throws SAXException {
				throw e;
			}

			@Override
			public void fatalError(SAXParseException e) throws SAXException {
				throw e;
			}
		});
		return builder.parse(xml);
	}
	
	public static Map<String, Object> loadAttributes(Element e) {
        Map<String, Object> map = new HashMap<String, Object>();
        NamedNodeMap nm = e.getAttributes();
        for (int j = 0; j < nm.getLength(); j++) {
            Node n = nm.item(j);
            if (n instanceof Attr) {
                Attr attr = (Attr) n;
                map.put(attr.getName(), attr.getNodeValue());
            }
        }
        return map;
    }

    public static Element loadElement(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 1) {
            throw new ConfigurationException(tagName + " elements length  over one!");
        }
        if (nodeList.getLength() == 1) {
            return (Element) nodeList.item(0);
        } else {
            return null;
        }
    }
    
    /**
	 * 获取节点下所有property
	 * 
	 * @param parent
	 * @return key-value property键值对
	 */
	public static Map<String, Object> loadElements(Element parent) {
		Map<String, Object> map = new HashMap<String, Object>();
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				Element e = (Element) node;
				String name = e.getNodeName();
				// 获取property
				if ("property".equals(name)) {
					String key = e.getAttribute("name");
					String value = e.getTextContent();
					map.put(key, StringUtils.isEmpty(value) ? null : value.trim());
				}
			}
		}
		return map;
	}

}
