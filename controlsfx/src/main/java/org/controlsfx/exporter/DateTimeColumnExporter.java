package org.controlsfx.exporter;

import java.util.Date;
import java.util.function.Function;

/**
 * An extension to {@link ColumnExporter} to be used with {@link ExcelCellType#DATE}.
 * This column exporter can be used to format the date with the date/time pattern.
 * Default pattern is "MM/dd/yyyy".
 * @param <T> The type of the TableView generic type.
 */
public class DateTimeColumnExporter<T> extends ColumnExporter<T, Date> {

    /**
     * Default format to be used with {@link ExcelCellType#DATE}.
     */
    public static final String DEFAULT_FORMAT = "MM/dd/yyyy";
    
    private String dateFormat = DEFAULT_FORMAT;

    /**
     * Creates an instance of DateTimeColumnExporter with the supplied converter and date format pattern.
     * @param converter Function to convert generic type of TableView to the type of cells in the TableColumn.
     * @param dateFormat Date and Time pattern string to format the Date.  
     */
    public DateTimeColumnExporter(Function<T, Date> converter, String dateFormat) {
        super(converter, ExcelCellType.DATE);
        this.dateFormat = dateFormat;
    }

    /**
     * Returns the Date and Time patter string used to format the Date.
     * Default value is "MM/dd/yyyy".
     * @return Date and Time pattern string.
     */
    public String getDateFormat() {
        return dateFormat;
    }
}
