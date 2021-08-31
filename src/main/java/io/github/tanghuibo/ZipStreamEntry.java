package io.github.tanghuibo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.CRC32;

/**
 * @author tanghuibo
 * @date 2021/8/30上午12:58
 */
public class ZipStreamEntry {


    /**
     * DOS time constant for representing timestamps before 1980.
     */
    private static final long DOSTIME_BEFORE_1980 = (1 << 21) | (1 << 16);


    /**
     * 时间
     */
    private final Long xdostime;

    /**
     * 文件大小
     */
    private final Long size;

    /**
     * 文件名
     */
    private final String name;

    /**
     * 循环冗余校验
     */
    private final CRC32 crc32;

    /**
     * 输出流
     */
    private final InputStream inputStream;

    public ZipStreamEntry(InputStream inputStream, String name, Long size) {
        this.name = name;
        this.size = size;
        this.inputStream = inputStream;
        this.crc32 = new CRC32();
        xdostime = javaToExtendedDosTime(System.currentTimeMillis());
    }

    public static long javaToExtendedDosTime(long time) {
        if (time < 0) {
            return DOSTIME_BEFORE_1980;
        }
        long dostime = javaToDosTime(time);
        return (dostime != DOSTIME_BEFORE_1980)
                ? dostime + ((time % 2000) << 32)
                : DOSTIME_BEFORE_1980;
    }

    @SuppressWarnings("deprecation") // Use of date methods
    private static long javaToDosTime(long time) {
        Date d = new Date(time);
        int year = d.getYear() + 1900;
        if (year < 1980) {
            return DOSTIME_BEFORE_1980;
        }
        return (year - 1980) << 25 | (d.getMonth() + 1) << 21 |
                d.getDate() << 16 | d.getHours() << 11 | d.getMinutes() << 5 |
                d.getSeconds() >> 1;
    }

    public Long getCsize() {
        if(size == 0) {
            return 5L;
        }
        return  size + ((size / 65531 + 1) * 5);
    }

    public Long getCrc() {
        return crc32.getValue();
    }

    public Integer getFlag() {
        return 2056;
    }

    public Integer getMethod() {
        return 8;
    }

    public Long getXdostime() {
        return xdostime;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public int readData(byte[] bytes) throws IOException {
        int readLength = inputStream.read(bytes);
        crc32.update(bytes, 0, readLength);
        return readLength;
    }
}
