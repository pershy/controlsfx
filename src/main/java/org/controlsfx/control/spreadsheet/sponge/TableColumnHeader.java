/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.controlsfx.control.spreadsheet.sponge;

import static com.sun.javafx.scene.control.TableColumnSortTypeWrapper.getSortTypeName;
import static com.sun.javafx.scene.control.TableColumnSortTypeWrapper.getSortTypeProperty;
import static com.sun.javafx.scene.control.TableColumnSortTypeWrapper.isAscending;
import static com.sun.javafx.scene.control.TableColumnSortTypeWrapper.isDescending;
import static com.sun.javafx.scene.control.TableColumnSortTypeWrapper.setSortType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.ConditionalFeature;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.css.converters.SizeConverter;
import com.sun.javafx.scene.control.MultiplePropertyChangeListenerHandler;


/**
 * Region responsible for painting a single column header.
 */
public class TableColumnHeader extends Region {
	// Copied from TableColumn. The value here should always be in-sync with
	// the value in TableColumn
	private static final double DEFAULT_WIDTH = 80.0F;

	private boolean autoSizeComplete = false;

	/***************************************************************************
	 *                                                                         *
	 * Constructor                                                             *
	 *                                                                         *
	 **************************************************************************/

	public TableColumnHeader(final SpreadsheetViewSkinBase skin, final TableColumnBase tc) {
		this.skin = skin;
		this.column = tc;

		setFocusTraversable(false);

		updateColumnIndex();
		initUI();

		// change listener for multiple properties
		changeListenerHandler = new MultiplePropertyChangeListenerHandler(new Callback<String, Void>() {
			@Override public Void call(String p) {
				handlePropertyChanged(p);
				return null;
			}
		});
		changeListenerHandler.registerChangeListener(sceneProperty(), "SCENE");

		if (column != null && skin != null) {
			updateSortPosition();
			skin.getSortOrder().addListener(weakSortOrderListener);
			skin.getVisibleLeafColumns().addListener(weakVisibleLeafColumnsListener);
		}

		if (column != null) {
			changeListenerHandler.registerChangeListener(column.idProperty(), "TABLE_COLUMN_ID");
			changeListenerHandler.registerChangeListener(column.styleProperty(), "TABLE_COLUMN_STYLE");
			changeListenerHandler.registerChangeListener(column.widthProperty(), "TABLE_COLUMN_WIDTH");
			changeListenerHandler.registerChangeListener(column.visibleProperty(), "TABLE_COLUMN_VISIBLE");
			changeListenerHandler.registerChangeListener(column.sortNodeProperty(), "TABLE_COLUMN_SORT_NODE");
			changeListenerHandler.registerChangeListener(column.sortableProperty(), "TABLE_COLUMN_SORTABLE");
			changeListenerHandler.registerChangeListener(column.textProperty(), "TABLE_COLUMN_TEXT");
			changeListenerHandler.registerChangeListener(column.graphicProperty(), "TABLE_COLUMN_GRAPHIC");

			column.getStyleClass().addListener(weakStyleClassListener);
			updateStyleClass();
		}
	}



	/***************************************************************************
	 *                                                                         *
	 * Listeners                                                               *
	 *                                                                         *
	 **************************************************************************/

	protected final MultiplePropertyChangeListenerHandler changeListenerHandler;

	protected void handlePropertyChanged(String p) {
		if ("SCENE".equals(p)) {
			updateScene();
		} else if ("TABLE_COLUMN_VISIBLE".equals(p)) {
			setVisible(getTableColumn().isVisible());
		} else if ("TABLE_COLUMN_WIDTH".equals(p)) {
			// It is this that ensures that when a column is resized that the header
			// visually adjusts its width as necessary.
			isSizeDirty = true;
			requestLayout();
		} else if ("TABLE_COLUMN_ID".equals(p)) {
			setId(column.getId());
		} else if ("TABLE_COLUMN_STYLE".equals(p)) {
			setStyle(column.getStyle());
		} else if ("TABLE_COLUMN_SORT_TYPE".equals(p)) {
			updateSortGrid();
			if (arrow != null) {
				arrow.setRotate(isAscending(column) ? 180 : 0.0);
			}
		} else if ("TABLE_COLUMN_SORT_NODE".equals(p)) {
			updateSortGrid();
		} else if ("TABLE_COLUMN_SORTABLE".equals(p)) {
			// we need to notify all headers that a sortable state has changed,
			// in case the sort grid in other columns needs to be updated.
			if (skin.getSortOrder().contains(getTableColumn())) {
				final NestedTableColumnHeader root = getTableHeaderRow().getRootHeader();
				updateAllHeaders(root);
			}
		} else if ("TABLE_COLUMN_TEXT".equals(p)) {
			label.setText(column.getText());
		} else if ("TABLE_COLUMN_GRAPHIC".equals(p)) {
			label.setGraphic(column.getGraphic());
		}
	}

	private final ListChangeListener<TableColumnBase<?,?>> sortOrderListener = new ListChangeListener<TableColumnBase<?,?>>() {
		@Override public void onChanged(Change<? extends TableColumnBase<?,?>> c) {
			updateSortPosition();
		}
	};

	private final ListChangeListener<TableColumnBase<?,?>> visibleLeafColumnsListener = new ListChangeListener<TableColumnBase<?,?>>() {
		@Override public void onChanged(Change<? extends TableColumnBase<?,?>> c) {
			updateColumnIndex();
		}
	};

	private final ListChangeListener<String> styleClassListener = new ListChangeListener<String>() {
		@Override public void onChanged(Change<? extends String> c) {
			updateStyleClass();
		}
	};

	private final WeakListChangeListener<TableColumnBase<?,?>> weakSortOrderListener =
			new WeakListChangeListener<TableColumnBase<?,?>>(sortOrderListener);
	private final WeakListChangeListener<TableColumnBase<?,?>> weakVisibleLeafColumnsListener =
			new WeakListChangeListener<TableColumnBase<?,?>>(visibleLeafColumnsListener);
	private final WeakListChangeListener<String> weakStyleClassListener =
			new WeakListChangeListener<String>(styleClassListener);

	private static final EventHandler<MouseEvent> mousePressedHandler = new EventHandler<MouseEvent>() {
		@Override public void handle(MouseEvent me) {
			final TableColumnHeader header = (TableColumnHeader) me.getSource();

			// pass focus to the table, so that the user immediately sees
			// the focus rectangle around the table control.
			header.getTableViewSkin().getSkinnable().requestFocus();

			if (me.isPrimaryButtonDown() && header.isColumnReorderingEnabled()) {
				header.columnReorderingStarted(me);
			}
			me.consume();
		}
	};

	private static final EventHandler<MouseEvent> mouseDraggedHandler = new EventHandler<MouseEvent>() {
		@Override public void handle(MouseEvent me) {
			final TableColumnHeader header = (TableColumnHeader) me.getSource();

			if (me.isPrimaryButtonDown() && header.isColumnReorderingEnabled()) {
				header.columnReordering(me);
			}
			me.consume();
		}
	};

	private static final EventHandler<MouseEvent> mouseReleasedHandler = new EventHandler<MouseEvent>() {
		@Override public void handle(MouseEvent me) {
			if (me.isPopupTrigger()) {
				return;
			}

			final TableColumnHeader header = (TableColumnHeader) me.getSource();
			final TableColumnBase tableColumn = header.getTableColumn();

			final ContextMenu menu = tableColumn.getContextMenu();
			if (menu != null && menu.isShowing()) {
				return;
			}
			if (header.getTableHeaderRow().isReordering() && header.isColumnReorderingEnabled()) {
				header.columnReorderingComplete(me);
			} else if (me.isStillSincePress()) {
				TableColumnHeader.sortColumn(
						header.getTableViewSkin().getSortOrder(),
						tableColumn,
						header.isSortingEnabled(),
						header.isSortColumn,
						me.isShiftDown());
			}
			me.consume();
		}
	};

	private static final EventHandler<ContextMenuEvent> contextMenuRequestedHandler = new EventHandler<ContextMenuEvent>() {
		@Override public void handle(ContextMenuEvent me) {
			final TableColumnHeader header = (TableColumnHeader) me.getSource();
			final TableColumnBase tableColumn = header.getTableColumn();

			final ContextMenu menu = tableColumn.getContextMenu();
			if (menu != null) {
				menu.show(header, me.getScreenX(), me.getScreenY());
				me.consume();
			}
		}
	};



	/***************************************************************************
	 *                                                                         *
	 * Internal Fields                                                         *
	 *                                                                         *
	 **************************************************************************/

	private double dragOffset;

	private final SpreadsheetViewSkinBase skin;
	protected SpreadsheetViewSkinBase getTableViewSkin() {
		return skin;
	}

	private DoubleProperty size;
	private double getSize() {
		return size == null ? 20.0 : size.doubleValue();
	}
	private DoubleProperty sizeProperty() {
		if (size == null) {
			size = new StyleableDoubleProperty(20) {
				@Override public void set(double v) {
					// guard against a 0 or negative size
					super.set(v <= 0 ? 20.0 : v);
				}

				@Override public Object getBean() {
					return TableColumnHeader.this;
				}

				@Override public String getName() {
					return "size";
				}

				@Override public CssMetaData<TableColumnHeader,Number> getCssMetaData() {
					return StyleableProperties.SIZE;
				}
			};
		}
		return size;
	}

	private NestedTableColumnHeader nestedColumnHeader;
	NestedTableColumnHeader getNestedColumnHeader() { return nestedColumnHeader; }
	void setNestedColumnHeader(NestedTableColumnHeader nch) { nestedColumnHeader = nch; }

	private final TableColumnBase<?,?> column;
	public TableColumnBase getTableColumn() { return column; }

	private TableHeaderRow tableHeaderRow;
	TableHeaderRow getTableHeaderRow() { return tableHeaderRow; }
	void setTableHeaderRow(TableHeaderRow thr) { tableHeaderRow = thr; }

	private NestedTableColumnHeader parentHeader;
	NestedTableColumnHeader getParentHeader() { return parentHeader; }
	void setParentHeader(NestedTableColumnHeader ph) { parentHeader = ph; }

	// work out where this column currently is within its parent
	private Label label;

	// sort order
	private int sortPos = -1;
	private Region arrow;
	private Label sortOrderLabel;
	private HBox sortOrderDots;
	private Node sortArrow;
	private boolean isSortColumn;

	private boolean isSizeDirty = false;
	private boolean sortOrderDotsDirty = false;

	boolean isLastVisibleColumn = false;
	private int columnIndex = -1;



	/***************************************************************************
	 *                                                                         *
	 * Public API                                                              *
	 *                                                                         *
	 **************************************************************************/



	/***************************************************************************
	 *                                                                         *
	 * Private methods                                                         *
	 *                                                                         *
	 **************************************************************************/

	// RT-29682: When the sortable property of a TableColumnBase changes this
	// may impact other TableColumnHeaders, as they may need to change their
	// sort order representation. Rather than install listeners across all
	// TableColumn in the sortOrder list for their sortable property, we simply
	// update the sortPosition of all headers whenever the sortOrder property
	// changes, assuming the column is within the sortOrder list.
	private void updateAllHeaders(TableColumnHeader header) {
		if (header instanceof NestedTableColumnHeader) {
			final List<TableColumnHeader> children = ((NestedTableColumnHeader)header).getColumnHeaders();
			for (int i = 0; i < children.size(); i++) {
				updateAllHeaders(children.get(i));
			}
		} else {
			header.updateSortPosition();
		}
	}

	private void updateStyleClass() {
		// For now we leave the 'column-header' style class intact so that the
		// appropriate border styles are shown, etc.
		getStyleClass().setAll("column-header");
		getStyleClass().addAll(column.getStyleClass());
	}

	private void updateScene() {
		// RT-17684: If the TableColumn widths are all currently the default,
		// we attempt to 'auto-size' based on the preferred width of the first
		// n rows (we can't do all rows, as that could conceivably be an unlimited
		// number of rows retrieved from a very slow (e.g. remote) data source.
		// Obviously, the bigger the value of n, the more likely the default
		// width will be suitable for most values in the column
		final int n = 30;
		if (! autoSizeComplete) {
			if (getTableColumn() == null || getTableColumn().getPrefWidth() != DEFAULT_WIDTH || getScene() == null) {
				return;
			}
			getTableViewSkin().resizeColumnToFitContent(getTableColumn(), n);
			autoSizeComplete = true;
		}
	}

	void dispose() {
		final SpreadsheetViewSkinBase skin = getTableViewSkin();
		if (skin != null) {
			skin.getVisibleLeafColumns().removeListener(weakVisibleLeafColumnsListener);
			skin.getSortOrder().removeListener(weakSortOrderListener);
		}

		changeListenerHandler.dispose();
	}

	private boolean isSortingEnabled() {
		// this used to check if ! PlatformUtil.isEmbedded(), but has been changed
		// to always return true (for now), as we want to support column sorting
		// everywhere
		return true;
	}

	private boolean isColumnReorderingEnabled() {
		// we only allow for column reordering if there are more than one column,
		return !PlatformImpl.isSupported(ConditionalFeature.INPUT_TOUCH) && getTableViewSkin().getVisibleLeafColumns().size() > 1;
	}

	private void initUI() {
		// TableColumn will be null if we are dealing with the root NestedTableColumnHeader
		if (column == null) {
			return;
		}

		// set up mouse events
		setOnMousePressed(mousePressedHandler);
		setOnMouseDragged(mouseDraggedHandler);
		setOnContextMenuRequested(contextMenuRequestedHandler);
		setOnMouseReleased(mouseReleasedHandler);

		// --- label
		label = new Label();
		label.setAlignment(Pos.CENTER);
		label.setText(column.getText());
		label.setGraphic(column.getGraphic());
		label.setVisible(column.isVisible());

		// ---- container for the sort arrow (which is not supported on embedded
		// platforms)
		if (isSortingEnabled()) {
			// put together the grid
			updateSortGrid();
		}
	}

	private void updateSortPosition() {
		this.sortPos = ! column.isSortable() ? -1 : getSortPosition(skin.getSortOrder(), column);
		updateSortGrid();
	}

	private void updateSortGrid() {
		// Fix for RT-14488
		if (this instanceof NestedTableColumnHeader) {
			return;
		}

		getChildren().clear();
		getChildren().add(label);

		// we do not support sorting in embedded devices
		if (! isSortingEnabled()) {
			return;
		}

		isSortColumn = sortPos != -1;
		if (! isSortColumn) {
			if (sortArrow != null) {
				sortArrow.setVisible(false);
			}
			return;
		}

		// RT-28016: if the tablecolumn is not a visible leaf column, we should ignore this
		final int visibleLeafIndex = skin.getVisibleLeafIndex(getTableColumn());
		if (visibleLeafIndex == -1) {
			return;
		}

		final int sortColumnCount = getSortColumnCount(getTableViewSkin().getSortOrder());
		final boolean showSortOrderDots = sortPos <= 3 && sortColumnCount > 1;

		Node _sortArrow = null;
		if (getTableColumn().getSortNode() != null) {
			_sortArrow = getTableColumn().getSortNode();
			getChildren().add(_sortArrow);
		} else {
			final GridPane sortArrowGrid = new GridPane();
			_sortArrow = sortArrowGrid;
			sortArrowGrid.setPadding(new Insets(0, 3, 0, 0));
			getChildren().add(sortArrowGrid);

			// if we are here, and the sort arrow is null, we better create it
			if (arrow == null) {
				arrow = new Region();
				arrow.getStyleClass().setAll("arrow");
				arrow.setVisible(true);
				arrow.setRotate(isAscending(column) ? 180.0F : 0.0F);
				changeListenerHandler.registerChangeListener(getSortTypeProperty(column), "TABLE_COLUMN_SORT_TYPE");
			}

			arrow.setVisible(isSortColumn);

			if (sortPos > 2) {
				if (sortOrderLabel == null) {
					// ---- sort order label (for sort positions greater than 3)
					sortOrderLabel = new Label();
					sortOrderLabel.getStyleClass().add("sort-order");
				}

				// only show the label if the sortPos is greater than 3 (for sortPos
				// values less than three, we show the sortOrderDots instead)
				sortOrderLabel.setText("" + (sortPos + 1));
				sortOrderLabel.setVisible(sortColumnCount > 1);

				// update the grid layout
				sortArrowGrid.add(arrow, 1, 1);
				GridPane.setHgrow(arrow, Priority.NEVER);
				GridPane.setVgrow(arrow, Priority.NEVER);
				sortArrowGrid.add(sortOrderLabel, 2, 1);
			} else if (showSortOrderDots) {
				if (sortOrderDots == null) {
					sortOrderDots = new HBox(1);
					sortOrderDots.getStyleClass().add("sort-order-dots-container");
				}

				// show the sort order dots
				final boolean isAscending = isAscending(column);
				final int arrowRow = isAscending ? 1 : 2;
				final int dotsRow = isAscending ? 2 : 1;

				sortArrowGrid.add(arrow, 1, arrowRow);
				GridPane.setHalignment(arrow, HPos.CENTER);
				sortArrowGrid.add(sortOrderDots, 1, dotsRow);

				sortOrderDotsDirty = true;
			} else {
				// only show the arrow
				sortArrowGrid.add(arrow, 1, 1);
				GridPane.setHgrow(arrow, Priority.NEVER);
				GridPane.setVgrow(arrow, Priority.ALWAYS);
			}
		}

		sortArrow = _sortArrow;
		if (sortArrow != null) {
			sortArrow.setVisible(isSortColumn);
		}

		requestLayout();
	}

	private void updateSortOrderDots(int sortPos) {
		double arrowWidth = arrow.prefWidth(-1);

		// This is a bit of a hack - we're forcing the arrow to have its CSS
		// processed so that it has its bounds calculated
		if (arrowWidth == 0.0) {
			arrow.impl_processCSS(true);
			arrowWidth = arrow.prefWidth(-1);
		}

		sortOrderDots.getChildren().clear();

		for (int i = 0; i <= sortPos; i++) {
			final Region r = new Region();
			r.getStyleClass().add("sort-order-dot");

			final String sortTypeName = getSortTypeName(column);
			if (sortTypeName != null && ! sortTypeName.isEmpty()) {
				r.getStyleClass().add(sortTypeName.toLowerCase());
			}

			sortOrderDots.getChildren().add(r);
		}

		sortOrderDots.setAlignment(Pos.TOP_CENTER);
		sortOrderDots.setMaxWidth(arrowWidth);
	}

	private void moveColumn(TableColumnBase column, int newColumnPos) {
		if (column == null || newColumnPos < 0) {
			return;
		}

		final ObservableList<TableColumnBase> columns = column.getParentColumn() == null ?
				getTableViewSkin().getColumns() :
					column.getParentColumn().getColumns();

				final int currentPos = columns.indexOf(column);
				if (newColumnPos == currentPos) {
					return;
				}

				if (newColumnPos >= columns.size()) {
					newColumnPos = columns.size() - 1;
				}

				final List<TableColumnBase> tempList = new ArrayList<TableColumnBase>(columns);
				tempList.remove(column);
				tempList.add(newColumnPos, column);

				columns.setAll(tempList);
	}

	private void updateColumnIndex() {
		//        TableView tv = getTableView();
		final SpreadsheetViewSkinBase skin = getTableViewSkin();
		final TableColumnBase tc = getTableColumn();
		columnIndex = skin == null || tc == null ? -1 : skin.getVisibleLeafIndex(tc);

		// update the pseudo class state regarding whether this is the last
		// visible cell (i.e. the right-most).
		isLastVisibleColumn = getTableColumn() != null &&
				columnIndex != -1 &&
				columnIndex == getTableViewSkin().getVisibleLeafColumns().size() - 1;
		pseudoClassStateChanged(PSEUDO_CLASS_LAST_VISIBLE, isLastVisibleColumn);
	}






	/***************************************************************************
	 *                                                                         *
	 * Layout                                                                  *
	 *                                                                         *
	 **************************************************************************/

	/** {@inheritDoc} */
	@Override protected void layoutChildren() {
		if (isSizeDirty) {
			resize(getTableColumn().getWidth(), getHeight());
			isSizeDirty = false;
		} else if (sortOrderDotsDirty) {
			updateSortOrderDots(sortPos);
			sortOrderDotsDirty = false;
		}

		double sortWidth = 0;
		final double w = snapSize(getWidth()) - (snappedLeftInset() + snappedRightInset());
		final double h = getHeight() - (snappedTopInset() + snappedBottomInset());
		double x = w;

		// a bit hacky, but we REALLY don't want the arrow shape to fluctuate
		// in size
		if (arrow != null) {
			arrow.setMaxSize(arrow.prefWidth(-1), arrow.prefHeight(-1));
		}

		if (sortArrow != null && sortArrow.isVisible()) {
			sortWidth = sortArrow.prefWidth(-1);
			x -= sortWidth;
			sortArrow.resize(sortWidth, sortArrow.prefHeight(-1));
			positionInArea(sortArrow, x, snappedTopInset(),
					sortWidth, h, 0, HPos.CENTER, VPos.CENTER);
		}

		if (label != null) {
			final double labelWidth = w - sortWidth;
			label.resizeRelocate(snappedLeftInset(), 0, labelWidth, getHeight());
		}
	}

	/** {@inheritDoc} */
	@Override protected double computePrefWidth(double height) {
		if (getNestedColumnHeader() != null) {
			final double width = getNestedColumnHeader().prefWidth(height);

			if (column != null) {
				column.impl_setWidth(width);
			}

			return width;
		} else if (column != null && column.isVisible()) {
			return column.getWidth();
		}

		return 0;
	}

	/** {@inheritDoc} */
	@Override protected double computeMinHeight(double width) {
		return label == null ? 0 : label.minHeight(width);
	}

	/** {@inheritDoc} */
	@Override protected double computePrefHeight(double width) {
		if (getTableColumn() == null) {
			return 0;
		}
		return Math.max(getSize(), label.prefHeight(-1));
	}



	/***************************************************************************
	 *                                                                         *
	 * Column Reordering                                                       *
	 *                                                                         *
	 **************************************************************************/
	private int newColumnPos;

	private void columnReorderingStarted(MouseEvent me) {
		if (! column.impl_isReorderable()) {
			return;
		}

		// Used to ensure the column ghost is positioned relative to where the
		// user clicked on the column header
		dragOffset = me.getX();

		// Note here that we only allow for reordering of 'root' columns
		getTableHeaderRow().setReorderingColumn(column);
		getTableHeaderRow().setReorderingRegion(this);
	}

	private void columnReordering(MouseEvent me) {
		if (! column.impl_isReorderable()) {
			return;
		}

		// this is for handling the column drag to reorder columns.
		// It shows a line to indicate where the 'drop' will be.

		// indicate that we've started dragging so that the dragging
		// line overlay is shown
		getTableHeaderRow().setReordering(true);

		// Firstly we need to determine where to draw the line.
		// Find which column we're over
		TableColumnHeader hoverHeader = null;

		// x represents where the mouse is relative to the parent
		// NestedTableColumnHeader
		final double x = getParentHeader().sceneToLocal(me.getSceneX(), me.getSceneY()).getX();

		// calculate where the ghost column header should be
		final double dragX = getTableViewSkin().getSkinnable().sceneToLocal(me.getSceneX(), me.getSceneY()).getX() - dragOffset;
		getTableHeaderRow().setDragHeaderX(dragX);

		double startX = 0;
		double endX = 0;
		double headersWidth = 0;
		newColumnPos = 0;
		for (final TableColumnHeader header : getParentHeader().getColumnHeaders()) {
			final double headerWidth = header.prefWidth(-1);
			headersWidth += headerWidth;

			startX = header.getBoundsInParent().getMinX();
			endX = startX + headerWidth;

			if (x >= startX && x < endX) {
				hoverHeader = header;
				break;
			}
			newColumnPos++;
		}

		// hoverHeader will be null if the drag occurs outside of the
		// tableview. In this case we handle the newColumnPos specially
		// and then short-circuit. This results in the drop action
		// resulting in the correct result (the column will drop at
		// the start or end of the table).
		if (hoverHeader == null) {
			newColumnPos = x > headersWidth ? getParentHeader().getColumns().size() - 1 : 0;
			return;
		}

		// This is the x-axis value midway through hoverHeader. It's
		// used to determine whether the drop should be to the left
		// or the right of hoverHeader.
		final double midPoint = startX + (endX - startX) / 2;
		final boolean beforeMidPoint = x <= midPoint;

		// Based on where the mouse actually is, we have to shuffle
		// where we want the column to end up. This code handles that.
		final int currentPos = getIndex();
		newColumnPos += newColumnPos > currentPos && beforeMidPoint ?
				-1 : newColumnPos < currentPos && !beforeMidPoint ? 1 : 0;

		double lineX = getTableHeaderRow().sceneToLocal(hoverHeader.localToScene(hoverHeader.getBoundsInLocal())).getMinX();
		lineX = lineX + (beforeMidPoint ? 0 : hoverHeader.getWidth());

		if (lineX >= -0.5 && lineX <= getTableViewSkin().getSkinnable().getWidth()) {
			getTableHeaderRow().getColumnReorderLine().setTranslateX(lineX);

			// then if this is the first event, we set the property to true
			// so that the line becomes visible until the drop is completed.
			// We also set reordering to true so that the various reordering
			// effects become visible (ghost, transparent overlay, etc).
			getTableHeaderRow().getColumnReorderLine().setVisible(true);
		}

		getTableHeaderRow().setReordering(true);
	}

	private int getIndex() {
		return column.getParentColumn() == null ?
				getTableViewSkin().getColumns().indexOf(column) :
					column.getParentColumn().getColumns().indexOf(column);
	}

	protected void columnReorderingComplete(MouseEvent me) {
		if (! column.impl_isReorderable()) {
			return;
		}

		// Move col from where it is now to the new position.
		moveColumn(getTableColumn(), newColumnPos);

		// cleanup
		getTableHeaderRow().getColumnReorderLine().setTranslateX(0.0F);
		getTableHeaderRow().getColumnReorderLine().setLayoutX(0.0F);
		newColumnPos = 0;

		getTableHeaderRow().setReordering(false);
		getTableHeaderRow().getColumnReorderLine().setVisible(false);
		getTableHeaderRow().setReorderingColumn(null);
		getTableHeaderRow().setReorderingRegion(null);
		dragOffset = 0.0F;
	}



	/***************************************************************************
	 *                                                                         *
	 * Static utility methods                                                  *
	 *                                                                         *
	 **************************************************************************/

	// Because it is possible that some columns are in the sortOrder list but are
	// not themselves sortable, we cannot just do sortOrderList.indexOf(column).
	// Therefore, this method does the proper work required of iterating through
	// and ignoring non-sortable (and null) columns in the sortOrder list.
	private static int getSortPosition(final ObservableList<TableColumnBase> sortOrder, final TableColumnBase tc) {
		if (tc == null) {
			return -1;
		}

		int pos = 0;
		for (int i = 0; i < sortOrder.size(); i++) {
			final TableColumnBase _tc = sortOrder.get(i);
			if (_tc == null || ! _tc.isSortable()) {
				continue;
			}

			if (tc.equals(_tc)) {
				return pos;
			}

			pos++;
		}
		return -1;
	}

	// as with getSortPosition above, this method iterates through the sortOrder
	// list ignoring the null and non-sortable columns, so that we get the correct
	// number of columns in the sortOrder list.
	private static int getSortColumnCount(final ObservableList<TableColumnBase> sortOrder) {
		int pos = 0;
		for (int i = 0; i < sortOrder.size(); i++) {
			final TableColumnBase _tc = sortOrder.get(i);
			if (_tc == null || ! _tc.isSortable()) {
				continue;
			}

			pos++;
		}
		return pos;
	}

	public static void sortColumn(final ObservableList<TableColumnBase<?,?>> sortOrder,
			final TableColumnBase column,
			final boolean isSortingEnabled,
			final boolean isSortColumn,
			final boolean addColumn) {
		if (! isSortingEnabled) {
			return;
		}

		// we only allow sorting on the leaf columns and columns
		// that actually have comparators defined, and are sortable
		if (column == null || column.getColumns().size() != 0 || column.getComparator() == null || !column.isSortable())
		{
			return;
			//        final int sortPos = getTable().getSortOrder().indexOf(column);
			//        final boolean isSortColumn = sortPos != -1;
		}

		// addColumn is true e.g. when the user is holding down Shift
		if (addColumn) {
			if (!isSortColumn) {
				setSortType(column, TableColumn.SortType.ASCENDING);
				sortOrder.add(column);
			} else if (isAscending(column)) {
				setSortType(column, TableColumn.SortType.DESCENDING);
			} else {
				final int i = sortOrder.indexOf(column);
				if (i != -1) {
					sortOrder.remove(i);
				}
			}
		} else {
			// the user has clicked on a column header - we should add this to
			// the TableView sortOrder list if it isn't already there.
			if (isSortColumn && sortOrder.size() == 1) {
				// the column is already being sorted, and it's the only column.
				// We therefore move through the 2nd or 3rd states:
				//   1st click: sort ascending
				//   2nd click: sort descending
				//   3rd click: natural sorting (sorting is switched off)
				if (isAscending(column)) {
					setSortType(column, TableColumn.SortType.DESCENDING);
				} else {
					// remove from sort
					sortOrder.remove(column);
				}
			} else if (isSortColumn) {
				// the column is already being used to sort, so we toggle its
				// sortAscending property, and also make the column become the
				// primary sort column
				if (isAscending(column)) {
					setSortType(column, TableColumn.SortType.DESCENDING);
				} else if (isDescending(column)) {
					setSortType(column, TableColumn.SortType.ASCENDING);
				}

				// to prevent multiple sorts, we make a copy of the sort order
				// list, moving the column value from the current position to
				// its new position at the front of the list
				final List<TableColumnBase<?,?>> sortOrderCopy = new ArrayList<TableColumnBase<?,?>>(sortOrder);
				sortOrderCopy.remove(column);
				sortOrderCopy.add(0, column);
				sortOrder.setAll(column);
			} else {
				// add to the sort order, in ascending form
				setSortType(column, TableColumn.SortType.ASCENDING);
				sortOrder.setAll(column);
			}
		}
	}

	/***************************************************************************
	 *                                                                         *
	 * Stylesheet Handling                                                     *
	 *                                                                         *
	 **************************************************************************/

	private static final PseudoClass PSEUDO_CLASS_LAST_VISIBLE =
			PseudoClass.getPseudoClass("last-visible");

	/**
	 * Super-lazy instantiation pattern from Bill Pugh.
	 * @treatAsPrivate implementation detail
	 */
	private static class StyleableProperties {
		private static final CssMetaData<TableColumnHeader,Number> SIZE =
				new CssMetaData<TableColumnHeader,Number>("-fx-size",
						SizeConverter.getInstance(), 20.0) {

			@Override
			public boolean isSettable(TableColumnHeader n) {
				return n.size == null || !n.size.isBound();
			}

			@Override
			public StyleableProperty<Number> getStyleableProperty(TableColumnHeader n) {
				return (StyleableProperty<Number>)n.sizeProperty();
			}
		};

		private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
		static {

			final List<CssMetaData<? extends Styleable, ?>> styleables =
					new ArrayList<CssMetaData<? extends Styleable, ?>>(Region.getClassCssMetaData());
			styleables.add(SIZE);
			STYLEABLES = Collections.unmodifiableList(styleables);

		}
	}

	/**
	 * @return The CssMetaData associated with this class, which may include the
	 * CssMetaData of its super classes.
	 */
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.STYLEABLES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
		return getClassCssMetaData();
	}

}