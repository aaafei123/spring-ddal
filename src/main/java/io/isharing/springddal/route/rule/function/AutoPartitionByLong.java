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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import io.isharing.springddal.route.rule.RuleAlgorithm;

/**
 * 按Long值范围拆分，范围值可在mapFile（autopartition-long.txt）中定义。
 */
public class AutoPartitionByLong extends AbstractPartitionAlgorithm implements RuleAlgorithm {

	private String mapFile;
	private LongRange[] longRongs;
	private String defaultNode;

	private int _partDefaultNode = -1;

	@Override
	public void init() {
		initialize();
	}

	public void setMapFile(String mapFile) {
		this.mapFile = mapFile;
	}

	@Override
	public Integer calculate(String columnValue) {
		// columnValue = NumberParseUtil.eliminateQoute(columnValue);
		try {
			long value = Long.parseLong(columnValue);
			Integer rst = null;
			for (LongRange longRang : this.longRongs) {
				if (value <= longRang.valueEnd && value >= longRang.valueStart) {
					return longRang.nodeIndx;
				}
			}
			// 数据超过范围，暂时使用配置的默认节点
			if (rst == null && _partDefaultNode >= 0) {
				return _partDefaultNode;
			}
			return rst;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					new StringBuilder()
					.append("columnValue:")
					.append(columnValue)
					.append(" Please eliminate any quote and non number within it.")
					.toString(), e);
		}
	}

	@Override
	public Integer[] calculateRange(String beginValue, String endValue) {
		return AbstractPartitionAlgorithm.calculateSequenceRange(this, beginValue, endValue);
	}

	@Override
	public int getPartitionNum() {
		// int nPartition = longRongs.length;

		/*
		 * fix #1284 这里的统计应该统计Range的nodeIndex的distinct总数
		 */
		Set<Integer> distNodeIdxSet = new HashSet<Integer>();
		for (LongRange range : longRongs) {
			distNodeIdxSet.add(range.nodeIndx);
		}
		int nPartition = distNodeIdxSet.size();
		return nPartition;
	}

	private void initialize() {
		BufferedReader in = null;
		try {
			if(StringUtils.isBlank(mapFile)) {
				setMapFile("autopartition-long.txt");
			}
			InputStream fin = this.getClass().getClassLoader().getResourceAsStream(mapFile);
			if (fin == null) {
				throw new RuntimeException("can't find class resource file " + mapFile);
			}
			in = new BufferedReader(new InputStreamReader(fin));
			LinkedList<LongRange> longRangeList = new LinkedList<LongRange>();

			for (String line = null; (line = in.readLine()) != null;) {
				line = line.trim();
				if (line.startsWith("#") || line.startsWith("//")) {
					continue;
				}
				int ind = line.indexOf('=');
				if (ind < 0) {
					System.out.println(" warn: bad line int " + mapFile + " :" + line);
					continue;
				}
				String pairs[] = line.substring(0, ind).trim().split("-");
				long longStart = NumberParseUtil.parseLong(pairs[0].trim());
				long longEnd = NumberParseUtil.parseLong(pairs[1].trim());
				int nodeId = Integer.parseInt(line.substring(ind + 1).trim());
				longRangeList.add(new LongRange(nodeId, longStart, longEnd));
			}
			longRongs = longRangeList.toArray(new LongRange[longRangeList.size()]);
			_partDefaultNode = getDataNodeNoFromDNName(defaultNode);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}

		} finally {
			try {
				in.close();
			} catch (Exception e2) {
			}
		}
	}
	
	private static int getDataNodeNoFromDNName(String dnName){
		String regEx="[^0-9]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(dnName);
		String result = m.replaceAll("").trim();
		return Integer.parseInt(result);
	}

	public String getDefaultNode() {
		return defaultNode;
	}

	public void setDefaultNode(String defaultNode) {
		this.defaultNode = defaultNode;
	}

	public int get_partDefaultNode() {
		return _partDefaultNode;
	}

	public void set_partDefaultNode(int _partDefaultNode) {
		this._partDefaultNode = _partDefaultNode;
	}

	static class LongRange {
		public final int nodeIndx;
		public final long valueStart;
		public final long valueEnd;

		public LongRange(int nodeIndx, long valueStart, long valueEnd) {
			super();
			this.nodeIndx = nodeIndx;
			this.valueStart = valueStart;
			this.valueEnd = valueEnd;
		}
	}
	
	public static void main(String[] args) {
		AutoPartitionByLong apl = new AutoPartitionByLong();
//		apl.setMapFile("autopartition-long.txt");
		apl.setDefaultNode("dn1");
		apl.init();
		int result = apl.calculate("20");
		System.out.println(">>> Total Partition: "+apl.getPartitionNum());
		System.out.println(">>> Current Partition: "+result);
		System.out.println(">>> Partition Info:");
		Integer[] range = apl.calculateRange("1", "20");
		for(Integer i : range){
			System.out.println("\t> Partition " + i);
		}
	}
}