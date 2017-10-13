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

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import io.isharing.springddal.route.rule.conf.XMLLoader;

/**
 * 动态数据源
 * 
 * @author <a href=mailto:cn.fei.chen@qq.com>Chen Fei</a>
 * 
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

//	private Object writeDataSource;
//	private List<Object> readDataSources;
//	private Map<String, Object> readDataSources;
//	private int readDataSourceSize = 0;

//	private AtomicInteger readIndex = new AtomicInteger(0);
	
	/**
	 * 数据源键名
	 */
//	private static final String DATASOURCE_KEY_WRITE = "write";
//	private static final String DATASOURCE_KEY_READ = "read";
	
	public DynamicDataSource() {
		super();
		initDataSources();
	}

	/**
	 * 初始化数据库源
	 * 
	 * 备注：数据库源配置于dbshardConfig.xml文件中
	 * 
	 * @param dataSourceFactory
	 */
	private void initDataSources() {
		Object writeDataSource = XMLLoader.getDefaultWriteDataNode();
		setDefaultTargetDataSource(writeDataSource);
		Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
		targetDataSources = XMLLoader.getDataSources();
//		for (Map.Entry<Object, Object> entry : targetDataSources.entrySet()) {
//			System.out.println(entry.getKey() + "--->" + entry.getValue());
//		}
		setTargetDataSources(targetDataSources);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource#
	 * afterPropertiesSet()
	 */
//	@Override
//	public void afterPropertiesSet() {
//		if (this.writeDataSource == null) {
//			throw new IllegalArgumentException("Property 'writeDataSource' is required");
//		}
//		setDefaultTargetDataSource(writeDataSource);
//		Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
//		targetDataSources.put(DATASOURCE_KEY_WRITE, writeDataSource);
//		if (this.readDataSources == null) {
//			readDataSourceSize = 0;
//		} else {
//			/*for (int i = 0; i < readDataSources.size(); i++) {
//				targetDataSources.put(DATASOURCE_KEY_READ + i, readDataSources.get(i));
//			}*/
//			int i = 0;
//			for(Entry<String, Object> e : readDataSources.entrySet()) {
//				targetDataSources.put(DATASOURCE_KEY_READ + i, e.getValue());
//				i++;
//	        }
//			readDataSourceSize = readDataSources.size();
//		}
//		setTargetDataSources(targetDataSources);
//		super.afterPropertiesSet();
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource#
	 * determineCurrentLookupKey()
	 */
	@Override
	protected Object determineCurrentLookupKey() {
//		if (DynamicDataSourceHolder.isChoiceNone() || DynamicDataSourceHolder.isChoiceWrite()) {
//			return DATASOURCE_KEY_WRITE;
//		}
//		int index = readIndex.incrementAndGet() % readDataSourceSize;
//		return DATASOURCE_KEY_READ + index;
		return DynamicDataSourceHolder.getDataSourceKey();
	}

//	public DataSourceFactory getDataSourceFactory() {
//		return dataSourceFactory;
//	}
//
//	public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
//		this.dataSourceFactory = dataSourceFactory;
//	}

	/**
	 * @return the writeDataSource
	 */
//	public Object getWriteDataSource() {
//		return writeDataSource;
//	}

	/**
	 * @param writeDataSource
	 *            the writeDataSource to set
	 */
//	public void setWriteDataSource(Object writeDataSource) {
//		this.writeDataSource = writeDataSource;
//	}
//
//	public Map<String, Object> getReadDataSources() {
//		return readDataSources;
//	}
//
//	public void setReadDataSources(Map<String, Object> readDataSources) {
//		this.readDataSources = readDataSources;
//	}


	/**
	 * @return the readDataSources
	 */
//	public List<Object> getReadDataSources() {
//		return readDataSources;
//	}

	/**
	 * @param readDataSources
	 *            the readDataSources to set
	 */
//	public void setReadDataSources(List<Object> readDataSources) {
//		this.readDataSources = readDataSources;
//	}

}
