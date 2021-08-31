package io.github.tanghuibo;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tanghuibo
 * @date 2021/8/30上午12:58
 */
public class ZipStreamOutputStream {

    List<ZipXEntity> zipXEntityList = new ArrayList<>();

    static long LOCSIG = 0x04034b50L;   // "PK\003\004"
    static long EXTSIG = 0x08074b50L;   // "PK\007\008"
    static long ENDSIG = 0x06054b50L;   // "PK\005\006"
    static long CENSIG = 0x02014b50L;

    OutputStream out;
    private int written = 0;

    public ZipStreamOutputStream(OutputStream out) {
        this.out = out;
    }

    public void writeLOC(ZipStreamEntry e) throws IOException {
        int flag = e.getFlag();
//        int elen = getExtraLen(e.extra);
        int elen = 0;

        writeInt(LOCSIG);               // LOC header signature
        writeShort(version(e));     // version needed to extract
        writeShort(flag);           // general purpose bit flag
        writeShort(e.getMethod());       // compression method
        writeInt(e.getXdostime());       // last modification time
        // store size, uncompressed size, and crc-32 in data descriptor
        // immediately following compressed entry data
        writeInt(0);
        writeInt(0);
        writeInt(0);
        byte[] nameBytes = e.getName().getBytes(StandardCharsets.UTF_8);
        writeShort(nameBytes.length);
        writeShort(elen);
        writeBytes(nameBytes, 0, nameBytes.length);
    }

    public void closeEntry(ZipStreamEntry e) throws IOException {
        long allSize = e.getSize();
        long readLength = 0;
        byte[] bytes = new byte[65531];
        while (readLength < allSize) {
            int newReadLength = e.readData(bytes);
            if(newReadLength == 0) {
                throw new RuntimeException("数据大小错误");
            }
            long oldReadLength = readLength;
            readLength = readLength + newReadLength;
            long oldReadCount = oldReadLength == 0 ? -1 : (oldReadLength -1) / 65531;
            long newReadCount = (readLength -1) / 65531;
            if(newReadCount > oldReadCount) {
                int stopPoint = (int) (65531 * newReadCount - oldReadLength);
                if(stopPoint != 0) {
                    writeBytes(bytes, 0, stopPoint);
                }
                long signLength = Math.min(65531L, allSize - newReadCount * 65531L);
                writeBytes(getSignByte(signLength), 0, 5);
                if(stopPoint != newReadLength) {
                    writeBytes(bytes, stopPoint, newReadLength - stopPoint);
                }
            } else {
                writeBytes(bytes, 0, newReadLength);
            }
        }
        if(allSize % 65531 == 0) {
            writeBytes(getSignByte(0), 0, 5);
        }
        writeEXT(e);
    }

    private byte[] getSignByte(long length) {
        if(length == 65531) {
            return new byte[] {0, (byte) 251, (byte) 255, 4, 0};
        }
        byte a0 = 1;
        long a1 = length % 256;
        long a2 = (length /256) % 256;
        long a3 = 255 - a1;
        long a4 = 255 - a2;
        return new byte[] {a0, (byte) a1, (byte) a2, (byte) a3, (byte) a4};
    }


    private void writeEXT(ZipStreamEntry e) throws IOException {
        writeInt(EXTSIG);           // EXT header signature
        writeInt(e.getCrc());            // crc-32
        writeInt(e.getCsize());          // compressed size
        writeInt(e.getSize());           // uncompressed size
    }

    private int version(ZipStreamEntry e) {
        return 20;
    }

    /*
     * Writes a 8-bit byte to the output stream.
     */
    private void writeByte(int v) throws IOException {
        OutputStream out = this.out;
        out.write(v & 0xff);
        written += 1;
    }

    /*
     * Writes a 16-bit short to the output stream in little-endian byte order.
     */
    private void writeShort(int v) throws IOException {
        OutputStream out = this.out;
        out.write((v >>> 0) & 0xff);
        out.write((v >>> 8) & 0xff);
        written += 2;
    }

    /*
     * Writes a 32-bit int to the output stream in little-endian byte order.
     */
    private void writeInt(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int)((v >>>  0) & 0xff));
        out.write((int)((v >>>  8) & 0xff));
        out.write((int)((v >>> 16) & 0xff));
        out.write((int)((v >>> 24) & 0xff));
        written += 4;
    }

    /*
     * Writes a 64-bit int to the output stream in little-endian byte order.
     */
    private void writeLong(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int)((v >>>  0) & 0xff));
        out.write((int)((v >>>  8) & 0xff));
        out.write((int)((v >>> 16) & 0xff));
        out.write((int)((v >>> 24) & 0xff));
        out.write((int)((v >>> 32) & 0xff));
        out.write((int)((v >>> 40) & 0xff));
        out.write((int)((v >>> 48) & 0xff));
        out.write((int)((v >>> 56) & 0xff));
        written += 8;
    }

    /*
     * Writes an array of bytes to the output stream.
     */
    private void writeBytes(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        written += len;
    }

    public void put(ZipStreamEntry myZipEntity) throws IOException {
        zipXEntityList.add(new ZipXEntity(written, myZipEntity));
        writeLOC(myZipEntity);
        closeEntry(myZipEntity);
    }



    /*
     * Write central directory (CEN) header for specified entry.
     * REMIND: add support for file attributes
     */
    public void writeCEN(ZipXEntity zipXEntity) throws IOException {
        ZipStreamEntry e = zipXEntity.zipEntity;
        int flag = e.getFlag();
        int version = version(e);
        long csize = e.getCsize();
        long size = e.getSize();

        writeInt(CENSIG);           // CEN header signature
        writeShort(version);    // version made by
        writeShort(version);    // version needed to extract
        writeShort(flag);           // general purpose bit flag
        writeShort(e.getMethod());       // compression method
        writeInt(e.getXdostime());       // last modification time
        writeInt(e.getCrc());            // crc-32
        writeInt(csize);            // compressed size
        writeInt(size);             // uncompressed size
        byte[] nameBytes = e.getName().getBytes(StandardCharsets.UTF_8);
        writeShort(nameBytes.length);

        int elen = 0;

        writeShort(elen);
        writeShort(0);
        writeShort(0);              // starting disk number
        writeShort(0);              // internal file attributes (unused)
        writeInt(0);                // external file attributes (unused)
        writeInt(zipXEntity.offset);           // relative offset of local header
        writeBytes(nameBytes, 0, nameBytes.length);


    }

    public void finish() throws IOException {


        // write central directory
        long off = written;
        for (ZipXEntity zipXEntity : zipXEntityList) {
            writeCEN(zipXEntity);

        }

        writeEND(off, written - off);
    }

    public void writeEND(long off, long len) throws IOException {
        long xlen = len;
        long xoff = off;
        int count = zipXEntityList.size();
        writeInt(ENDSIG);                 // END record signature
        writeShort(0);                    // number of this disk
        writeShort(0);                    // central directory start disk
        writeShort(count);                // number of directory entries on disk
        writeShort(count);                // total number of directory entries
        writeInt(xlen);                   // length of central directory
        writeInt(xoff);                   // offset of central directory
        writeShort(0);
    }
    class ZipXEntity {
        private Integer offset;

        private ZipStreamEntry zipEntity;

        public ZipXEntity(Integer offset, ZipStreamEntry zipEntity) {
            this.offset = offset;
            this.zipEntity = zipEntity;
        }
    }
}
