package io.isharing.springddal.route;

import java.text.DecimalFormat;
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
import io.isharing.springddal.route.annotation.RouterConst;
import io.isharing.springddal.route.rule.RuleAlgorithm;
import io.isharing.springddal.route.rule.conf.DataSourceNode;
import io.isharing.springddal.route.rule.conf.DbGlobal;
import io.isharing.springddal.route.rule.conf.TableRule;
import io.isharing.springddal.route.rule.conf.XMLLoader;
import io.isharing.springddal.route.rule.function.PartitionByDate;
import io.isharing.springddal.route.rule.function.PartitionByMonth;
import io.isharing.springddal.route.rule.function.PartitionByYear;


/**
 * 根据规则、分片键、分片键的值进行动态拆库和表
 */
public class MysqlRouteStrategy implements RouteStrategy {

    private static final Logger log = LoggerFactory.getLogger(MysqlRouteStrategy.class);

	/**
	 * 根据理由规则进行路由计算，默认是只从各读库执行只读操作（即查询操作）
	 * 
	 * @param ruleName			路由规则
	 * @param routeField		拆分字段
	 * @param routeFieldValue	拆分字段的值
	 * @return					通过DbContextHolder保存拆分库表后的index
	 * @throws
	 */
	public void route(String ruleName, String routeField, String routeFieldValue) {
		route(ruleName, routeField, routeFieldValue, false, false);
	}

	/**
	 * 根据理由规则进行路由计算
	 * 
	 * @param ruleName			路由规则
	 * @param routeField		拆分字段
	 * @param routeFieldValue	拆分字段的值
	 * @param isRead			是否是读操作
	 * @param forceReadOnMaster	强制从Master主库中执行读操作
	 * @return					通过DbContextHolder保存拆分库表后的index
	 * @throws
	 */
	public void route(String ruleName, String routeField, String routeFieldValue, boolean isRead, boolean forceReadOnMaster) {
		String dbKey = "";
		String tableIndex = "";
		Integer dbIndex = null;
        long tbIndex = 0;

		DbGlobal dbGlobalCfg = XMLLoader.getDbGlobal();
		Map<String, DataSourceNode> dataNodesMap = XMLLoader.getDataNodesMap();
		Map<String, RuleAlgorithm> algorithmMap = XMLLoader.getPartitionAlgorithm();
        
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
        
        if (routeType == ROUTER_TYPE_DB) {
        	dbIndex = algorithm.calculate(routeFieldValue);
        } else if (routeType == ROUTER_TYPE_TABLE) {
        	tbIndex = routeFieldInt % tableNum;
	        tableIndex = getFormateTableIndex(dbGlobalCfg.getTableIndexStyle(), tbIndex);
        }else if (routeType == ROUTER_TYPE_DBANDTABLE && tableNum != 0) {//如果是按照分库分表的话，计算
    			dbIndex = algorithm.calculate(routeFieldValue);
    	        tbIndex = routeFieldInt % tableNum;
    	        tableIndex = getFormateTableIndex(dbGlobalCfg.getTableIndexStyle(), tbIndex);
        }
		DataSourceNode nodes = dataNodesMap.get("dn"+dbIndex);//TODO name应该可以随意命名，不应该必须以dn为前缀
		log.error(">>> isRead="+isRead+", forceReadOnMaster="+forceReadOnMaster);
        if(isRead){
        	if(forceReadOnMaster){
        		dbKey = routeToMasterDB(nodes);
        	}else{
    			dbKey = routeToSlavesDB(nodes);
        	}
		}else{
			dbKey = routeToMasterDB(nodes);
		}
        log.error(">>> dataNode=dn"+dbIndex+", dbKey="+dbKey+", tableIndex="+tableIndex);
        setDynamicDataSourceHolder(dbKey, tableIndex);
	}
	
	/**
	 * 针对全局表或者无Router注解配置使用，默认路由到全局库中。
	 */
	public void routeToGlobalNode(){
		Map<String, DataSourceNode> dataNodesMap = XMLLoader.getDataNodesMap();
		DataSourceNode nodes = dataNodesMap.get("global");
		String dbKey = routeToMasterDB(nodes);
		log.error(">>> dataNode=global, dbKey="+dbKey+", tableIndex=");
		setDynamicDataSourceHolder(dbKey, null);
	}

	/**
	 * 如果没有配置拆分字段，则默认路由到规则配置（rules.xml）中的默认节点。
	 * 如果还读不到写节点信息，则会再从dataNode节点中找默认写节点（相当于全局配置）。
	 * 
	 * @param ruleName	规则名称
	 * 
	 */
	public void routeToDefaultNode(String ruleName){
		DataSourceNode nodes = null;
		String defaultNodeName = "";
		TableRule tableRule = XMLLoader.getTableRuleByRuleName(ruleName);
		if(null != tableRule){
			defaultNodeName = tableRule.getDefaultNode();
			log.debug(">>> defaultNodeName="+defaultNodeName);
			Map<String, DataSourceNode> dataNodesMap = XMLLoader.getDataNodesMap();
			nodes = dataNodesMap.get(defaultNodeName);
		}
		String dbKey = routeToMasterDB(nodes);
		log.error(">>> dataNode="+defaultNodeName+", dbKey="+dbKey+", tableIndex=");
		setDynamicDataSourceHolder(dbKey, null);
	}
	
	private String routeToSlavesDB(DataSourceNode nodes){
		DynamicDataSourceHolder.markRead();
		List<String> nodesNameList = nodes.getReadNodesNameList();
		int index = (int) (Math.random() * nodesNameList.size());
		String dbKey = nodesNameList.get(index);
		log.error(">>> route to slaves db, dbKey="+dbKey);
		return dbKey;
	}
	
	private String routeToMasterDB(DataSourceNode nodes){
		String dbKey = "";
		DynamicDataSourceHolder.markWrite();
		if(null == nodes){
			dbKey = XMLLoader.getDefaultWriteDataNodeName();
			log.error(">>> Router not config, using global default data node for query. dbkey="+dbKey);
			return dbKey;
		}
		List<String> nodesNameList = nodes.getWriteNodesNameList();
		if(null == nodesNameList || nodesNameList.size() <= 0){//这个情况应该不会出现，只要配置了writeNodes节点都不可能为空
			log.error(">>> node data list is NULL!");
			return XMLLoader.getDefaultWriteDataNodeName();//如果还读不到写节点信息，则会再从dataNode节点中找默认写节点（相当于全局配置）
		}
		dbKey = nodesNameList.get(0);//TODO 默认只有一个写节点，未来是否考虑多个写节点？
		if(StringUtils.isBlank(dbKey)){//这个情况应该也不会出现，只要配置了writeNodes节点都不可能为空
			dbKey = XMLLoader.getDefaultWriteDataNodeName();
		}
		log.error(">>> route to master db, dbKey="+dbKey);
		return dbKey;
	}
	
	private void setDynamicDataSourceHolder(String dbKey, String tableIndex){
		DynamicDataSourceHolder.setDataSourceKey(dbKey);
		DynamicDataSourceHolder.setTableIndex(tableIndex);
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
