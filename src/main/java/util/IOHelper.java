package util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static util.Text.getRandomInteger;
import static util.Text.getRandomText;

//import org.testng.reporters.Files;
@Slf4j
public class IOHelper
{
    private static final String SEPARATOR = ",";
    private static FileWriter fw;
    private static PrintWriter pw;

    private String inputFileName;
    private String importFileName;
    private int sectionSize;
    private String header;

    // Members used for generating the temporary file.
    private ArrayList<String> rows;
    private ArrayList<String[]> data;

    // Members used to set a column of imported data to a specific value.
    private ArrayList<String> randomizeColumns;
    private HashMap<String, String> constantColumn;

    /**
     * Constructor the IOHelper class.
     *
     * @param inputFileName - This is the main input file from which the temporary import
     *                      file is generated.
     * @param sectionSize   - The number of rows wanted for an import operation. This number
     *                      cannot be more than the total size of the input file.
     */
    public IOHelper(String inputFileName, int sectionSize)
    {
        this.inputFileName = inputFileName;
        this.sectionSize = sectionSize;

        rows = new ArrayList<String>();
        data = new ArrayList<String[]>();

        randomizeColumns = new ArrayList<String>();
        constantColumn = new HashMap<String, String>();
    }

    /**
     * This method parses a tab delimited file. The first row of data is a header
     * which needs to be read in and ignored.
     *
     * @param fileName The name of the file to be read.
     * @return All the lines of text that the file contains.
     * @throws NumberFormatException If the text read could not be formatted as expected.
     * @throws IOException           If the file could not be found or processed.
     */
    public static Collection<String[]> readTabDelimFile(String fileName) throws
        NumberFormatException, IOException
    {
        ArrayList<String[]> lines = new ArrayList<String[]>();
        BufferedReader fh = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
        String s;

        // Read the first row (header) and ignore it.
        s = fh.readLine();
        while ((s = fh.readLine()) != null)
        {
            if (s.charAt(0) == '#')
            {
                break;
            }
            String f[] = s.split("\t");
            lines.add(f);
        }
        fh.close();
        return lines;
    }

    /**
     * Add a column name to the list. These column names are used to make sure that
     * the value passed in is set in the temporary file used for export testing.
     *
     * @param column The columns to be added.
     * @param value  The value that the new columns should contain.
     */
    public void addConstantColumn(String column, String value)
    {
        constantColumn.put(column, value);
    }

    /**
     * Add a field to the managed list that is randomized.
     *
     * @param columnName The name of the column
     */
    public void addRandomizedField(String columnName)
    {
        randomizeColumns.add(columnName);
    }

    /**
     * Put the desired data into the import file.
     *
     * @throws IOException If the file could not be imported.
     */
    private void createImportFile() throws IOException
    {
        // Create the output handlers.
        fw = new FileWriter(getImportFileName());
        pw = new PrintWriter(fw);

        // Output the original header to the import file.
        printToCSVFile(header, true);

        int start = 0;
        if (sectionSize >= rows.size())
            sectionSize = rows.size();
        else
            start = getRandomInteger(rows.size() - sectionSize);

        for (int i = start; i < start + sectionSize; i++)
        {
            // Set the internal data.
            String[] row = rows.get(i).split(SEPARATOR);

            // Set the randomized columns of data.
            ArrayList<String> cols = getRandomizeColumns();
            for (String col : cols)
            {
                int index = getColIndex(col);
                row[index] = getRandomText(row[index], 3);
            }

            // Set the constant columns of data.
            HashMap<String, String> constCol = getConstantColumn();
            for (String col : constCol.keySet())
            {
                int index = getColIndex(col);
                String value = constCol.get(col);
                row[index] = value;
            }
            // Add the row of data.
            data.add(row);

            // Rebuild the array of strings into a single string.
            StringBuilder builder = new StringBuilder();
            for (String s : row)
                builder.append(s + SEPARATOR);

            // Output the string to the file.
            printToCSVFile(builder.toString(), true);
        }

        // Flush the output to the file
        pw.flush();

        // Close the Print Writer
        pw.close();

        // Close the File Writer
        fw.close();
    }

    /**
     * Set the import file name.
     */
    private void createOutputFileName()
    {
        // Generate the random output file name.
        String outputFileName = getRandomText("import", 5);

        // Strip all the special characters from the name because many of
        // those characters are not valid for a file name.
        outputFileName = outputFileName.replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", "+");

        // Prepend the correct folder and set the import file name.
        outputFileName = outputFileName;
        setImportFileName(outputFileName + ".csv");
    }

    /**
     * Create a temporary file for handling imports.
     * 1. A file is created with a randomized name.
     * 2. The complete file is read from the input file.
     * 3. A section of the input file is chosen, some values are
     * then modified with additional randomization for uniqueness,
     * and they are written to the file.
     * 4. The name of the file is returned to the program for
     * doing the actual imports.
     *
     * @throws IOException If the temp file could not be created.
     */
    public void createTemporaryImport() throws IOException
    {
        // Create the output file name.
        createOutputFileName();

        // Read all the input
        loadInputFile();

        // Load the temporary file with the desired amount of data.
        createImportFile();
    }

    /**
     * @param text The head text to be found.
     * @return the index of the text found in the header.
     */
    private int getColIndex(String text)
    {
        String[] cols = header.split(",");
        int index = 0;
        for (String col : cols)
        {
            if (text.equalsIgnoreCase(col))
                break;
            index++;
        }
        return index;
    }

    /**
     * @return The data that contains a list of the columns that should have
     * constant data.
     */
    public HashMap<String, String> getConstantColumn()
    {
        return constantColumn;
    }

    /**
     * @return The list of data items that were loaded in the creation of the
     * temporary import file.
     */
    public ArrayList<String[]> getData()
    {
        return data;
    }

    /**
     * @return - The generated temporary import file name.
     */
    public String getImportFileName()
    {
        return importFileName;
    }

    /**
     * @return The input file name. This is the file that was used for loading the data
     */
    public String getInputFileName()
    {
        return inputFileName;
    }

    public ArrayList<String> getRandomizeColumns()
    {
        return randomizeColumns;
    }

    /**
     * @return - The number of rows desired for the import operation.
     */
    public int getSectionSize()
    {
        return sectionSize;
    }

    public void loadInputFile() throws IOException
    {
        BufferedReader fh = null;
        String buffer;
        InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream(getInputFileName());

        if (fileInputStream != null)
        {
            // Open a temporary file.
            File tmpInputFile = File.createTempFile(getInputFileName(), "");
            // Copy the temp file into the output stream.
            OutputStream outputStream = new FileOutputStream(tmpInputFile);
            IOUtils.copy(fileInputStream, outputStream);
            outputStream.close();
            tmpInputFile.deleteOnExit();

            // Open the input file.
            fh = new BufferedReader(new FileReader(tmpInputFile));
        }
        else
        {
            // Open the input file.
            fh = new BufferedReader(new FileReader(getInputFileName()));
        }

        // Read the first row (header) and ignore it.
        header = fh.readLine();
        while ((buffer = fh.readLine()) != null)
            rows.add(buffer);
        fh.close();
    }

    /**
     * @param string       The string to be printed to the output file.
     * @param printNewLine Indicates whether a newline character needs to be printed.
     */
    public void printToCSVFile(String string, Boolean printNewLine)
    {
        // Print the string to the output file.
        pw.print(string);

        // Check to see if a new line needs to be added to the file.
        if (printNewLine)
            pw.println();
    }

    /**
     * Set the generated temporary import file name.
     *
     * @param importFileName The name of the import file.
     */
    public void setImportFileName(String importFileName)
    {
        this.importFileName = importFileName;
    }

    /**
     * Sets the input file name. This file is read-only and used to generate a temporary
     * file with only a subset of the data that the input file may contain.
     *
     * @param inputFileName The name of the input file.
     */
    public void setInputFileName(String inputFileName)
    {
        this.inputFileName = inputFileName;
    }

    /**
     * Set the number of rows desired for the import operation.
     *
     * @param sectionSize The size, number of rows, to be used in creating the temporary file.
     */
    public void setSectionSize(int sectionSize)
    {
        this.sectionSize = sectionSize;
    }
}