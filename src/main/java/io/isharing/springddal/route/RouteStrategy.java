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

import io.isharing.springddal.route.annotation.Router;

/**
 * 路由接口  通过调用该接口来自动判断数据位于哪个服务器
 * 
 * @author <a href=mailto:cn.fei.chen@qq.com>Chen Fei</a>
 * 
 */
public interface RouteStrategy {
	
	public final static int ROUTER_TYPE_DB = 0;				/*仅分库*/
	public final static int ROUTER_TYPE_TABLE = 1;			/*仅分表*/
	public final static int ROUTER_TYPE_DBANDTABLE = 2;	/*分库分表*/
	
	/**
	 * 根据理由规则进行路由计算
	 * 
	 * @param router			Router注解对象
	 * @param routeField		拆分字段
	 * @param routeFieldValue	拆分字段的值
	 * @return					通过DbContextHolder保存拆分库表后的index
	 * @throws
	 */
    public void route(Router router, String routeField, String routeFieldValue) throws SQLNonTransientException;
    
	/**
	 * 根据理由规则进行路由计算
	 * 
	 * @param router			Router注解对象
	 * @param routeField		拆分字段
	 * @param routeFieldValue	拆分字段的值
	 * @param isRead			是否是读操作
	 * @return					通过DbContextHolder保存拆分库表后的index
	 * @throws
	 */
    public void route(Router router, String routeField, String routeFieldValue, boolean isRead) throws SQLNonTransientException;
    
    /**
     * 针对全局表或者无Router注解配置使用，默认路由到全局库中。
     */
    public void routeToGlobalNode(boolean isRead, boolean forceReadOnMaster);
    
    /**
	 * 如果没有配置拆分字段，则默认路由到规则配置（rules.xml）中的默认节点。
	 * 如果还读不到写节点信息，则会再从dataNode节点中找默认写节点（相当于全局配置）。
	 * 
     * @param ruleName	规则名称
     */
    public void routeToDefaultNode(String ruleName, boolean isRead, boolean forceReadOnMaster);
    
    
}
