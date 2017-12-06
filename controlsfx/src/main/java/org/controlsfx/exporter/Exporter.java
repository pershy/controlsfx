package org.controlsfx.exporter;

import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Creates an Excel file from a {@link TableView TableView}. The excel
 * file is created at the given {@link Path path}. The implementation may
 * use a {@link ColumnExporter} while creating a cell in the Excel file,
 * so as to retain the data and type. This may also be used to render custom
 * cells like images, buttons etc. into their textual representation. 
 *
 * <h3>Code Example:</h3>
 * <p>Creating an Exporter needs a TableView. The generic type of TableView and the 
 * Exporter must the same. For this example, we declare a Person model which consists of
 * a firstName and lastName properties. Here is how the model looks like:</p>
 *
 * <pre>{@code
 * public class Person {
 *     private StringProperty firstName;
 *     public void setFirstName(String value) { firstNameProperty().set(value); }
 *     public String getFirstName() { return firstNameProperty().get(); }
 *     public StringProperty firstNameProperty() {
 *         if (firstName == null) firstName = new SimpleStringProperty(this, "firstName");
 *         return firstName;
 *     }
 *
 *     private StringProperty lastName;
 *     public void setLastName(String value) { lastNameProperty().set(value); }
 *     public String getLastName() { return lastNameProperty().get(); }
 *     public StringProperty lastNameProperty() {
 *         if (lastName == null) lastName = new SimpleStringProperty(this, "lastName");
 *         return lastName;
 *     }
 *
 *     private final ObjectProperty<Date> dob;
 *     public void setDob(Date dob) { this.dob.set(dob); }
 *     public Date getDob() { return dob.get(); }
 *     public ObjectProperty<Date> dobProperty() {
 *         if (dob == null) dob = new SimpleObjectProperty(this, "dob");
 *         return dob;
 *     }
 * }}</pre>
 *
 * <p>Next, a TableView instance needs to be defined along with the TableColumns, as such:</p>
 *
 * <pre>{@code
 * TableView<Person> table = new TableView<>();
 *
 * TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
 * firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
 * TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
 * lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
 * TableColumn<Person, Date> dobCol = new TableColumn<>("Date of Birth");
 * dobCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
 *
 * tableView.getColumns().addAll(firstNameCol, lastNameCol, emailCol, ageCol, dobCol);
 * }</pre>
 *
 * <p>Once, the columns have been added to the TableView and shown on the scene-graph, it is
 * ready to be exported. We instantiate the Exporter using one of the default implementations provided
 * by the ControlsFX project or by defining a custom implementation. For this example, we will use the
 * <a href="https://bitbucket.org/controlsfx/controlsfx-exporter-apachepoi">ControlsFX Apache POI Exporter</a>.</p>
 *
 * <pre>{@code
 * Exporter<Person> exporter = new ApachePoiExporter<>(Paths.get("System.getProperty("user.home") + "/poi.xlsx"), "JavaFX TableView");
 * exporter.create(tableView);
 * }</pre>
 *
 * <p>If required, we can also declare ColumnExporter's for a TableColumn. In the following example,
 * we use a ColumnExporter to set the date format.</p>
 *
 * <pre>{@code
 * TableColumn<Person, Date> dobCol = new TableColumn<>("Date of Birth");
 * final ColumnExporter<Person, Date> dateTimeColumnExporter = new DateTimeColumnExporter<>(Person::getDob, "dd/MM/yyyy");
 * ExporterUtils.setColumnExporter(dobCol, dateTimeColumnExporter);
 * }</pre>
 *
 * <p>It is highly encouraged that developers using the exporter feature to observe the progress property 
 * and prevent user interaction with the TableView whilst the export is being performed</p>
 *
 * @param <T> The type of the TableView generic type
 */
public abstract class Exporter<T> {

    private ExecutorService executor;

    protected final Path path;

    public Exporter(Path path) {
        this.path = path;
        executor = Executors.newSingleThreadExecutor(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("exporter");
            return thread;
        });
    }

    /**
     * Creates the excel file at the specified path.
     * @param tableView TableView to be converted to excel.
     * @param styleExport {@link StyleExport} to decide if the export needs to extract cell styles.
     */
    public void export(TableView<T> tableView, StyleExport styleExport) throws IOException {
        if (Files.exists(path) && !Files.isWritable(path)) {
            throw new IOException("Cannot write to the path - " + path.toAbsolutePath().toString());
        }

        // Creation of headers is 10% progress
        // Creation of cells is 80% progress
        // Writing to the file adds 10% more
        executeOnBackgroundThread(() -> createHeaders(tableView));
        executeOnBackgroundThread(() -> applyColumnWidth(tableView));
        executeOnBackgroundThread(() -> {
            if (styleExport == StyleExport.APPLY_STYLES) {
                createCellsWithStyleAndWriteToFile(tableView);
            } else {
                createCells(tableView);
                createFile();
            }
        });
    }

    /**
     * Decides if export needs to copy cell styles along with the cell data.
     */
    public enum StyleExport {
        /**
         * Exports only the data from the TableView. No visual scroll of the TableView is required.
         */
        NO_STYLES,
        /**
         * Exports data along with basic cell styles like background color, border, font etc.
         * A visual scroll of the TableView is required to extract cell styles.
         */
        APPLY_STYLES
    }

    /**
     * Creates a cell for the header row (i.e. index 0) at the specified column index.
     * @param columnIndex The column index of the header cell.
     * @param header The value for the header cell.
     * @param tableColumnHeader The TableColumnHeader of the JavaFX TableView corresponding to the column index.
     */
    protected abstract void createHeaderCell(int columnIndex, String header, TableColumnHeader tableColumnHeader);

    /**
     * Creates a cell at the specified row and column index.
     * @param rowIndex The row index of the cell. Ideally, should start be from index 1.
     * @param columnIndex The column index of the cell.
     * @param tableCell The TableCell at the corresponding row and column index.
     * @param styleExport {@link StyleExport} to decide if the cells needs to be created with styles.
     */
    protected abstract void createCell(int rowIndex, int columnIndex, TableCell<T, ?> tableCell, StyleExport styleExport);

    /**
     * Applies the column width to the specified column of the excel sheet.
     * @param columnIndex The column index in the table.
     * @param width The width of the column to be set in pixels.
     */
    protected abstract void setColumnWidth(int columnIndex, double width);

    /**
     * Writes the excel to the specified {@link OutputStream}.
     * @param outputStream The {@link OutputStream} to which the excel is to be written.
     * @throws IOException In case an exception occurs while writing to the stream.
     */
    protected abstract void writeToFile(OutputStream outputStream) throws IOException;

    /**
     * Represents the progress of the export task. It is highly encouraged that developers using the exporter feature 
     * to observe the progress property and prevent user interaction with the TableView whilst the export is being performed.
     */
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress", 0.0);
    public final ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }
    public final double getProgress() {
        return progress.get();
    }

    /**
     * Extracts the column text from the supplied TableView to be used
     * as the first row in the Excel file. The implementation can make use of the 
     * method to get the list of headers in the TableView.
     * @param tableView TableView to extract headers
     * @return List of String to be used as columns header.
     */
    private List<String> getHeaders(TableView<T> tableView) {
        final ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
        return columns.stream().map(TableColumn::getText).collect(Collectors.toList());
    }

    private void createHeaders(TableView<T> tableView) {
        final List<String> headers = getHeaders(tableView);

        // Should we use lookUpAll() here?
        final TableViewSkinBase<T,T,TableView<T>,TableViewBehavior<T>,TableRow<T>,TableColumn<T,?>> skin = (TableViewSkinBase<T,T,TableView<T>,TableViewBehavior<T>,TableRow<T>,TableColumn<T,?>>) tableView.getSkin();
        TableHeaderRow tableHeader = skin.getTableHeaderRow();
        NestedTableColumnHeader rootHeader = tableHeader.getRootHeader();

        // Create header columns
        for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
            final TableColumnHeader tableColumnHeader = rootHeader.getColumnHeaders().get(columnIndex);
            createHeaderCell(columnIndex, headers.get(columnIndex), tableColumnHeader);
            progress.set(progress.get() + (0.1 / headers.size()));
        }
    }

    private void applyColumnWidth(TableView<T> tableView) {
        for (int columnIndex = 0; columnIndex < tableView.getColumns().size(); columnIndex++) {
            final double columnWidth = tableView.getColumns().get(columnIndex).getWidth();
            setColumnWidth(columnIndex, columnWidth);
        }
    }

    private void createFile() {
        // Write Excel to file system
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            writeToFile(outputStream);
            progress.set(1.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCells(TableView<T> tableView) {

        final ObservableList<T> items = tableView.getItems();
        // We loop on items first rather than columns because generally rows.size > columns.size.
        // If the performance is low, we can decide to switch the first loop to
        // be on columns and call applyColumnWidth() from the loop.
        // We could also implement a check to decide which path to take.
        for (int rowIndex = 0; rowIndex < items.size(); rowIndex++) {
            final ObservableList<TableColumn<T, ?>> visibleLeafColumns = tableView.getVisibleLeafColumns();
            for (int columnIndex = 0; columnIndex < visibleLeafColumns.size(); columnIndex++) {
                TableColumn<T, ?> tableColumn = visibleLeafColumns.get(columnIndex);
                final Callback<TableColumn<T, ?>, TableCell<T, ?>> cellCallback = (Callback<TableColumn<T, ?>, TableCell<T, ?>>) ((Callback) tableColumn.cellFactoryProperty().getValue());
                if (cellCallback == null) return;
                final TableCell<T, ?> tableCell = cellCallback.call(tableColumn);
                if (tableCell == null) return;

                // we set it's TableColumn, TableView and TableRow
                tableCell.updateTableColumn(tableColumn);
                tableCell.updateTableView(tableColumn.getTableView());
                tableCell.updateIndex(rowIndex);

                if ((tableCell.getText() != null && !tableCell.getText().isEmpty()) || tableCell.getGraphic() != null) {
                    createCell(rowIndex + 1, columnIndex, tableCell, StyleExport.NO_STYLES);
                }
                // dispose of the cell to prevent it retaining listeners (see RT-31015)
                tableCell.updateIndex(-1);
            }
            progress.set(progress.get() + (0.8 / tableView.getItems().size()));
        }
    }

    // Fields to be used for cell extraction process
    // Store the initial position and selected cell before scrolling takes place
    private final TableDefault tableDefault = new TableDefault();
    
    private void createCellsWithStyleAndWriteToFile(TableView<T> tableView) {
        // Store selected cells and clear selection
        final ObservableList<TablePosition> selectedCells = tableView.getSelectionModel().getSelectedCells();
        tableDefault.setSelectedCell(new ArrayList<>(selectedCells));
        tableView.getSelectionModel().clearSelection();
        
        final VirtualFlow<TableRow<T>> virtualFlow = (VirtualFlow) tableView.lookup(".virtual-flow");
        if (virtualFlow != null) {
            tableDefault.setPosition(virtualFlow.getPosition());
            // Make sure the table is at initial position 0, 0
            executeOnUIThread(scrollAndLayoutTable(tableView, virtualFlow, 0, 0));
        }
    }

    private Runnable scrollAndLayoutTable(TableView<T> tableView, VirtualFlow<TableRow<T>> virtualFlow, int rowIndex, int columnIndex) {
        return () -> {
            // Scroll the TableView in X direction
            tableView.scrollToColumnIndex(columnIndex);
            // Scroll the TableView in Y direction
            tableView.scrollTo(rowIndex);
            tableView.layout();
            
            final int totalNoOfRows = tableView.getItems().size();
            int endRowIndex = virtualFlow.getLastVisibleCell().getIndex();
            
            if (rowIndex < totalNoOfRows) {
                // For ordering, we get the index from the IndexedCell
                final Map<Integer, TableRow> indexRowMap =
                        IntStream.range(rowIndex, endRowIndex + 1) // include last index in the loop
                                .mapToObj(virtualFlow::getVisibleCell)
                                .filter(tr -> tr.getIndex() < totalNoOfRows)
                                .filter(tr -> tr.getItem() != null)
                                .collect(Collectors.toMap(TableRow::getIndex, Function.identity()));
                executeOnBackgroundThread(extractCellsFrom(tableView, virtualFlow, indexRowMap));
            } else {
                executeOnUIThread(restoreDefaults(tableView, virtualFlow));
            }
        };
    }

    private Runnable extractCellsFrom(TableView<T> tableView, VirtualFlow<TableRow<T>> virtualFlow, Map<Integer, TableRow> indexRowMap) {
        return () -> {
            final int totalNoOfRows = tableView.getItems().size();
            int maxColumnIndex = 0;
            final ObservableList<TableColumn<T, ?>> visibleLeafColumns = tableView.getVisibleLeafColumns();
            // We loop on items first rather than columns because generally rows.size > columns.size.
            // If the performance is low, we can decide to switch the first loop to
            // be on columns and call applyColumnWidth() from the loop.
            // We could also implement a check to decide which path to take.
            for (Map.Entry<Integer, TableRow> tableRowEntry : indexRowMap.entrySet()) {
                final TableRow tableRow = tableRowEntry.getValue();
                final ObservableList<Node> children = tableRow.getChildrenUnmodifiable();
                for (Node child : children) {
                    if (child instanceof TableCell) {
                        TableCell<T, ?> tc = (TableCell<T, ?>) child;
                        if (visibleLeafColumns.contains(tc.getTableColumn())) {
                            int columnIndex = tableView.getVisibleLeafIndex(tc.getTableColumn());
                            // We add 1 to the rowIndex since 1st row is already occupied by header
                            createCell(tableRowEntry.getKey() + 1, columnIndex, tc, StyleExport.APPLY_STYLES);
                            maxColumnIndex = Math.max(columnIndex, maxColumnIndex);
                            progress.set(progress.get() + (0.8 / (totalNoOfRows * visibleLeafColumns.size())));
                        }
                    }
                }
            }
            // Check if TableView has to scroll horizontally or vertically
            // If we haven't reached the end of columns keep scroll horizontally
            if (maxColumnIndex != visibleLeafColumns.size() - 1) {
                final int minRowIndex = indexRowMap.keySet().stream().mapToInt(Integer::intValue).min().orElse(0);
                executeOnUIThread(scrollAndLayoutTable(tableView, virtualFlow, minRowIndex, maxColumnIndex + 1));
            } else {
                final int maxRowIndex = indexRowMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(totalNoOfRows - 1);
                executeOnUIThread(scrollAndLayoutTable(tableView, virtualFlow, maxRowIndex + 1, 0));
            }
        };
    }

    private Runnable restoreDefaults(TableView<T> tableView, VirtualFlow<TableRow<T>> virtualFlow) {
        return () -> {
            // After cell creation, scroll back to initial position
            if (virtualFlow != null) {
                virtualFlow.setPosition(tableDefault.getPosition());
            } else {
                tableView.scrollTo(0);
            }
            tableView.layout();

            // Select the previously selected cells
            final List<TablePosition> selectedCells = tableDefault.getSelectedCell();
            if (selectedCells != null) {
                selectedCells.forEach(tp -> tableView.getSelectionModel().select(tp.getRow(), tp.getTableColumn()));
            }
            executeOnBackgroundThread(this::createFile);
        };
    }

    private void executeOnUIThread(Runnable runnable) {
        Platform.runLater(runnable);
    }

    private void executeOnBackgroundThread(Runnable runnable) {
        executor.execute(runnable);
    }
    
    private class TableDefault {
        
        private double position;
        private List<TablePosition> selectedCell;

        public double getPosition() {
            return position;
        }

        public void setPosition(double position) {
            this.position = position;
        }

        List<TablePosition> getSelectedCell() {
            return selectedCell;
        }

        void setSelectedCell(List<TablePosition> selectedCell) {
            this.selectedCell = selectedCell;
        }
    }
}
