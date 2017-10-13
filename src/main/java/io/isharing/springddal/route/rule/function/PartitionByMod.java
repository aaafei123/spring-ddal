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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.isharing.springddal.route.rule.RuleAlgorithm;


/**
 * 取模拆分。即根据columnValue与count（节点数）进行求模计算。
 * 比如count节点数为10，那么columnValue等于0时，拆分分区为0，columnValue等于21时，拆分分区位于1 (21 % 10 =1)。
 *
 */
public class PartitionByMod extends AbstractPartitionAlgorithm implements RuleAlgorithm  {

	/**
	 * 拆分的总节点数。
	 */
	private int count;
	
	@Override
	public void init() {
	
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public Integer calculate(String columnValue)  {
//		columnValue = NumberParseUtil.eliminateQoute(columnValue);
		try {
			BigInteger bigNum = new BigInteger(columnValue).abs();
			return (bigNum.mod(BigInteger.valueOf(count))).intValue();
		} catch (NumberFormatException e){
			throw new IllegalArgumentException(new StringBuilder().append("columnValue:").append(columnValue).append(" Please eliminate any quote and non number within it.").toString(),e);
		}
	}

	@Override
	public int getPartitionNum() {
		int nPartition = this.count;
		return nPartition;
	}

	private static void hashTest()  {
		PartitionByMod hash=new PartitionByMod();
		hash.setCount(11);
		hash.init();
		
		int[] bucket=new int[hash.count];
		
		Map<Integer,List<Integer>> hashed=new HashMap<Integer,List<Integer>>();
		
		int total=1000_0000;//数据量
		int c=0;
		for(int i=100_0000;i<total+100_0000;i++){//假设分片键从100万开始
			c++;
			int h=hash.calculate(Integer.toString(i));
			bucket[h]++;
			List<Integer> list=hashed.get(h);
			if(list==null){
				list=new ArrayList<>();
				hashed.put(h, list);
			}
			list.add(i);
		}
		System.out.println(c+"   "+total);
		double d=0;
		c=0;
		int idx=0;
		System.out.println("index    bucket   ratio");
		for(int i:bucket){
			d+=i/(double)total;
			c+=i;
			System.out.println(idx+++"  "+i+"   "+(i/(double)total));
		}
		System.out.println(d+"  "+c);
		
		System.out.println("****************************************************");
		rehashTest(hashed.get(0));
	}
	
	private static void rehashTest(List<Integer> partition)  {
		PartitionByMod hash=new PartitionByMod();
		hash.count=110;//分片数
		hash.init();
		
		int[] bucket=new int[hash.count];
		
		int total=partition.size();//数据量
		int c=0;
		for(int i:partition){//假设分片键从100万开始
			c++;
			int h=hash.calculate(Integer.toString(i));
			bucket[h]++;
		}
		System.out.println(c+"   "+total);
		c=0;
		int idx=0;
		System.out.println("index    bucket   ratio");
		for(int i:bucket){
			c+=i;
			System.out.println(idx+++"  "+i+"   "+(i/(double)total));
		}
	}
	
	public static void main(String[] args)  {
//		hashTest();
		PartitionByMod partitionByMod = new PartitionByMod();
		partitionByMod.count=8;
//		partitionByMod.calculate("\"6\"");
//		partitionByMod.calculate("\'6\'");
		int result = partitionByMod.calculate("26");
		System.out.println(result);
	}
}