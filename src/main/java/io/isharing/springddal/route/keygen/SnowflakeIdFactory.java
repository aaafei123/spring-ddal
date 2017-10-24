package io.isharing.springddal.route.keygen;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnowflakeIdFactory {
	
	private static final Logger log = LoggerFactory.getLogger(SnowflakeIdFactory.class);

	private static final long TWEPOCH;
	// 毫秒内自增位
    private final long SEQUENCE_BITS = 12L;
    // 机器标识位数
    private final long WORKER_ID_BITS = 5L;
    // 数据中心标识位数 
    private final long DATA_CENTER_ID_BITS = 5L;
    
    private final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);//4095
    
    // 机器ID最大值 31
    private final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);//31
    // 数据中心ID最大值 31
    private final long MAX_DATA_CENTER_ID = -1L ^ (-1L << DATA_CENTER_ID_BITS);//31
    
    // 机器ID偏左移12位
    private final long WORKER_ID_SHIFT = SEQUENCE_BITS;//12
    private final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;//17
    private final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;//22

    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, Calendar.JANUARY, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        TWEPOCH = calendar.getTimeInMillis();
    }

    public SnowflakeIdFactory(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATA_CENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long generateKey() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            //服务器时钟被调整了,ID生成器停止服务.
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;
        return ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT) | (datacenterId << DATA_CENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

}