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
package io.isharing.springddal.route;

import java.sql.SQLNonTransientException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isharing.commons.date.DateTimeUtils;

import io.isharing.springddal.datasource.DynamicDataSourceHolder;
import io.isharing.springddal.route.annotation.Router;
import io.isharing.springddal.route.annotation.RouterConst;
import io.isharing.springddal.route.exception.ConfigurationException;
import io.isharing.springddal.route.rule.RuleAlgorithm;
import io.isharing.springddal.route.rule.conf.DataSourceNode;
import io.isharing.springddal.route.rule.conf.DbGlobal;
import io.isharing.springddal.route.rule.conf.TableRule;
import io.isharing.springddal.route.rule.conf.XMLLoader;
import io.isharing.springddal.route.rule.function.PartitionByDate;
import io.isharing.springddal.route.rule.function.PartitionByMonth;
import io.isharing.springddal.route.rule.function.PartitionByYear;
import io.isharing.springddal.route.rule.utils.SplitUtil;


/**
 * 根据规则、分片键、分片键的值进行动态拆库和表
 * 
 * @author <a href=mailto:cn.fei.chen@qq.com>Chen Fei</a>
 * 
 */
public class MysqlRouteStrategy implements RouteStrategy {

    private static final Logger log = LoggerFactory.getLogger(MysqlRouteStrategy.class);

	/**
	 * 根据理由规则进行路由计算，默认是只从各读库执行只读操作（即查询操作）
	 * 
	 * @param router			Router注解对象
	 * @param routeField		拆分字段
	 * @param routeFieldValue	拆分字段的值
	 * @throws SQLNonTransientException 
	 * 
	 */
	public void route(Router router, String routeField, String routeFieldValue) throws SQLNonTransientException {
		route(router, routeField, routeFieldValue, false);
	}

	/**
	 * 根据理由规则进行路由计算
	 * 
	 * @param router			Router注解对象
	 * @param routeField		拆分字段
	 * @param routeFieldValue	拆分字段的值
	 * @param isRead			是否是读操作
	 * @throws SQLNonTransientException 
	 * 
	 */
	public void route(Router router, String routeField, String routeFieldValue, boolean isRead) throws SQLNonTransientException {
		String tableIndex = "";
		Integer dbIndex = null;
        long tbIndex = 0;

		DbGlobal dbGlobalCfg = XMLLoader.getDbGlobal();
		Map<String, RuleAlgorithm> algorithmMap = XMLLoader.getPartitionAlgorithm();
		boolean forceReadOnMaster = router.forceReadOnMaster();
		String ruleName = router.ruleName();
		String dataNode = router.dataNode();
        
        long tableNum = dbGlobalCfg.getTableNumber();//分表的总数量
        int routeType = dbGlobalCfg.getRouteType();//路由类型
        log.error(">>> routeType="+routeType+", tableNum="+tableNum);
        RuleAlgorithm algorithm = algorithmMap.get(ruleName);
        if(algorithm instanceof PartitionByYear
        		|| algorithm instanceof PartitionByMonth
        		|| algorithm instanceof PartitionByDate){
        	log.error(">>> partition by time, convert the route field value to date time format.");
        	if(!validateIsLongTimeFormat(routeFieldValue)
        			&& !validateIsShortTimeFormat(routeFieldValue)){//new Date() 的格式处理：Mon Aug 14 12:13:55 CST 2017
        		routeFieldValue = DateTimeUtils.formatDateTime(routeFieldValue, "EEE MMM dd HH:mm:ss z yyyy", "yyyy-MM-dd HH:mm:ss");
        	}
        }
        int routeFieldInt = RouteUtils.getResourceCode(routeFieldValue);
        tbIndex = routeFieldInt % tableNum;//计算tables的索引
        
        if (routeType == ROUTER_TYPE_DB) {
        	dbIndex = algorithm.calculate(routeFieldValue);
        } else if (routeType == ROUTER_TYPE_TABLE) {
	        tableIndex = getFormateTableIndex(dbGlobalCfg.getTableIndexStyle(), tbIndex);
        }else if (routeType == ROUTER_TYPE_DBANDTABLE && tableNum != 0) {//如果是按照分库分表的话，计算
    		dbIndex = algorithm.calculate(routeFieldValue);
    	    tableIndex = getFormateTableIndex(dbGlobalCfg.getTableIndexStyle(), tbIndex);
        }
		log.error(">>> routeField="+routeField+", routeFieldValue="+routeFieldValue+", dbIndex="+dbIndex+", tableIndex="+tableIndex);
		
		List<String> dnList = getDataNodeList(dataNode);
		String node;
		if (dbIndex >= 0 && dbIndex < dnList.size()) {
			node = dnList.get(dbIndex);
		} else {
			node = null;
			String msg = ">>> FATAL ERROR! Can't find a valid data node for specified node index :" + dbIndex;
			log.error(msg);
			throw new SQLNonTransientException(msg);
		}
		routeToDataNode(node, tableIndex, isRead, forceReadOnMaster);
	}
	
	/**
	 * 路由到指定节点
	 * 
	 * @param dataNode			节点名称
	 * @param isRead			是否是读操作
	 * @param tableIndex		分表索引值
	 * @param forceReadOnMaster	强制从Master主库中执行读操作
	 * 
	 */
	public void routeToDataNode(String dataNode, String tableIndex, boolean isRead, boolean forceReadOnMaster){
		DataSourceNode nodes = getDataSourceNodeByName(dataNode);
		log.error(">>> dataNode="+dataNode+", tableIndex="+tableIndex+", isRead="+isRead+", forceReadOnMaster="+forceReadOnMaster);
		if(null == nodes){
			//找不到分片节点，路由至全局节点
			log.error(">>> "+dataNode+" is not define, using global node instead. isRead="+isRead+", forceReadOnMaster="+forceReadOnMaster);
			routeToGlobalNode(isRead, forceReadOnMaster);
			return;
		}
		processRoute(nodes, dataNode, tableIndex, isRead, forceReadOnMaster);
	}
	
	/**
	 * 针对全局表或者无Router注解配置使用，默认路由到全局库中。
	 * 根据注解参数做读写分离，若没配置注解，则使用默认值（默认写全局库的写库）。
	 * 
	 * @param isRead			是否是读操作
	 * @param forceReadOnMaster	强制从Master主库中执行读操作
	 * 
	 */
	public void routeToGlobalNode(boolean isRead, boolean forceReadOnMaster){
		log.error(">>> dataNode=global, tableIndex=, isRead=false, forceReadOnMaster=true");
		routeToDataNode("global", null, false, true);
	}

	/**
	 * 这个default node是指的rule.xml中配置的defaultNode.
	 * 
	 * 如果没有配置拆分字段，则默认路由到规则配置（rules.xml）中的默认节点。
	 * 如果还读不到写节点信息，则会再从dataNode节点中找默认写节点（相当于全局配置）。
	 * 
	 * @param ruleName	规则名称
	 * @param isRead			是否是读操作
	 * @param forceReadOnMaster	强制从Master主库中执行读操作
	 * 
	 */
	public void routeToDefaultNode(String ruleName, boolean isRead, boolean forceReadOnMaster){
		TableRule tableRule = XMLLoader.getTableRuleByRuleName(ruleName);
		if(null != tableRule){
			String defaultNodeName = tableRule.getDefaultNode();
			log.debug(">>> defaultNodeName="+defaultNodeName);
			log.error(">>> dataNode="+defaultNodeName+", tableIndex=, isRead="+isRead+", forceReadOnMaster="+forceReadOnMaster);
			routeToDataNode(defaultNodeName, null, isRead, forceReadOnMaster);
		}
	}
	
	private void processRoute(DataSourceNode nodes, String dataNode, String tableIndex, boolean isRead, boolean forceReadOnMaster){
		if(!isRead || forceReadOnMaster){
			routeToMasterDB(nodes, dataNode, tableIndex);
		}else{
			routeToSlavesDB(nodes, dataNode, tableIndex);
		}
	}
	
	/**
	 * 路由到Master主库
	 * 
	 * @param nodes
	 */
	private void routeToMasterDB(DataSourceNode nodes, String dbIndex, String tableIndex){
		DynamicDataSourceHolder.markWrite();
		List<String> nodesNameList = nodes.getWriteNodesNameList();
		String dbKey = nodesNameList.get(0);//TODO 默认只有一个写节点，未来是否考虑多个写节点？
		log.error(">>> route to master db, dataNode="+dbIndex+", dbKey="+dbKey);
		setDynamicDataSourceHolder(dbKey, tableIndex);
	}
	
	/**
	 * 路由到Slave从库
	 * 
	 * @param nodes
	 * @return
	 */
	private void routeToSlavesDB(DataSourceNode nodes, String dbIndex, String tableIndex){
		DynamicDataSourceHolder.markRead();
		List<String> nodesNameList = nodes.getReadNodesNameList();
		int index = (int) (Math.random() * nodesNameList.size());
		String dbKey = nodesNameList.get(index);
		log.error(">>> route to slaves db, dataNode="+dbIndex+", dbKey="+dbKey+", tableIndex="+tableIndex);
		setDynamicDataSourceHolder(dbKey, tableIndex);
	}
	
	private void setDynamicDataSourceHolder(String dbKey, String tableIndex){
		DynamicDataSourceHolder.setDataSourceKey(dbKey);
		DynamicDataSourceHolder.setTableIndex(tableIndex);
	}
	
	private DataSourceNode getDataSourceNodeByName(String dataNodeName){
		Map<String, DataSourceNode> dataNodesMap = XMLLoader.getDataNodesMap();
		DataSourceNode nodes = dataNodesMap.get(dataNodeName);
		return nodes;
	}
	
	private List<String> getDataNodeList(String dataNode){
		String theDataNodes[] = SplitUtil.split(dataNode, ',', '$', '-');
    	List<String> dataNodes = new ArrayList<String>(theDataNodes.length);
		for (String dn : theDataNodes) {
			dataNodes.add(dn);
		}
		return dataNodes;
	}

    /**
     * 此方法是将例如+++0000根式的字符串替换成传参数字例如44 变成+++0044
     */
    private static String getFormateTableIndex(String style, long tbIndex) {
        String tableIndex = null;
        DecimalFormat df = new DecimalFormat();
        if (StringUtils.isEmpty(style)) {
            style = RouterConst.ROUTER_TABLE_SUFFIX_DEFAULT;//在格式后添加诸如单位等字符
        }
        df.applyPattern(style);
        tableIndex = df.format(tbIndex);
        return tableIndex;
    }
	
    private static boolean validateIsLongTimeFormat(String timeStr) {
		String format = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
				+ "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";
		Pattern pattern = Pattern.compile(format);
		Matcher matcher = pattern.matcher(timeStr);
		if (matcher.matches()) {
			pattern = Pattern.compile("(\\d{4})-(\\d+)-(\\d+).*");
			matcher = pattern.matcher(timeStr);
			if (matcher.matches()) {
				int y = Integer.valueOf(matcher.group(1));
				int m = Integer.valueOf(matcher.group(2));
				int d = Integer.valueOf(matcher.group(3));
				if (d > 28) {
					Calendar c = Calendar.getInstance();
					c.set(y, m-1, 1);
					int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
					return (lastDay >= d);
				}
			}
			return true;
		}
		return false;
	}
    
    private static boolean validateIsShortTimeFormat(String timeStr) {
		String format = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])";
		Pattern pattern = Pattern.compile(format);
		Matcher matcher = pattern.matcher(timeStr);
		if (matcher.matches()) {
			pattern = Pattern.compile("(\\d{4})-(\\d+)-(\\d+).*");
			matcher = pattern.matcher(timeStr);
			if (matcher.matches()) {
				int y = Integer.valueOf(matcher.group(1));
				int m = Integer.valueOf(matcher.group(2));
				int d = Integer.valueOf(matcher.group(3));
				if (d > 28) {
					Calendar c = Calendar.getInstance();
					c.set(y, m-1, 1);
					int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
					return (lastDay >= d);
				}
			}
			return true;
		}
		return false;
	}
    
    public static void main(String[] args) {
//    	System.out.println(validateIsLongTimeFormat("2016-5-2 08:02:02"));
//		System.out.println(validateIsLongTimeFormat("2016-02-29 08:02:02"));
//		System.out.println(validateIsLongTimeFormat("2015-02-28 08:02:02"));
//		System.out.println(validateIsLongTimeFormat("2016-02-02 082:02"));
//		System.out.println(validateIsLongTimeFormat("2016-02-02"));
//		System.out.println(validateIsLongTimeFormat("2017-08-01 00:00:01"));
//		
//		System.out.println(validateIsShortTimeFormat("2016-02-02 082:02:21"));
//		System.out.println(validateIsShortTimeFormat("2016-02-02 05:02:23"));
//		System.out.println(validateIsShortTimeFormat("2016-02-02"));
		
		String d = "2017-08-01 00:00:01";
		System.out.println(validateIsLongTimeFormat(d));
		System.out.println(validateIsShortTimeFormat(d));
		if(!validateIsLongTimeFormat("2017-08-01 00:00:01")
    			&& !validateIsShortTimeFormat("2017-08-01 00:00:01")){
			System.out.println("---------");
		}
		
	}
    
}
