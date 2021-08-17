package util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author bchristiansen
 */
public class UnzipUtility
{
    private static final int BUFFER_SIZE = 4096;

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn    The input file.
     * @param filePath The path to the output file.
     * @throws IOException If the output file cannot be opened or written.
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException
    {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1)
        {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    /**
     * Extracts a zip file specified by the zipFileP to a directory specified by
     * destDirectory (will be created if it does not exists)
     *
     * @param zipFilePath   The path and name of the zip file.
     * @param destDirectory The destination folder.
     * @throws IOException Any IO error occurring during the attempt.
     */
    public void unzip(String zipFilePath, String destDirectory) throws IOException, Exception
    {
        File destDir = new File(destDirectory);
        if (!destDir.exists())
        {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null)
        {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory())
            {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            }
            else
            {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
}