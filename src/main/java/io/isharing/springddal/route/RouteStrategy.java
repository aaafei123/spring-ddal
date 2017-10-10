package io.isharing.springddal.route;

/**
 * 路由接口  通过调用该接口来自动判断数据位于哪个服务器
 */
public interface RouteStrategy {
	
	public final static int ROUTER_TYPE_DB = 0;				/*仅分库*/
	public final static int ROUTER_TYPE_TABLE = 1;			/*仅分表*/
	public final static int ROUTER_TYPE_DBANDTABLE = 2;	/*分库分表*/
	
	/**
	 * 根据理由规则进行路由计算
	 * 
	 * @param ruleName			路由规则
	 * @param routeField		拆分字段
	 * @param routeFieldValue	拆分字段的值
	 * @return					通过DbContextHolder保存拆分库表后的index
	 * @throws
	 */
    public void route(String ruleName, String routeField, String routeFieldValue);
    
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
    public void route(String ruleName, String routeField, String routeFieldValue, boolean isRead, boolean forceReadOnMaster);
    
    /**
     * 针对全局表或者无Router注解配置使用，默认路由到全局库中。
     */
    public void routeToGlobalNode();
    
    /**
	 * 如果没有配置拆分字段，则默认路由到规则配置（rules.xml）中的默认节点。
	 * 如果还读不到写节点信息，则会再从dataNode节点中找默认写节点（相当于全局配置）。
	 * 
     * @param ruleName	规则名称
     */
    public void routeToDefaultNode(String ruleName);
    
    
}
