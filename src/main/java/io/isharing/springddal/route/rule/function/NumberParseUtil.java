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

public class NumberParseUtil {
	/**
	 * 只去除开头结尾的引号，而且是结对去除，语法不对的话通不过
	 * @param number
	 * @return
     */
	public static String eliminateQoute(String number){
		number = number.trim();
		if(number.contains("\"")){
			if(number.charAt(0)=='\"'){
				number = number.substring(1);
				if(number.charAt(number.length()-1)=='\"'){
					number = number.substring(0,number.length()-1);
				}
			}
		}else if(number.contains("\'")){
			if(number.charAt(0)=='\''){
				number = number.substring(1);
				if(number.charAt(number.length()-1)=='\''){
					number = number.substring(0,number.length()-1);
				}
			}
		}
		return number;
	}

	/**
	 * can parse values like 200M ,200K,200M1(2000001)
	 * 
	 * @param val
	 * @return
	 */
	public static long parseLong(String val) {
		val = val.toUpperCase();
		int indx = val.indexOf("M");

		int plus = 10000;
		if (indx < 0) {
			indx = val.indexOf("K");
			plus = 1000;
		}
		if (indx > 0) {
			String longVal = val.substring(0, indx);

			long theVale = Long.parseLong(longVal) * plus;
			String remain = val.substring(indx + 1);
			if (remain.length() > 0) {
				theVale += Integer.parseInt(remain);
			}
			return theVale;
		} else {
			return Long.parseLong(val);
		}

	}
}