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
