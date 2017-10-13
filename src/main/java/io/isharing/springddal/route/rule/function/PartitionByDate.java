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
package io.isharing.springddal.route.rule.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.isharing.springddal.route.rule.RuleAlgorithm;

/**
 * 例子 按日期列分区  格式 between操作解析的范例
 * 功能说明：按开始时间sBeginDate到结束时间sEndDate范围内，每sPartionDay天做一个分片。
 */
public class PartitionByDate extends AbstractPartitionAlgorithm implements RuleAlgorithm {

	private static final long serialVersionUID = -4646042281671936362L;
	private static final Logger LOGGER = LoggerFactory.getLogger(PartitionByDate.class);

	/**
	 * 开始日期
	 */
	private String sBeginDate;
	/**
	 * 结束日期, 如果配置了 sEndDate 则代表数据达到了这个日期的分片后后循环从开始分片插入; 如果没配置则按每sPartionDay天继续切分。
	 */
	private String sEndDate;
	/**
	 * 每分片天数, 即默认从开始日期算起，分隔 x 天一个分区
	 */
	private String sPartionDay;
	/**
	 * 日期格式
	 */
	private String dateFormat;

	private long beginDate;
	private long partionTime;
	private long endDate;
	private int nCount;

	private ThreadLocal<SimpleDateFormat> formatter;
	
	private static final long oneDay = 86400000;

	@Override
	public void init() {
		try {
			partionTime = Integer.parseInt(sPartionDay) * oneDay;
			
			beginDate = new SimpleDateFormat(dateFormat).parse(sBeginDate).getTime();

			if(sEndDate!=null&&!sEndDate.equals("")){
			    endDate = new SimpleDateFormat(dateFormat).parse(sEndDate).getTime();
			    nCount = (int) ((endDate - beginDate) / partionTime) + 1;
			}
			formatter = new ThreadLocal<SimpleDateFormat>() {
				@Override
				protected SimpleDateFormat initialValue() {
					return new SimpleDateFormat(dateFormat);
				}
			};
		} catch (ParseException e) {
			throw new java.lang.IllegalArgumentException(e);
		}
	}

	@Override
	public Integer calculate(String columnValue)  {
		try {
			long targetTime = formatter.get().parse(columnValue).getTime();
			int targetPartition = (int) ((targetTime - beginDate) / partionTime);
			if(targetTime>endDate && nCount!=0){
				targetPartition = targetPartition%nCount;
			}
			return targetPartition;
		} catch (ParseException e) {
			throw new IllegalArgumentException(
					new StringBuilder()
					.append("columnValue:")
					.append(columnValue)
					.append(" Please check if the format satisfied.")
					.toString(),e);
		}
	}

	@Override
	public Integer[] calculateRange(String beginValue, String endValue)  {
		SimpleDateFormat format = new SimpleDateFormat(this.dateFormat);
		try {
			Date beginDate = format.parse(beginValue);
			Date endDate = format.parse(endValue);
			Calendar cal = Calendar.getInstance();
			List<Integer> list = new ArrayList<Integer>();
			while(beginDate.getTime() <= endDate.getTime()){
				Integer nodeValue = this.calculate(format.format(beginDate));
				if(Collections.frequency(list, nodeValue) < 1) list.add(nodeValue);
				cal.setTime(beginDate);
				cal.add(Calendar.DATE, 1);
				beginDate = cal.getTime();
			}
			Integer[] nodeArray = new Integer[list.size()];
			for (int i=0;i<list.size();i++) {
				nodeArray[i] = list.get(i);
			}
			return nodeArray;
		} catch (ParseException e) {
			LOGGER.error("error",e);
			return new Integer[0];
		}
	}
	
	@Override
	public int getPartitionNum() {
		int count = this.nCount;
		return count > 0 ? count : -1;
	}

	public void setsBeginDate(String sBeginDate) {
		this.sBeginDate = sBeginDate;
	}

	public void setsPartionDay(String sPartionDay) {
		this.sPartionDay = sPartionDay;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public String getsEndDate() {
		return this.sEndDate;
	}
	public void setsEndDate(String sEndDate) {
		this.sEndDate = sEndDate;
	}

	public static void main(String[] args) {
		String bd = "2017-08-01 13:33:12";
		String ed = "2017-12-31 23:59:59";
		PartitionByDate pd = new PartitionByDate();
		pd.setsBeginDate(bd);
		pd.setsEndDate(ed);
		pd.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pd.setsPartionDay("30");
		pd.init();
		int result = pd.calculate("2018-08-31 13:33:12");
		Integer[] range = pd.calculateRange(bd, ed);
		System.out.println(">>> Total Partition: "+pd.getPartitionNum());
		System.out.println(">>> Current Partition: "+result);
		System.out.println(">>> Partition Info:");
		for(Integer i : range){
			System.out.println("\t> Partition " + i);
		}
	}
}
