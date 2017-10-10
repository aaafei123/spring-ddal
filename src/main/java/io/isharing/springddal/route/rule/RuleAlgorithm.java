package io.isharing.springddal.route.rule;

public interface RuleAlgorithm {
	void init();
	Integer calculate(String columnValue) ;
	Integer[] calculateRange(String beginValue,String endValue) ;
	int getPartitionNum();
}