package io.isharing.springddal.route.rule.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isharing.commons.string.StringUtils;

import io.isharing.springddal.route.rule.RuleAlgorithm;

/**
 * partitionCount: 分片个数列表<br />
 * partitionLength: 分片范围列表<br />
 * 分区长度: 默认为最大2^n=1024 ,即最大支持1024分区<br />
 * 约束 :<br />
 * 		count,length两个数组的长度必须是一致的。<br />
 * 		1024 = sum((count[i]*length[i]))。 <br />
 * 		count和length两个向量的点积恒等于1024<br />
 * 用法例子：<br />
 * public void testPartitionByLong() {<br />
 *     // 本例的分区策略：希望将数据水平分成3份，前两份各占25%，第三份占50%。（故本例非均匀分区）<br />
 *     // |<---------------------1024------------------------>|<br />
 *     // |<----256--->|<----256--->|<----------512---------->|<br />
 *     // | partition0 | partition1 | partition2 |<br />
 *     // | 共2份,故count[0]=2 | 共1份，故count[1]=1 |<br />
 *     int[] count = new int[] { 2, 1 };<br />
 *     int[] length = new int[] { 256, 512 };<br />
 *     PartitionUtil pu = new PartitionUtil(count, length);<br /><br />
 * 
 *     // 下面代码演示分别以offerId字段或memberId字段根据上述分区策略拆分的分配结果<br />
 *     int DEFAULT_STR_HEAD_LEN = 8; // cobar默认会配置为此值<br />
 *     long offerId = 12345;<br />
 *     String memberId = "qiushuo";<br /><br />
 * 
 *     // 若根据offerId分配，partNo1将等于0，即按照上述分区策略，offerId为12345时将会被分配到partition0中<br />
 *     int partNo1 = pu.partition(offerId);<br /><br />
 * 
 *     // 若根据memberId分配，partNo2将等于2，即按照上述分区策略，memberId为qiushuo时将会被分到partition2中<br />
 *     int partNo2 = pu.partition(memberId, 0, DEFAULT_STR_HEAD_LEN);<br /><br />
 * 
 *     Assert.assertEquals(0, partNo1);<br />
 *     Assert.assertEquals(2, partNo2);<br />
 * }<br />
 */
public final class PartitionByLong extends AbstractPartitionAlgorithm implements RuleAlgorithm {

	private static final long serialVersionUID = 1043719201606373967L;
	private static final Logger LOGGER = LoggerFactory.getLogger(PartitionByLong.class);

	protected int[] count;
	protected int[] length;
	protected PartitionUtil partitionUtil;

	private static int[] toIntArray(String string) {
		String[] strs = StringUtils.split(string, ',');
		int[] ints = new int[strs.length];
		for (int i = 0; i < strs.length; ++i) {
			ints[i] = Integer.parseInt(strs[i]);
		}
		return ints;
	}

	public void setPartitionCount(String partitionCount) {
		this.count = toIntArray(partitionCount);
	}

	public void setPartitionLength(String partitionLength) {
		this.length = toIntArray(partitionLength);
	}

	@Override
	public void init() {
		partitionUtil = new PartitionUtil(count, length);
	}

	@Override
	public Integer calculate(String columnValue) {
		// columnValue = NumberParseUtil.eliminateQoute(columnValue);
		try {
			long key = Long.parseLong(columnValue);
			return partitionUtil.partition(key);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(new StringBuilder().append("columnValue:").append(columnValue)
					.append(" Please eliminate any quote and non number within it.").toString(), e);
		}
	}

	@Override
	public Integer[] calculateRange(String beginValue, String endValue) {
		return AbstractPartitionAlgorithm.calculateSequenceRange(this, beginValue, endValue);
	}

	// @Override
	// public int getPartitionCount() {
	// int nPartition = 0;
	// for(int i = 0; i < count.length; i++) {
	// nPartition += count[i];
	// }
	// return nPartition;
	// }

	public static void main(String[] args) {
		PartitionByLong pl = new PartitionByLong();
		pl.setPartitionLength("4");
		pl.setPartitionCount("256");
		pl.init();
		int result = pl.calculate("128");
		System.out.println(">>> Total Partition: "+pl.getPartitionNum());
		System.out.println(">>> Current Partition: "+result);
		System.out.println(">>> Partition Info:");
		Integer[] range = pl.calculateRange("1", "9");
		for(Integer i : range){
			System.out.println("\t> Partition " + i);
		}
	}
}