package io.isharing.springddal.route.rule.conf;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

import io.isharing.springddal.route.rule.RuleAlgorithm;

public class XMLLoader {
	
//	private static List<DataSourceNode> dataNodes = null;
	private static Map<String, DataSourceNode> dataNodesMap = null;
	private static Map<Object, Object> dataSources = null;
	private static DbGlobal dbGlobal = null;
	private static DataSource defaultWriteDataNode;
	private static String defaultWriteDataNodeName;
	
	private static Map<String, TableRule> tableRules = null;
	private static Map<String, RuleAlgorithm> partitionAlgorithm = null;
	
	private volatile static XMLDataNodesLoader ds;
	private volatile static XMLTableRulesLoader rl;
	
	private static XMLDataNodesLoader getXMLDataNodesLoader() {
		if (ds == null) {
			synchronized (XMLDataNodesLoader.class) {
				if (ds == null) {
					ds = new XMLDataNodesLoader();
				}
			}
		}
		return ds;
	}
	
	private static XMLTableRulesLoader getXMLTableRulesLoader() {
		if (rl == null) {
			synchronized (XMLTableRulesLoader.class) {
				if (rl == null) {
					rl = new XMLTableRulesLoader();
				}
			}
		}
		return rl;
	}

//	public static List<DataSourceNode> getDataNodes() {
//		if(dataNodes == null) {
//			dataNodes = getXMLDataNodesLoader().getDataNodes();
//		}
//		return dataNodes;
//	}

	public static Map<String, DataSourceNode> getDataNodesMap() {
		if(dataNodesMap == null) {
			dataNodesMap = getXMLDataNodesLoader().getDataNodesMap();
		}
		return dataNodesMap;
	}

	public static Map<Object, Object> getDataSources() {
		if(dataSources == null) {
			dataSources = getXMLDataNodesLoader().getDataSources();
		}
		return dataSources;
	}

	public static DataSource getDefaultWriteDataNode() {
		if(defaultWriteDataNode == null) {
			defaultWriteDataNode = getXMLDataNodesLoader().getDefaultWriteDataNode();
		}
		return defaultWriteDataNode;
	}

	public static String getDefaultWriteDataNodeName() {
		if(StringUtils.isBlank(defaultWriteDataNodeName)){
			defaultWriteDataNodeName = getXMLDataNodesLoader().getDefaultWriteDataNodeName();
		}
		return defaultWriteDataNodeName;
	}

	public static DbGlobal getDbGlobal() {
		if(dbGlobal == null) {
			dbGlobal = getXMLDataNodesLoader().getDbGlobal();
		}
		return dbGlobal;
	}

	public static Map<String, TableRule> getTableRules() {
		if(tableRules == null) {
			tableRules = getXMLTableRulesLoader().getTableRules();
		}
		return tableRules;
	}

	public static Map<String, RuleAlgorithm> getPartitionAlgorithm() {
		if(partitionAlgorithm == null) {
			partitionAlgorithm = getXMLTableRulesLoader().getPartitionAlgorithm();
		}
		return partitionAlgorithm;
	}
	
    public static TableRule getTableRuleByRuleName(String ruleName){
    	if(StringUtils.isBlank(ruleName)){
    		return null;
    	}
    	Map<String, TableRule> tableRuleMap = getTableRules();
    	TableRule tableRule = tableRuleMap.get(ruleName);
    	return tableRule;
    }
	
	public static void main(String[] args) {
//		XMLLoader xml = new XMLLoader();
//		System.out.println(XMLLoader.getDataNodes());
//		System.out.println(XMLLoader.getDataNodes());
		Map<String, DataSourceNode>  m = XMLLoader.getDataNodesMap();
		for (Map.Entry<String, DataSourceNode> entry : m.entrySet()) {
			System.out.println(entry.getKey() + "--->" + entry.getValue());
		}
//		System.out.println(XMLLoader.getDataNodesMap());
//		System.out.println(XMLLoader.getDataSources());
//		System.out.println(XMLLoader.getDbGlobal());
//		System.out.println(XMLLoader.getTableRules());
//		System.out.println(XMLLoader.getDefaultWriteDataNodeName());
//		System.out.println(XMLLoader.getDefaultWriteDataNode());
		
	}
}
