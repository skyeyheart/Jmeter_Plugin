package kg.apc.jmeter.charting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author Stéphane Hoblingre
 */
public class GraphModelToCsvExporter
{
    private AbstractMap<String, AbstractGraphRow> model = null;
    private File destFile = null;
    private String csvSeparator;
    private char decimalSeparator;
    private SimpleDateFormat dateFormatter;

    public GraphModelToCsvExporter(
            AbstractMap<String, AbstractGraphRow> rows,
            File destFile,
            String csvSeparator)
    {
        this.destFile = destFile;
        this.model = rows;
        this.csvSeparator = csvSeparator;
        this.decimalSeparator = new DecimalFormatSymbols().getDecimalSeparator();
        dateFormatter = new SimpleDateFormat("HH:mm:ss" + decimalSeparator + "S");
    }

    //used for Unit Tests only as of now
    public GraphModelToCsvExporter(
            AbstractMap<String, AbstractGraphRow> rows,
            File destFile,
            String csvSeparator,
            char decimalSeparator)
    {
        this(rows, destFile, csvSeparator);
        this.decimalSeparator = decimalSeparator;
    }

    private String xValueFormatter(long xValue)
    {
        if (xValue > 1000000000000L)
        {
            return dateFormatter.format(xValue);
        } else
        {
            return "" + xValue;
        }
    }

    public void writeCsvFile() throws IOException
    {
        //first, get all X values and rows names
        ConcurrentSkipListSet<Long> xValues = new ConcurrentSkipListSet<Long>();
        Iterator<Entry<String, AbstractGraphRow>> it = model.entrySet().iterator();
        ArrayList<String> rawsName = new ArrayList<String>();
        while(it.hasNext())
        {
            Entry<String, AbstractGraphRow> row = it.next();
            rawsName.add(row.getKey());
            Iterator<Entry<Long, AbstractGraphPanelChartElement>> itRow = row.getValue().iterator();
            while (itRow.hasNext())
            {
                Entry<Long, AbstractGraphPanelChartElement> element = itRow.next();
                xValues.add(element.getKey());
            }
        }

        //write file...
        //1st line
        BufferedWriter writer = new BufferedWriter(new FileWriter(destFile));
        writer.write("Category (X) axis");
   
        for(int i=0; i<rawsName.size(); i++)
        {
            writer.write(csvSeparator);
            writer.write(rawsName.get(i));
        }

        writer.newLine();
        writer.flush();

        //data lines

        Iterator<Long> itXValues = xValues.iterator();
        while(itXValues.hasNext())
        {
            long xValue = itXValues.next();
            writer.write(xValueFormatter(xValue));
         
            for(int i=0; i<rawsName.size(); i++)
            {
                writer.write(csvSeparator);
                AbstractGraphRow row = model.get(rawsName.get(i));
                AbstractGraphPanelChartElement value = row.getElement(xValue);
                if(value != null)
                {
                    writer.write("" + value.getValue());
                }
            }

            writer.newLine();
            writer.flush();
        }
        writer.close();
    }
}
