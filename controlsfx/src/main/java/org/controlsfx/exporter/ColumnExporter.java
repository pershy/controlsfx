package org.controlsfx.exporter;

import javafx.scene.control.TableColumn;

import java.util.function.Function;

/**
 * Can be used to define a {@link Function converter} and a {@link ExcelCellType excelCellType} which are
 * used on a {@link TableColumn TableColumn} to fetch data from individual 
 * {@link javafx.scene.control.TableCell}. There should be a one to one mapping
 * between a TableColumn and a ColumnExporter.
 * 
 * During the creation of a TableColumn, it is advised to create a ColumnExporter
 * and make a call to {@link ExporterUtils#setColumnExporter(TableColumn, ColumnExporter)}
 * which stores the ColumnExporter and can be used later by calling
 * {@link ExporterUtils#getColumnExporter(TableColumn)}.
 * 
 * @param <T> The type of the TableView generic type.
 * @param <R> The type of the content in all cells in this TableColumn.
 */
public class ColumnExporter<T, R> {

    private final Function<T, R> converter;
    private final ExcelCellType excelCellType;

    /**
     * Creates an instance of ColumnExporter with the supplied converter and column cell type.
     * @param converter Function to convert generic type of TableView to the type of cells in the TableColumn.
     * @param excelCellType {@link ExcelCellType Type} of all the cells in the TableColumn.  
     */
    public ColumnExporter(Function<T, R> converter, ExcelCellType excelCellType) {
        this.converter = converter;
        this.excelCellType = excelCellType;
    }

    /**
     * Returns a {@link Function} which maps the generic type of {@link javafx.scene.control.TableView}
     * to the type of data stored in all cells of a {@link TableColumn}.
     * @return Mapper function to convert generic type of TableView to the type of data stored
     *         in the cells of a TableColumn.
     */
    public Function<T, ?> getConverter() {
        return converter;
    }

    /**
     * Returns the {@link ExcelCellType type} of all the cells in a column of a TableView which can be used 
     * by the {@link Exporter} implementation to create cells in an Excel file with their
     * type intact.
     * @return Type of all the cells in a column
     */
    public ExcelCellType getExcelCellType() {
        return excelCellType;
    }
}
