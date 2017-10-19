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
package io.isharing.springddal.route.rule.conf;

public class DataNode {
	
	private String nodeName;
	private String writeNodes;
	private String readNodes;
//	private boolean defaultWriteNode;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getWriteNodes() {
		return writeNodes;
	}

	public void setWriteNodes(String writeNodes) {
		this.writeNodes = writeNodes;
	}

	public String getReadNodes() {
		return readNodes;
	}

	public void setReadNodes(String readNodes) {
		this.readNodes = readNodes;
	}

//	public boolean isDefaultWriteNode() {
//		return defaultWriteNode;
//	}
//
//	public void setDefaultWriteNode(boolean defaultWriteNode) {
//		this.defaultWriteNode = defaultWriteNode;
//	}

	@Override
	public String toString() {
		return "DataNode [writeNodes=" + writeNodes + ", readNodes=" + readNodes + "]";
	}
}
