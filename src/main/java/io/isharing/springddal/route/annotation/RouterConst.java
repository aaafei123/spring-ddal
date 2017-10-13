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

/**
 * 拆分规则库引擎参数的默认属性名
 */
public class RouterConst {

    /**
     * 默认拆分字段，值为createDate
     */
//    public static final String ROUTER_ROUTEFIELD_DEFAULT = "createDate";
//    public static final String ROUTER_DBRULENAME_DEFAULT = "partitionByDate";
	
	public static final String ROUTER_GLOBAL = "Global";
    public static final String ROUTER_TBLRULENAME_DEFAULT = "AutoPartitionByLong";
    public static final String ROUTER_TABLE_SUFFIX_DEFAULT = "_0000";

}
