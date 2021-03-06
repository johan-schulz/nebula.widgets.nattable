/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractMultiRowCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

public class SelectRowsCommand extends AbstractMultiRowCommand {

	private ColumnPositionCoordinate columnPositionCoordinate;
	private final boolean withShiftMask;
	private final boolean withControlMask;
	private RowPositionCoordinate rowPositionCoordinateToMoveIntoViewport;

	public SelectRowsCommand(ILayer layer, int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		this(layer, columnPosition, ArrayUtil.asIntArray(rowPosition), withShiftMask, withControlMask, rowPosition);
	}

	public SelectRowsCommand(ILayer layer, int columnPosition, int[] rowPositions, boolean withShiftMask, boolean withControlMask, int rowPositionToMoveIntoViewport) {
		super(layer, rowPositions);
		this.columnPositionCoordinate = new ColumnPositionCoordinate(layer, columnPosition);
		this.withControlMask = withControlMask;
		this.withShiftMask = withShiftMask;
		this.rowPositionCoordinateToMoveIntoViewport = new RowPositionCoordinate(layer, rowPositionToMoveIntoViewport);
	}

	protected SelectRowsCommand(SelectRowsCommand command) {
		super(command);
		this.columnPositionCoordinate = command.columnPositionCoordinate;
		this.withShiftMask = command.withShiftMask;
		this.withControlMask = command.withControlMask;
		this.rowPositionCoordinateToMoveIntoViewport = command.rowPositionCoordinateToMoveIntoViewport;
	}

	@Override
	public boolean convertToTargetLayer(ILayer targetLayer) {
		if (super.convertToTargetLayer(targetLayer)) {
			ColumnPositionCoordinate targetColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(columnPositionCoordinate, targetLayer);
			if (targetColumnPositionCoordinate != null && targetColumnPositionCoordinate.getColumnPosition() >= 0) {
				this.columnPositionCoordinate = targetColumnPositionCoordinate;
				this.rowPositionCoordinateToMoveIntoViewport = LayerCommandUtil.convertRowPositionToTargetContext(rowPositionCoordinateToMoveIntoViewport, targetLayer);
				return true;
			}
		}
		return false;
	}

	public int getColumnPosition() {
		return columnPositionCoordinate.getColumnPosition();
	}

	public boolean isWithShiftMask() {
		return withShiftMask;
	}

	public boolean isWithControlMask() {
		return withControlMask;
	}
	
	public int getRowPositionToMoveIntoViewport() {
		if (rowPositionCoordinateToMoveIntoViewport != null) {
			return rowPositionCoordinateToMoveIntoViewport.getRowPosition();
		} else {
			return -1;
		}
	}

	public SelectRowsCommand cloneCommand() {
		return new SelectRowsCommand(this);
	}
}
