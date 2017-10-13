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
package io.isharing.springddal.datasource;

/**
 * 数据源管理器
 * 
 * @author <a href=mailto:cn.fei.chen@qq.com>Chen Fei</a>
 * 
 */
public class DynamicDataSourceHolder {
	
    private static final ThreadLocal<DataSourceType> rwholder = new ThreadLocal<DataSourceType>();
    private static final ThreadLocal<String> dataSourceHolder = new ThreadLocal<String>();
	private static final ThreadLocal<String> tableIndexHolder= new ThreadLocal<String>();
	
	private static enum DataSourceType {
        write, read;
    }
    
    /**
     * 标记为写数据源
     */
    public static void markWrite() {
        rwholder.set(DataSourceType.write);
    }
    
    /**
     * 标记为读数据源
     */
    public static void markRead() {
        rwholder.set(DataSourceType.read);
    }
    
    /**
     * 重置
     */
    public static void reset() {
        rwholder.set(null);
    }
    
    /**
     * 是否还未设置数据源
     * @return
     */
    public static boolean isChoiceNone() {
        return null == rwholder.get(); 
    }
    
    /**
     * 当前是否选择了写数据源
     * @return
     */
    public static boolean isChoiceWrite() {
        return DataSourceType.write == rwholder.get();
    }
    
    /**
     * 当前是否选择了读数据源
     * @return
     */
    public static boolean isChoiceRead() {
        return DataSourceType.read == rwholder.get();
    }
    
    public static void setDataSourceKey(String dbKey) {
		dataSourceHolder.set(dbKey);
	}

	public static String getDataSourceKey() {
		return (String) dataSourceHolder.get();
	}

	public static void clearDbKey() {
		dataSourceHolder.remove();
	}
	
	public static void setTableIndex(String tableIndex){
		tableIndexHolder.set(tableIndex);
	}
	
	public static String getTableIndex(){
		return (String) tableIndexHolder.get();
	}
	
	public static void clearTableIndex(){
		tableIndexHolder.remove();
	}
	
}

