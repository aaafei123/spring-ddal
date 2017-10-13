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
import java.util.List;

import org.apache.log4j.Logger;

import io.isharing.springddal.route.rule.RuleAlgorithm;

/**
 * 例子 按月份列分区 ，每个自然月一个分片，格式 between操作解析的范例
 * 每个月存入每个月单独的数据库中
 * 
 */
public class PartitionByMonth extends AbstractPartitionAlgorithm implements RuleAlgorithm {

	private static final long serialVersionUID = 4208799852149537498L;
	private static final Logger LOGGER = Logger.getLogger(PartitionByDate.class);
	
	/**
	 * 开始日期
	 */
	private String sBeginDate;
	/**
	 * 结束日期, 如果配置了 sEndDate 则代表数据达到了这个日期的分片后后循环从开始分片插入; 
	 * 如果没配置则按每一年的月份继续切分（即如开始时间为2017年1月，传入的时间为2018年12月，则分区为23）。
	 */
	private String sEndDate;
	/**
	 * 日期格式
	 */
	private String dateFormat;
	
	private Calendar beginDate;
	private Calendar endDate;
	private int nPartition;

	private ThreadLocal<SimpleDateFormat> formatter;

	@Override
	public void init() {
		try {
			beginDate = Calendar.getInstance();
			beginDate.setTime(new SimpleDateFormat(dateFormat)
					.parse(sBeginDate));
			formatter = new ThreadLocal<SimpleDateFormat>() {
				@Override
				protected SimpleDateFormat initialValue() {
					return new SimpleDateFormat(dateFormat);
				}
			};
			if(sEndDate!=null&&!sEndDate.equals("")) {
				endDate = Calendar.getInstance();
				endDate.setTime(new SimpleDateFormat(dateFormat).parse(sEndDate));
				nPartition = ((endDate.get(Calendar.YEAR) - beginDate.get(Calendar.YEAR)) * 12
								+ endDate.get(Calendar.MONTH) - beginDate.get(Calendar.MONTH)) + 1;

				if (nPartition <= 0) {
					throw new java.lang.IllegalArgumentException("Incorrect time range for month partitioning!");
				}
			} else {
				nPartition = -1;
			}
		} catch (ParseException e) {
			throw new java.lang.IllegalArgumentException(e);
		}
	}

	/**
	 * For circulatory partition, calculated value of target partition needs to be
	 * rotated to fit the partition range
	 */
	private int reCalculatePartition(int targetPartition) {
		/**
		 * If target date is previous of start time of partition setting, shift
		 * the delta range between target and start date to be positive value
		 */
		if (targetPartition < 0) {
			targetPartition = nPartition - (-targetPartition) % nPartition;
		}

		if (targetPartition >= nPartition) {
			targetPartition =  targetPartition % nPartition;
		}

		return targetPartition;
	}

	@Override
	public Integer calculate(String columnValue)  {
		try {
			int targetPartition;
			Calendar curTime = Calendar.getInstance();
			curTime.setTime(formatter.get().parse(columnValue));
			targetPartition = ((curTime.get(Calendar.YEAR) - beginDate.get(Calendar.YEAR))
					* 12 + curTime.get(Calendar.MONTH)
					- beginDate.get(Calendar.MONTH));

			/**
			 * For circulatory partition, calculated value of target partition needs to be
			 * rotated to fit the partition range
 			 */
			if (nPartition > 0) {
				targetPartition = reCalculatePartition(targetPartition);
			}
			return targetPartition;

		} catch (ParseException e) {
			throw new IllegalArgumentException(new StringBuilder().append("columnValue:").append(columnValue).append(" Please check if the format satisfied.").toString(),e);
		}
	}

	@Override
	public Integer[] calculateRange(String beginValue, String endValue) {
		try {
			int startPartition, endPartition;
			Calendar partitionTime = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat(dateFormat);
			partitionTime.setTime(format.parse(beginValue));
			startPartition = ((partitionTime.get(Calendar.YEAR) - beginDate.get(Calendar.YEAR))
					* 12 + partitionTime.get(Calendar.MONTH)
					- beginDate.get(Calendar.MONTH));
			partitionTime.setTime(format.parse(endValue));
			endPartition = ((partitionTime.get(Calendar.YEAR) - beginDate.get(Calendar.YEAR))
					* 12 + partitionTime.get(Calendar.MONTH)
					- beginDate.get(Calendar.MONTH));

			List<Integer> list = new ArrayList<Integer>();

			while (startPartition <= endPartition) {
				Integer nodeValue = reCalculatePartition(startPartition);
				if (Collections.frequency(list, nodeValue) < 1)
					list.add(nodeValue);
				startPartition++;
			}
			int size = list.size();
			return (list.toArray(new Integer[size]));
		} catch (ParseException e) {
			LOGGER.error("error",e);
			return new Integer[0];
		}
	}
	
	@Override
	public int getPartitionNum() {
		int nPartition = this.nPartition;
		return nPartition;
	}

	public void setsBeginDate(String sBeginDate) {
		this.sBeginDate = sBeginDate;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void setsEndDate(String sEndDate) {
		this.sEndDate = sEndDate;
	}

	public static void main(String[] args) {
		String bd = "2017-08-01 13:33:12";
		String ed = "2017-12-31 23:59:59";
		PartitionByMonth pm = new PartitionByMonth();
		pm.setsBeginDate(bd);
//		pm.setsEndDate(ed);
		pm.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pm.init();
		int result = pm.calculate("2018-12-01 13:33:12");
		Integer[] range = pm.calculateRange(bd, ed);
		System.out.println(">>> Total Partition: "+pm.getPartitionNum());
		System.out.println(">>> Current Partition: "+result);
		System.out.println(">>> Partition Info:");
		for(Integer i : range){
			System.out.println("\t> Partition " + i);
		}
	}
}
