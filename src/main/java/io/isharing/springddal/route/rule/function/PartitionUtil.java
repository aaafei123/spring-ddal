package io.isharing.springddal.route.rule.function;

/**
 * 数据分区工具
 */
public final class PartitionUtil {

    // 分区长度:数据段分布定义，其中取模的数一定要是2^n， 因为这里使用x % 2^n == x & (2^n - 1)等式，来优化性能。
    private static final int PARTITION_LENGTH = 1024;

    // %转换为&操作的换算数值
    private static final long AND_VALUE = PARTITION_LENGTH - 1;

    // 分区线段
    private final int[] segment = new int[PARTITION_LENGTH];

    /**
     * <pre>
     * @param count 表示定义的分区数
     * @param length 表示对应每个分区的取值长度
     * 注意：其中count,length两个数组的长度必须是一致的。
     * 约束：1024 = sum((count[i]*length[i]))。 count和length两个向量的点积恒等于1024
     * </pre>
     */
    public PartitionUtil(int[] count, int[] length) {
        if (count == null || length == null || (count.length != length.length)) {
            throw new RuntimeException("error,check your scope & scopeLength definition.");
        }
        int segmentLength = 0;
        for (int i = 0; i < count.length; i++) {
            segmentLength += count[i];
        }
        int[] ai = new int[segmentLength + 1];

        int index = 0;
        for (int i = 0; i < count.length; i++) {
            for (int j = 0; j < count[i]; j++) {
                ai[++index] = ai[index - 1] + length[i];
            }
        }
        if (ai[ai.length - 1] != PARTITION_LENGTH) {
            throw new RuntimeException("error,check your partitionScope definition.");
        }

        // 数据映射操作
        for (int i = 1; i < ai.length; i++) {
            for (int j = ai[i - 1]; j < ai[i]; j++) {
                segment[j] = (i - 1);
            }
        }
    }

    public int partition(long hash) {
        return segment[(int) (hash & AND_VALUE)];
    }

    public int partition(String key, int start, int end) {
        return partition(hash(key, start, end));
    }
    
	/**
	 * 字符串hash算法：s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1] <br>
	 * 其中s[]为字符串的字符数组，换算成程序的表达式为：<br>
	 * h = 31*h + s.charAt(i); => h = (h << 5) - h + s.charAt(i); <br>
	 *
	 * @param start
	 *            hash for s.substring(start, end)
	 * @param end
	 *            hash for s.substring(start, end)
	 */
	public static long hash(String s, int start, int end) {
		if (start < 0) {
			start = 0;
		}
		if (end > s.length()) {
			end = s.length();
		}
		long h = 0;
		for (int i = start; i < end; ++i) {
			h = (h << 5) - h + s.charAt(i);
		}
		return h;
	}

	public static void main(String[] args) {
		// 本例的分区策略：希望将数据水平分成3份，前两份各占25%，第三份占50%。（故本例非均匀分区）
	    // |<---------------------1024------------------------>|
	    // |<----256--->|<----256--->|<----------512---------->|
	    // | partition0 | partition1 | partition2 |
	    // | 共2份,故count[0]=2 | 共1份，故count[1]=1 |
	    int[] count = new int[] { 2, 1 };
	    int[] length = new int[] { 256, 512 };
	    PartitionUtil pu = new PartitionUtil(count, length);

	    // 下面代码演示分别以offerId字段或memberId字段根据上述分区策略拆分的分配结果
	    int DEFAULT_STR_HEAD_LEN = 8; // cobar默认会配置为此值
	    long offerId = 12345;
	    String memberId = "qiushuo";

	    // 若根据offerId分配，partNo1将等于0，即按照上述分区策略，offerId为12345时将会被分配到partition0中
	    int partNo1 = pu.partition(offerId);

	    // 若根据memberId分配，partNo2将等于2，即按照上述分区策略，memberId为qiushuo时将会被分到partition2中
	    int partNo2 = pu.partition(memberId, 0, DEFAULT_STR_HEAD_LEN);

//	    Assert.assertEquals(0, partNo1);
//	    Assert.assertEquals(2, partNo2);

	    System.out.println(partNo1);
	    System.out.println(partNo2);
	}
}