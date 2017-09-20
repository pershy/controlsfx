package org.controlsfx.exporter;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates an Excel file from a {@link TableView TableView}. The excel
 * file is created at the given {@link Path path}. The implementation should
 * use a {@link ColumnExporter} while creating a cell in the Excel file,
 * so as to retain the data and type.
 * 
 * <h3>Code Example:</h3>
 * <p>Creating an Exporter needs a TableView. The generic type of TableView and the 
 * Exporter must the same. For this example, we declare a Person model which consists of
 * a firstName and lastName properties. Here is how the model looks like:</p>
 * 
 * <pre>
 * {@code
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
 * }}</pre>
 *
 * <p>Next, a TableView instance needs to be defined, as such:</p>
 *
 * <pre>
 * {@code
 * TableView<Person> table = new TableView<Person>();
 * }
 * </pre>
 * 
 * <p>Next, we need to declare TableColumns and their corresponding ColumnExporter, as shown below:</p>
 * 
 * <pre>
 * {@code
 * TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
 * ExporterUtils.setColumnExporter(firstNameCol, new ColumnExporter<>(Person::getFirstName, ExcelCellType.STRING));
 *
 * TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
 * ExporterUtils.setColumnExporter(lastNameCol, new ColumnExporter<>(Person::getLastName, ExcelCellType.STRING));
 * }
 * </pre>
 * 
 * <p>Once, the columns have been added to the TableView and shown on the scene-graph, it is
 * ready to be exported. We instantiate the Exporter using one of the default implementations provided
 * by the ControlsFX project or by defining a custom implementation. For this example, we will use the
 * <a href="https://bitbucket.org/controlsfx/controlsfx-exporter-apachepoi">ControlsFX Apache POI Exporter</a>.</p>
 *     
 * <pre>
 * {@code
 * Exporter<Person> exporter = new ApachePoiExporter<>(Paths.get("System.getProperty("user.home") + "/poi.xlsx"), "JavaFX TableView");
 * exporter.create(tableView);
 * }
 * </pre>
 *
 * @param <T> The type of the TableView generic type
 */
public abstract class Exporter<T> {

    protected final Path path;

    public Exporter(Path path) {
        this.path = path;
    }

    /**
     * Creates the excel file at the specified path. This runs on a background thread.
     * @param tableView TableView to be converted to excel.
     */
    public void export(TableView<T> tableView) throws IOException {
        if (Files.exists(path) && !Files.isWritable(path)) {
            throw new IOException("Cannot write to the path - " + path.toAbsolutePath().toString());
        }

        // Creation of cells including header is 90% progress
        // Writing to the file adds 10% more
        final Thread exporterThread = new Thread(() -> {
            createHeaders(tableView);
            createCells(tableView);
            applyColumnWidth(tableView);
            try (OutputStream outputStream = Files.newOutputStream(path)) {
                writeToFile(outputStream);
                progress.set(1.0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        exporterThread.setName("exporter");
        exporterThread.setDaemon(true);
        exporterThread.start();
    }

    /**
     * Creates a cell for the header row (i.e. index 0) at the specified column index.
     * @param columnIndex The column index of the header cell.
     * @param header The value for the header cell.
     * @param tableColumn The TableColumn of the JavaFX TableView corresponding to the column index.
     */
    protected abstract void createHeaderCell(int columnIndex, String header, TableColumn<T, ?> tableColumn);

    /**
     * Creates a cell at the specified row and column index.
     * @param rowIndex The row index of the cell. Ideally, should start be from index 1.
     * @param columnIndex The column index of the cell.
     * @param item The value for the cell.
     * @param tableColumn The TableColumn of the JavaFX TableView corresponding to the column index.
     */
    protected abstract void createCell(int rowIndex, int columnIndex, T item, TableColumn<T, ?> tableColumn);

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

    /**
     * Represents the progress of the export task.
     */
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress", 0.0);
    public final ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }
    public final double getProgress() {
        return progress.get();
    }

    private void createHeaders(TableView<T> tableView) {
        final List<String> headers = getHeaders(tableView);
        final int totalNoOfCells = headers.size() * tableView.getItems().size() + headers.size(); // Adding header cells as well
        // Create header columns
        for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
            createHeaderCell(columnIndex, headers.get(columnIndex), tableView.getColumns().get(columnIndex));
            progress.set(progress.get() + (0.9 / totalNoOfCells));
        }
    }

    private void createCells(TableView<T> tableView) {
        final ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
        final ObservableList<T> items = tableView.getItems();
        final int totalNoOfCells = columns.size() * tableView.getItems().size() + columns.size();

        // We loop on items first rather than columns because generally rows.size > columns.size.
        // If the performance is low, we can decide to switch the first loop to
        // be on columns and call applyColumnWidth() from the loop.
        // We could also implement a check to decide which path to take.
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            // Header is already added as row 0
            final int rowIndex = itemIndex + 1;
            final T item = items.get(itemIndex);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                final TableColumn<T, ?> tableColumn = columns.get(columnIndex);
                createCell(rowIndex, columnIndex, item, tableColumn);
                progress.set(progress.get() + (0.9 / totalNoOfCells));
            }
        }
    }

    private void applyColumnWidth(TableView<T> tableView) {
        for (int columnIndex = 0; columnIndex < tableView.getColumns().size(); columnIndex++) {
            final double columnWidth = tableView.getColumns().get(columnIndex).getWidth();
            setColumnWidth(columnIndex, columnWidth);
        }
    }
}
