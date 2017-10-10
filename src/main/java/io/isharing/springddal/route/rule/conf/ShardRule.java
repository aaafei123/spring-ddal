package io.isharing.springddal.route.rule.conf;

public class ShardRule {

	private String tableName;
	private String column;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	@Override
	public String toString() {
		return "ShardRule [tableName=" + tableName + ", column=" + column + "]";
	}
}
