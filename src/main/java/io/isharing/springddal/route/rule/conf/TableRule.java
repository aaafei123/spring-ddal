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

import java.io.Serializable;

public class TableRule implements Serializable {

	private static final long serialVersionUID = -4371474720936040919L;
	
	public static final String NAME_ALIAS = "name";
	public static final String CLASS_ALIAS = "class";
	public static final String DEFAULT_NODE_ALIAS = "defaultNode";
	public static final String ROUTE_COLUMN_ALIAS = "routeColumn";
	public static final String MAPFILE_ALIAS = "mapFile";
	public static final String NODES_COUNT_ALIAS = "nodesCount";
	public static final String PARTITION_COUNT_ALIAS = "partitionCount";
	public static final String PARTITION_LENGTH_ALIAS = "partitionLength";
	public static final String DATEFORMAT_ALIAS = "dateFormat";
	public static final String SBEGINDATE_ALIAS = "sBeginDate";
	public static final String SENDDATE_ALIAS = "sEndDate";
	public static final String SPARTITIONDAY_ALIAS = "sPartionDay";
	
	
	private String name;
	private String clazz;
	
	private String defaultNode;
	private String routeColumn;

	private String mapFile;

	private String nodesCount;

	private String partitionCount;
	private String partitionLength;

	private String dateFormat;
	private String sBeginDate;
	private String sEndDate;
	private String sPartionDay;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getDefaultNode() {
		return defaultNode;
	}

	public void setDefaultNode(String defaultNode) {
		this.defaultNode = defaultNode;
	}

	public String getRouteColumn() {
		return routeColumn;
	}

	public void setRouteColumn(String routeColumn) {
		this.routeColumn = routeColumn;
	}

	public String getMapFile() {
		return mapFile;
	}

	public void setMapFile(String mapFile) {
		this.mapFile = mapFile;
	}
	
	public String getNodesCount() {
		return nodesCount;
	}

	public void setNodesCount(String nodesCount) {
		this.nodesCount = nodesCount;
	}

	public String getPartitionCount() {
		return partitionCount;
	}

	public void setPartitionCount(String partitionCount) {
		this.partitionCount = partitionCount;
	}

	public String getPartitionLength() {
		return partitionLength;
	}

	public void setPartitionLength(String partitionLength) {
		this.partitionLength = partitionLength;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getsBeginDate() {
		return sBeginDate;
	}

	public void setsBeginDate(String sBeginDate) {
		this.sBeginDate = sBeginDate;
	}

	public String getsEndDate() {
		return sEndDate;
	}

	public void setsEndDate(String sEndDate) {
		this.sEndDate = sEndDate;
	}

	public String getsPartionDay() {
		return sPartionDay;
	}

	public void setsPartionDay(String sPartionDay) {
		this.sPartionDay = sPartionDay;
	}

	@Override
	public String toString() {
		return "TableRule [name=" + name + ", clazz=" + clazz + ", defaultNode=" + defaultNode + ", routeColumn="
				+ routeColumn + ", mapFile=" + mapFile + ", nodesCount=" + nodesCount + ", partitionCount="
				+ partitionCount + ", partitionLength=" + partitionLength + ", dateFormat=" + dateFormat
				+ ", sBeginDate=" + sBeginDate + ", sEndDate=" + sEndDate + ", sPartionDay=" + sPartionDay + "]";
	}

}
