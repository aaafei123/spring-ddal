package io.isharing.springddal.route.rule.conf;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.isharing.springddal.route.exception.ConfigurationException;
import io.isharing.springddal.route.rule.RuleAlgorithm;
import io.isharing.springddal.route.rule.function.AbstractPartitionAlgorithm;
import io.isharing.springddal.route.rule.utils.ParameterMapping;

public class XMLTableRulesLoader {
	
	private static final Logger log = LoggerFactory.getLogger(XMLTableRulesLoader.class);

	private final static String TableRulesDTD = "/rules.dtd";
	private final static String TableRulesXML = "/rules.xml";
	private final Map<String, TableRule> tableRules = new ConcurrentHashMap<String, TableRule>();
	private final Map<String, RuleAlgorithm> partitionAlgorithm = new ConcurrentHashMap<String, RuleAlgorithm>();

	public XMLTableRulesLoader() {
		super();
		loadTableRules();
	}
	
	public Map<String, TableRule> getTableRules() {
		return tableRules;
	}

	public Map<String, RuleAlgorithm> getPartitionAlgorithm() {
		return partitionAlgorithm;
	}

	private void loadTableRules() {
		Element root = null;
		try {
			InputStream isdtd = XMLDataNodesLoader.class.getResourceAsStream(TableRulesDTD);
			InputStream isxml = XMLDataNodesLoader.class.getResourceAsStream(TableRulesXML);
			Document xmldoc = XMLParserUtils.getDocument(isdtd, isxml);
			root = xmldoc.getDocumentElement();
			parseTableRulsXML(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseTableRulsXML(Element root) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		TableRule tableRule = null;
		NodeList list = root.getElementsByTagName("tableRules").item(0).getChildNodes();
		for (int i = 0, n = list.getLength(); i < n; ++i) {
			Node node = list.item(i);
			if (node instanceof Element) {
				Element e = (Element) node;
				// 获取name标签
				String name = e.getAttribute("name");
				// 如果Map已有，则function重复
				if (partitionAlgorithm.containsKey(name)) {
					throw new ConfigurationException("rule function " + name + " duplicated!");
				}
				// 获取class标签
				String clazz = e.getAttribute("class");
//				System.out.println(">>> "+name+", "+clazz);
				
				tableRule = new TableRule();
				tableRule.setName(name);
				tableRule.setClazz(clazz);
				
				Map<String, Object> props = XMLParserUtils.loadElements(e);
				tableRule = initTables(props, tableRule);
				
				// 根据class利用反射新建分片算法
				AbstractPartitionAlgorithm algorithm = createPartitionAlgorithm(name, clazz);
				// 根据读取参数配置分片算法
				ParameterMapping.mapping(algorithm, props);
				// 每个AbstractPartitionAlgorithm可能会实现init来初始化
				algorithm.init();
				// 放入functions map
				partitionAlgorithm.put(name, algorithm);
				tableRules.put(name, tableRule);
			}
		}
//		System.out.println("======================");
//		for (Map.Entry<String, TableRule> entry : tableRules.entrySet()) {
//			System.out.println(entry.getKey()+", "+entry.getValue());
//		}
//		System.out.println("----------------------");
//		for (Map.Entry<String, RuleAlgorithm> entry : partitionAlgorithm.entrySet()) {
//			System.out.println(entry.getKey()+", "+entry.getValue().getPartitionNum());
//		}
	}

	private AbstractPartitionAlgorithm createPartitionAlgorithm(String name, String clazz)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> clz = Class.forName(clazz);
		// 判断是否继承AbstractPartitionAlgorithm
		if (!AbstractPartitionAlgorithm.class.isAssignableFrom(clz)) {
			throw new IllegalArgumentException(
					"rule function must implements " + AbstractPartitionAlgorithm.class.getName() + ", name=" + name);
		}
		return (AbstractPartitionAlgorithm) clz.newInstance();
	}
	
	/**
	 * 根据配置文件初始化TableRule对象。这里也可以用Map替代，更具通用性，需要与XML节点各名称对应即可。
	 * 
	 * @param props
	 * @param tableRule
	 * @return
	 */
	private TableRule initTables(Map<String, Object> props, TableRule tableRule){
		for (Map.Entry<String, Object> entry : props.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if(key.equals(TableRule.NAME_ALIAS)){
				tableRule.setName(value.toString());
			}
			if(key.equals(TableRule.CLASS_ALIAS)){
				tableRule.setClazz(value.toString());
			}
			if(key.equals(TableRule.DEFAULT_NODE_ALIAS)){
				tableRule.setDefaultNode(value.toString());
			}
			if(key.equals(TableRule.ROUTE_COLUMN_ALIAS)){
				tableRule.setRouteColumn(value.toString());
			}
			if(key.equals(TableRule.MAPFILE_ALIAS)){
				tableRule.setMapFile(value.toString());
			}
			if(key.equals(TableRule.NODES_COUNT_ALIAS)){
				tableRule.setNodesCount(value.toString());
			}
			if(key.equals(TableRule.PARTITION_COUNT_ALIAS)){
				tableRule.setPartitionCount(value.toString());
			}
			if(key.equals(TableRule.PARTITION_LENGTH_ALIAS)){
				tableRule.setPartitionLength(value.toString());
			}
			if(key.equals(TableRule.DATEFORMAT_ALIAS)){
				tableRule.setDateFormat(value.toString());
			}
			if(key.equals(TableRule.SBEGINDATE_ALIAS)){
				tableRule.setsBeginDate(value.toString());
			}
			if(key.equals(TableRule.SENDDATE_ALIAS)){
				tableRule.setsEndDate(value.toString());
			}
			if(key.equals(TableRule.SPARTITIONDAY_ALIAS)){
				tableRule.setsPartionDay(value.toString());
			}
		}
		return tableRule;
	}
	
	public static void main(String[] args) {
		new XMLTableRulesLoader();
	}
}
