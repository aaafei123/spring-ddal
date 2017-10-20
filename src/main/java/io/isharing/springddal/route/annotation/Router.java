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
package io.isharing.springddal.route.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分库分表的路由配置，可定义在类名上也可定义在类方法名上，如果同时配置了，那么方法名上的配置优先级大于类名上的。<br>
 * 
 * @author <a href=mailto:cn.fei.chen@qq.com>Chen Fei</a>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface Router {
	
	/**
	 * 是否拆分，默认为拆分
	 */
	boolean isRoute() default true;
	
	/**
	 * 强制将读操作在写库中读，以避免写的时候从读库读不到数据（主从同步延迟导致的问题）
	 * TODO 未考虑事务传播的情况
	 */
	boolean forceReadOnMaster() default false;
	
	/**
	 * 是否读操作，默认true
	 */
	boolean readOnly() default true;
	
	/**
	 * 拆分字段
	 */
//    String routerField() default RouterConst.ROUTER_ROUTEFIELD_DEFAULT;
    
    /**
     * 拆分规则名
     */
    String ruleName() default "";
    
    /**
     * 数据库节点，对应datanodes.xml文件中的dataNodes节点。
     * 多个节点以英文逗号分隔，如果是连续节点可以定义为：dn$0-10，表示定义了dn0-dn10共10个节点。
     */
    String dataNode() default "";
    
    /**
     * 拆分类型，普通规则拆分默认为空。全局库/表须定义为：global
     */
    String type() default "";
    
    /**
     * 拆分表的后缀样式/风格
     */
    String tableStyle() default RouterConst.ROUTER_TABLE_SUFFIX_DEFAULT;
    
}
