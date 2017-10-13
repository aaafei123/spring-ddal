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

import java.io.Serializable;

import io.isharing.springddal.route.rule.RuleAlgorithm;


/**
 * 路由分片函数抽象类
 * 为了实现一个默认的支持范围分片的函数 calcualteRange
 * 重写它以实现自己的范围路由规则
 *
 */
public abstract class AbstractPartitionAlgorithm implements RuleAlgorithm ,Serializable {

	private static final long serialVersionUID = 1775487253161848486L;

	@Override
	public void init() {
	}

	/**
	 * 返回所有被路由到的节点的编号
	 * 返回长度为0的数组表示所有节点都被路由（默认）
	 * 返回null表示没有节点被路由到
	 */
	@Override
	public Integer[] calculateRange(String beginValue, String endValue)  {
		return new Integer[0];
	}
	
	/**
	 * 对于存储数据按顺序存放的字段做范围路由，可以使用这个函数
	 * @param algorithm
	 * @param beginValue
	 * @param endValue
	 * @return
	 */
	public static Integer[] calculateSequenceRange(AbstractPartitionAlgorithm algorithm, String beginValue, String endValue)  {
		Integer begin = 0, end = 0;
		begin = algorithm.calculate(beginValue);
		end = algorithm.calculate(endValue);

		if(begin == null || end == null){
			return new Integer[0];
		}
		
		if (end >= begin) {
			int len = end-begin+1;
			Integer [] re = new Integer[len];
			for(int i =0;i<len;i++){
				re[i]=begin+i;
			}
			return re;
		}else{
			return new Integer[0];
		}
	}
	
	/**
	 * 返回分区数, 返回-1表示分区数没有限制
	 * @return
	 */
	public int getPartitionNum() {
		return -1; // 表示没有限制
	}
	
}
