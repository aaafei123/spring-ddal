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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.druid.pool.DruidDataSourceFactory;


public class XMLDataNodesLoader {
	
	private static final Logger log = LoggerFactory.getLogger(XMLDataNodesLoader.class);
	
	private final static String DataNodesDTD = "/datanodes.dtd";
	private final static String DataNodesXML = "/datanodes.xml";
	
	/**
	 * dataNodes的数据源，以key-value存储，其中key为XML文件中dataNode的name属性，value为DataSourceNode对象。DataSourceNode对应XML文件中的dataNode节点，数据源以List保存。
	 */
	private final Map<String, DataSourceNode> dataNodesMap = new ConcurrentHashMap<String, DataSourceNode>();
	/**
	 * 所有数据源（包含读、写数据源），均以key-value的形式存储，其中key为XML文件中dataSource的name属性，value为转换后的DataSource对象
	 */
	private final Map<Object, Object> dataSources = new ConcurrentHashMap<Object, Object>();
	
	/**
	 * 数据库的全局配置
	 */
	private DbGlobal dbGlobal = new DbGlobal();
	
	/**
	 * 全局的默认写节点
	 */
	private DataSource defaultWriteDataNode;
	
	/**
	 * 全局的默认写节点的key名称
	 */
	private String defaultWriteDataNodeName;
	
	
	public XMLDataNodesLoader() {
		super();
		loadDataNodesConfig();
	}

	public Map<String, DataSourceNode> getDataNodesMap() {
		return dataNodesMap;
	}

	public Map<Object, Object> getDataSources() {
		return dataSources;
	}

	public DbGlobal getDbGlobal() {
		return dbGlobal;
	}

	public DataSource getDefaultWriteDataNode() {
		return defaultWriteDataNode;
	}
	
	public String getDefaultWriteDataNodeName() {
		return defaultWriteDataNodeName;
	}

	private void loadDataNodesConfig() {
		Element root = null;
		try {
			InputStream isdtd = XMLDataNodesLoader.class.getResourceAsStream(DataNodesDTD);
			InputStream isxml = XMLDataNodesLoader.class.getResourceAsStream(DataNodesXML);
			Document xmldoc = XMLParserUtils.getDocument(isdtd, isxml);
			root = xmldoc.getDocumentElement();
			//DataSource Configuration
			Map<String, DataNodeSource> ds = parseDataSourceCfg(root);

			//dataNode Configuration
			List<DataNode> dataNodeCfgs = parseDataNodeCfg(root);
			
			//DB Global Configuration
			dbGlobal = parseDbGlobalCfg(root);
			
			Map<String, DataSource> dataSourceMap = initDataSources(ds);
			initDataNodes(dataSourceMap, dataNodeCfgs);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, DataSource> initDataSources(Map<String, DataNodeSource> dataSourceCfgs) throws Exception{
		Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
		for (Map.Entry<String, DataNodeSource> entry : dataSourceCfgs.entrySet()) {
			Properties p = new Properties();
			p.setProperty("driverClassName", "com.mysql.jdbc.Driver");
			p.setProperty("url", entry.getValue().getUrl());
			p.setProperty("username", entry.getValue().getUserName());
			p.setProperty("password", entry.getValue().getPassword());
			
			p.setProperty("filters", "stat");
			p.setProperty("maxActive", "200");
			p.setProperty("initialSize", "30");
			p.setProperty("maxWait", "60000");
			p.setProperty("minIdle","1");
			p.setProperty("timeBetweenEvictionRunsMillis","60000");
			p.setProperty("minEvictableIdleTimeMillis","300000");
			p.setProperty("validationQuery","SELECT 'x'");
			p.setProperty("testWhileIdle","true");
			p.setProperty("testOnBorrow","false");
			p.setProperty("testOnReturn","false");
			
//			DataSource ds = BasicDataSourceFactory.createDataSource(p);	//dhcp
			DataSource ds = DruidDataSourceFactory.createDataSource(p); //druid
			
			dataSourceMap.put(entry.getValue().getName(), ds);
			//log.debug(">>> key=" + entry.getKey() + ", value=" + entry.getValue().getUrl());
		}
		return dataSourceMap;
	}
	
	private void initDataNodes(Map<String, DataSource> dataSourceMap, List<DataNode> dataNodeCfgs) {
		for (DataNode cfg : dataNodeCfgs) {
//			List<DataSource> writeNodesDsList = new ArrayList<DataSource>();
//			List<DataSource> readNodesDsList = new ArrayList<DataSource>();
			List<String> writeNodesDsNameList = new ArrayList<String>();
			List<String> readNodesDsNameList = new ArrayList<String>();
			
			List<String> writeNodesStr = split(cfg.getWriteNodes(), ",");
			
			for (String str : writeNodesStr) {
				if (dataSourceMap.get(str) != null) {
//					writeNodesDsList.add(dataSourceMap.get(str));
					writeNodesDsNameList.add(str);
					dataSources.put(str, dataSourceMap.get(str));
					if(cfg.isDefaultWriteNode()){
						defaultWriteDataNode = dataSourceMap.get(str);
						defaultWriteDataNodeName = str;
					}
				}
			}
			List<String> readNodesStr = split(cfg.getReadNodes(), ",");
			for (String str : readNodesStr) {
				if (dataSourceMap.get(str) != null) {
//					readNodesDsList.add(dataSourceMap.get(str));
					readNodesDsNameList.add(str);
					dataSources.put(str, dataSourceMap.get(str));
				}
			}
			DataSourceNode dsNode = new DataSourceNode(writeNodesDsNameList, readNodesDsNameList);
			if(cfg.isDefaultWriteNode()){
				dataNodesMap.put("default", dsNode);
			}
			dataNodesMap.put(cfg.getNodeName(), dsNode);
//			dataNodes.add(dsNode);
		}
		dataSourceMap = null;
	}
	
	/**
	 * DataSource Configuration
	 * @param root
	 */
	private Map<String, DataNodeSource> parseDataSourceCfg(Element root){
		Map<String, DataNodeSource> dataSourcesMap = new ConcurrentHashMap<String, DataNodeSource>();
		NodeList nodeList = root.getElementsByTagName("dataSources").item(0).getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i) == null || nodeList.item(i).getChildNodes().getLength() < 1) {
				continue;
			}
			DataNodeSource dscfg = new DataNodeSource();
			
			String dsName = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
			dscfg.setName(dsName);
			dataSourcesMap.put(dsName, dscfg);
			
			NodeList dsNodes = nodeList.item(i).getChildNodes();
			for (int j = 0; j < dsNodes.getLength(); j++) {
				Node tmp = dsNodes.item(j);
				if (tmp == null) {
					continue;
				}
				if ("url".equals(tmp.getNodeName())) {
					dscfg.setUrl(tmp.getTextContent());
					continue;
				}
				if ("userName".equals(tmp.getNodeName())) {
					dscfg.setUserName(tmp.getTextContent());
					continue;
				}
				if ("password".equals(tmp.getNodeName())) {
					dscfg.setPassword(tmp.getTextContent());
					continue;
				}
			}
		}
		return dataSourcesMap;
	}
	
	/**
	 * DataNode configuration
	 * 
	 * @param root
	 */
	private List<DataNode> parseDataNodeCfg(Element root){
		List<DataNode> dataNodesList = new ArrayList<DataNode>();
		NodeList dnList = root.getElementsByTagName("dataNodes").item(0).getChildNodes();
		for (int i = 0; i < dnList.getLength(); i++) {
			if (dnList.item(i) == null || dnList.item(i).getChildNodes().getLength() < 1) {
				continue;
			}
			DataNode dataNodeCfg = new DataNode();
			
			String nodeName = dnList.item(i).getAttributes().getNamedItem("name").getNodeValue();
			dataNodeCfg.setNodeName(nodeName);
			
			try{
				String isDefault = dnList.item(i).getAttributes().getNamedItem("default").getNodeValue();
				dataNodeCfg.setDefaultWriteNode(Boolean.parseBoolean(isDefault));
			}catch(NullPointerException ne){
				dataNodeCfg.setDefaultWriteNode(false);
			}
			
			NodeList dn = dnList.item(i).getChildNodes();
			dataNodesList.add(dataNodeCfg);
			for (int k = 0; k < dn.getLength(); k++) {
				Node tmp = dn.item(k);
				if (tmp == null) {
					continue;
				}
				if ("writeNodes".equals(tmp.getNodeName())) {
					dataNodeCfg.setWriteNodes(tmp.getTextContent());
					continue;
				}
				if ("readNodes".equals(tmp.getNodeName())) {
					dataNodeCfg.setReadNodes(tmp.getTextContent());
					continue;
				}
			}
		}
		return dataNodesList;
	}
	
	/**
	 * DB Global Configuration
	 * @param root
	 */
	private DbGlobal parseDbGlobalCfg(Element root){
		DbGlobal dbGlobalCfg = new DbGlobal();
		NodeList dgnl = root.getElementsByTagName("global");
		for (int i = 0; i < dgnl.getLength(); i++) {
			NodeList dsNodes = dgnl.item(i).getChildNodes();
			for (int j = 0; j < dsNodes.getLength(); j++) {
				Node tmp = dsNodes.item(j);
				if (tmp == null) {
					continue;
				}
//				if ("dbNumber".equals(tmp.getNodeName())) {
//					dbGlobalCfg.setDbNumber(Integer.parseInt(tmp.getTextContent()));
//					continue;
//				}
				if ("tableNumber".equals(tmp.getNodeName())) {
					dbGlobalCfg.setTableNumber(Integer.parseInt(tmp.getTextContent()));
					continue;
				}
				if ("routeType".equals(tmp.getNodeName())) {
					dbGlobalCfg.setRouteType(Integer.parseInt(tmp.getTextContent()));
					continue;
				}
//				if ("ruleType".equals(tmp.getNodeName())) {
//					dbGlobalCfg.setRuleType(Integer.parseInt(tmp.getTextContent()));
//					continue;
//				}
//				if ("defaultWriteNode".equals(tmp.getNodeName())) {
//					dbGlobalCfg.setDefaultWriteNode(tmp.getTextContent());
//					continue;
//				}
				if ("tableIndexStyle".equals(tmp.getNodeName())) {
					dbGlobalCfg.setTableIndexStyle(tmp.getTextContent());
					continue;
				}
			}
		}
		return dbGlobalCfg;
	}
	
	private List<String> split(String input, String s) {
		if (input == null) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		String[] tmp = input.split(s);
		for (String str : tmp) {
			if (str == null || str.matches("\\s*")) {
				continue;
			}
			list.add(str.trim());
		}
		return list;
	}
	
	public static void main(String[] args) {
		new XMLDataNodesLoader();
	}
}
