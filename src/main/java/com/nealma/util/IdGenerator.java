package com.nealma.util;

/**
 * Created by neal.ma on 3/29/16.
 * 在分布式系统中，需要生成全局UID的场合比较多，twitter的snowflake解决了这种需求
 * 核心代码 毫秒级时间 41位 ＋ 机器ID 10位 ＋ 毫秒内序列 12位
 */
public class IdGenerator {

    /**
     * twitter snowflake 64 bit
     * 第一位为未使用（实际上也可作为long的符号位），接下来的41位为毫秒级时间，然后5位datacenter标识位，5位机器ID（并不算标识符，实际是为线程标识），然后12位该毫秒内的当前毫秒内的计数，加起来刚好64位，为一个Long型。
     * 0---0000000000 0000000000 0000000000 0000000000 0 --- 00000 ---00000 ---000000000000
     *
     */

    private static long workerId;
    private static long datacenterId;
    private final static short dataCenterIdBits = 5;
    private final static short workerIdBits = 5;
    private final static short sequenceBits = 12;
    private static long sequence = 0L;
    private static long maxWorkerId = -1L ^ -1L << workerIdBits;
    private static long maxDatacenterId = -1L ^ -1L << dataCenterIdBits;
    private static long lastTimestamp = 0L;
    private static short timestampLeftShift = (short) (dataCenterIdBits + workerIdBits + sequenceBits);
    private static short workerIdLeftShift = sequenceBits;
    private static short datacenterIdLeftShift = (short) (workerIdBits + sequenceBits);
    public final static long sequenceMask = -1L ^ -1L << sequenceBits;

    public IdGenerator(final long datacenterId, final long workerId) {
        super();
        if(workerId > this.maxWorkerId || workerId < 0){
            throw new IllegalArgumentException(String.format("worker id can't be greater then %d or less then 0", this.maxWorkerId));
        }
        this.workerId = workerId;

        if(datacenterId > this.maxDatacenterId || datacenterId < 0){
            throw new IllegalArgumentException(String.format("datacenter id can't be greater then %d or less then 0", this.datacenterId));
        }
        this.datacenterId = datacenterId;
    }


    private long timestamp(){
        return System.currentTimeMillis();
    }
    private long timeGen(){
        return this.lastTimestamp << timestampLeftShift;
    }
    private long workerIdGen(){
        return this.workerId << workerIdLeftShift;
    }
    private long datacenterIdGen(){
        return this.datacenterId << datacenterIdLeftShift;
    }

    private long idGen(){
        System.out.println(String.format("%d, %d, %d, %d", this.lastTimestamp, this.datacenterId, this.workerId, this.sequence));
        return timeGen() | datacenterIdGen() | workerIdGen() | this.sequence;
    }

    public synchronized long nextId(){
        long timestamp = timestamp();
        if(this.lastTimestamp == timestamp){
            this.sequence = (this.sequence + 1) & this.sequenceMask;
            if(this.sequence == 0L){
                timestamp = tillNextMills(this.lastTimestamp);
            }
        }

        if(timestamp < this.lastTimestamp){
            try {
                throw new Exception(String.format("Clock moved backwards, pls refresh id gen %d milliseconds", this.lastTimestamp - timestamp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.lastTimestamp = timestamp;

        return idGen();
    }

    private long tillNextMills(final long lastTimestamp) {
        long timestamp = timestamp();
        while (timestamp <= lastTimestamp){
            timestamp = timestamp();
        }
        return timestamp;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1l, 1l);
        for (int i = 0;  i < 100;  i++) {
            System.out.println(idGenerator.nextId());
        }
    }
}
