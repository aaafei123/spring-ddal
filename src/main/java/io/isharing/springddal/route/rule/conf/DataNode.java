package io.isharing.springddal.route.rule.conf;

public class DataNode {
	
	private String nodeName;
	private String writeNodes;
	private String readNodes;
	private boolean defaultWriteNode;

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

	public boolean isDefaultWriteNode() {
		return defaultWriteNode;
	}

	public void setDefaultWriteNode(boolean defaultWriteNode) {
		this.defaultWriteNode = defaultWriteNode;
	}

	@Override
	public String toString() {
		return "DataNode [writeNodes=" + writeNodes + ", readNodes=" + readNodes + ", mainWriteNode="+defaultWriteNode+"]";
	}
}
