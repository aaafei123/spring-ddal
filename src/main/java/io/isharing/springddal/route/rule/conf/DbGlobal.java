package io.isharing.springddal.route.rule.conf;

public class DbGlobal {
	
	/** 数据库数量 */
//	private int dbNumber;
	
	/** 数据表数量 */
	private int tableNumber;
	
	/** 规则类型 */
//	private int ruleType;
	
	/** 路由类型 */
	private int routeType;
	
	/** 数据表index样式 */
	private String tableIndexStyle;
	
	/** 默认主库写节点 */
//	private String defaultWriteNode;

//	public int getDbNumber() {
//		return dbNumber;
//	}
//
//	public void setDbNumber(int dbNumber) {
//		this.dbNumber = dbNumber;
//	}

	public int getTableNumber() {
		return tableNumber;
	}

	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

//	public int getRuleType() {
//		return ruleType;
//	}
//
//	public void setRuleType(int ruleType) {
//		this.ruleType = ruleType;
//	}

	public int getRouteType() {
		return routeType;
	}

	public void setRouteType(int routeType) {
		this.routeType = routeType;
	}

	public String getTableIndexStyle() {
		return tableIndexStyle;
	}

	public void setTableIndexStyle(String tableIndexStyle) {
		this.tableIndexStyle = tableIndexStyle;
	}

//	public String getDefaultWriteNode() {
//		return defaultWriteNode;
//	}
//
//	public void setDefaultWriteNode(String defaultWriteNode) {
//		this.defaultWriteNode = defaultWriteNode;
//	}
	
}
