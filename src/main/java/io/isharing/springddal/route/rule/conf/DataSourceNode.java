package io.isharing.springddal.route.rule.conf;

import java.util.List;

public class DataSourceNode {
	
	private List<String> writeNodesNameList;
	private List<String> readNodesNameList;
	
//	private List<DataSource> writeNodes;
//	private List<DataSource> readNodes;

	public DataSourceNode(List<String> writeNodesNameList, List<String> readNodesNameList) {
		this.writeNodesNameList = writeNodesNameList;
		this.readNodesNameList = readNodesNameList;
	}
	
//	public DataSourceNode(List<DataSource> writeNodes, List<DataSource> readNodes) {
//		this.writeNodes = writeNodes;
//		this.readNodes = readNodes;
//	}

	public List<String> getWriteNodesNameList() {
		return writeNodesNameList;
	}

	public List<String> getReadNodesNameList() {
		return readNodesNameList;
	}

//	public List<DataSource> getWriteNodes() {
//		return writeNodes;
//	}
//
//	public List<DataSource> getReadNodes() {
//		return readNodes;
//	}
}
