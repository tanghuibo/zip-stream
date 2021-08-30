import io.github.tanghuibo.ZipStreamEntry;
import io.github.tanghuibo.ZipStreamOutputStream;
import org.junit.Test;

import java.io.*;

/**
 * ZipStreamTest
 *
 * @author tanghuibo
 * @date 2021/8/30 20:24
 */
public class ZipStreamTest {

    @Test
    public void fileEntity() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("d://666.zip");
        ZipStreamOutputStream myZipOutputStream = new ZipStreamOutputStream(fileOutputStream);
        FileInputStream byteArrayInputStream1 = new FileInputStream("d://555.zip");
        ZipStreamEntry myZipEntity1 = new ZipStreamEntry(byteArrayInputStream1, "test.zip", (long) byteArrayInputStream1.available());
        myZipOutputStream.put(myZipEntity1);
        myZipOutputStream.finish();
        fileOutputStream.flush();
    }

    @Test
    public void oneEntity() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("d://555.zip");
        ZipStreamOutputStream myZipOutputStream = new ZipStreamOutputStream(fileOutputStream);
        byte[] bytes1 = new byte[65532];
        for (int i = 0; i < bytes1.length; i++) {
            bytes1[i] = (byte) ( (i % 10) + 65);
        }
        ByteArrayInputStream byteArrayInputStream1 = new ByteArrayInputStream(bytes1);
        ZipStreamEntry myZipEntity1 = new ZipStreamEntry(byteArrayInputStream1, "test1.txt", (long) bytes1.length);
        myZipOutputStream.put(myZipEntity1);
        myZipOutputStream.finish();
        fileOutputStream.flush();
    }

    @Test
    public void twoEntity() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("d://999.zip");
        ZipStreamOutputStream myZipOutputStream = new ZipStreamOutputStream(fileOutputStream);
        byte[] bytes1 = new byte[20];
        for (int i = 0; i < bytes1.length; i++) {
            bytes1[i] = (byte) ( (i % 10) + 65);
        }
        ByteArrayInputStream byteArrayInputStream1 = new ByteArrayInputStream(bytes1);
        ZipStreamEntry myZipEntity1 = new ZipStreamEntry(byteArrayInputStream1, "test1.txt", (long) bytes1.length);
        myZipOutputStream.put(myZipEntity1);


        byte[] bytes2 = new byte[60];
        for (int i = 0; i < bytes2.length; i++) {
            bytes2[i] = (byte) ( (i % 10) + 70);
        }
        ByteArrayInputStream byteArrayInputStream2 = new ByteArrayInputStream(bytes2);
        ZipStreamEntry myZipEntity2 = new ZipStreamEntry(byteArrayInputStream2, "test2.txt", (long) bytes2.length);
        myZipOutputStream.put(myZipEntity2);


        myZipOutputStream.finish();
        fileOutputStream.flush();
    }
}
