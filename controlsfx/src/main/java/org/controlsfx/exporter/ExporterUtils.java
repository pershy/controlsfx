package org.controlsfx.exporter;

import javafx.scene.control.TableColumn;

/**
 * Helper class to store and fetch {@link ColumnExporter} for a {@link TableColumn}.
 * 
 * <p>The following are the two particular instances where they should be used:</p>
 * <ul>
 *     <li>To store ColumnExporter while creating TableColumns for the TableView</li>
 *     <li>To fetch ColumnExporter while writing an {@link Exporter} implementations</li>
 * </ul>
 */
public class ExporterUtils {
    
    private static final String COLUMN_EXPORTER = "column-exporter";

    /**
     * Can be used to store a {@link ColumnExporter} for a specific {@link TableColumn}.
     * @param tableColumn TableColumn in a TableView.
     * @param columnExporter ColumnExporter for the specific TableColumn.
     * @param <T> The type of the TableView generic type.
     * @param <R> The type of all the cells in a TableColumn.
     */
    public static <T, R> void setColumnExporter(TableColumn<T, R> tableColumn, ColumnExporter<T, R> columnExporter) {
        tableColumn.getProperties().put(COLUMN_EXPORTER, columnExporter);
    }

    /**
     * Return the {@link ColumnExporter} for the {@link TableColumn}. Returns null if no
     * value if present.
     * @param tableColumn TableColumn in the TableView.
     * @param <T> The type of the TableView generic type.
     * @return ColumnExporter for the TableColumn.
     */
    public static <T> ColumnExporter<T, ?> getColumnExporter(TableColumn<T, ?> tableColumn) {
        return (ColumnExporter<T, ?>) tableColumn.getProperties().get(COLUMN_EXPORTER);
    }
}
