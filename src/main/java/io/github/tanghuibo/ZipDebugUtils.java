package io.github.tanghuibo;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author tanghuibo
 * @date 2021/8/30上午12:29
 */
public class ZipDebugUtils {

    public static void toZip(String outputPath, List<String> inputPathList) {
        try(OutputStream out = new FileOutputStream(outputPath)) {
            ZipOutputStream zipOutputStream = new ZipOutputStream(out);
            zipOutputStream.setLevel(0);
            for (String inputPath : inputPathList) {
                try(InputStream inputStream = new FileInputStream(inputPath)) {
                    String fileName = new File(inputPath).getName();
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(IOUtils.toByteArray(inputStream));
                    zipOutputStream.closeEntry();
                }
            }
            zipOutputStream.finish();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
