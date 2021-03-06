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
package org.eclipse.nebula.widgets.nattable.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractRowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommandHandler;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;


public class TreeLayer extends AbstractRowHideShowLayer {

	public static final String TREE_COLUMN_CELL = "TREE_COLUMN_CELL"; //$NON-NLS-1$

	public static final int TREE_COLUMN_NUMBER = 0;

	private final ITreeRowModel<?> treeRowModel;

	private final Set<Integer> hiddenRowIndexes;

	private IndentedTreeImagePainter indentedTreeImagePainter;
	private ICellPainter treeImagePainter;
	
	public TreeLayer(IUniqueIndexLayer underlyingLayer, ITreeRowModel<?> treeRowModel) {
		this(underlyingLayer, treeRowModel, true);
	}
	
	public TreeLayer(IUniqueIndexLayer underlyingLayer, ITreeRowModel<?> treeRowModel, boolean useDefaultConfiguration) {
		super(underlyingLayer);
		this.treeRowModel = treeRowModel;

		this.hiddenRowIndexes = new TreeSet<Integer>();
		
		if (useDefaultConfiguration) {
			addConfiguration(new DefaultTreeLayerConfiguration(this));
		}
		
		setConfigLabelAccumulator(new IConfigLabelAccumulator() {
			@Override
			public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
				if (isTreeColumn(columnPosition)) {
					configLabels.addLabelOnTop(TREE_COLUMN_CELL);
				}
			}
		});

		indentedTreeImagePainter = new IndentedTreeImagePainter(treeRowModel);
		treeImagePainter = indentedTreeImagePainter.getTreeImagePainter();

		registerCommandHandler(new TreeExpandCollapseCommandHandler(this));
	}

	public ITreeRowModel<?> getModel() {
		return this.treeRowModel;
	}

	/**
	 * @return the treeImagePainter
	 */
	public ICellPainter getTreeImagePainter() {
		return treeImagePainter;
	}
	
	private boolean isTreeColumn(int columnPosition) {
		return columnPosition == TREE_COLUMN_NUMBER;
	}
	
	@Override
	public ICellPainter getCellPainter(int columnPosition, int rowPosition, ILayerCell cell, IConfigRegistry configRegistry) {
		ICellPainter cellPainter = super.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
		
		if (cell.getConfigLabels().hasLabel(TREE_COLUMN_CELL)) {
			cellPainter = new BackgroundPainter(new CellPainterDecorator(cellPainter, CellEdgeEnum.LEFT, indentedTreeImagePainter));
		}
		
		return cellPainter;
	}

	@Override
	public boolean isRowIndexHidden(int rowIndex) {
		return this.hiddenRowIndexes.contains(Integer.valueOf(rowIndex)) || isHiddenInUnderlyingLayer(rowIndex);
	}

	@Override
	public Collection<Integer> getHiddenRowIndexes() {
		return this.hiddenRowIndexes;
	}

	/**
	 * Performs an expand/collapse action dependent on the current state of the tree node
	 * at the given row index.
	 * @param parentIndex The index of the row that shows the tree node for which the
	 * 			expand/collapse action should be performed.
	 */
	public void expandOrCollapseIndex(int parentIndex) {
		if (this.treeRowModel.isCollapsed(parentIndex)) {
			expandTreeRow(parentIndex);
		} else {
			collapseTreeRow(parentIndex);
		}
	}

	/**
	 * Collapses the tree node for the given row index.
	 * @param parentIndex The index of the row that shows the node that should be collapsed
	 */
	public void collapseTreeRow(int parentIndex) {
		List<Integer> rowIndexes = this.treeRowModel.collapse(parentIndex);
		List<Integer> rowPositions = new ArrayList<Integer>();
		for (Integer rowIndex : rowIndexes) {
			int rowPos = getRowPositionByIndex(rowIndex);
			//if the rowPos is negative, it is not visible because of hidden state in an underlying layer
			if (rowPos >= 0) {
				rowPositions.add(rowPos);
			}
		}
		this.hiddenRowIndexes.addAll(rowIndexes);
		invalidateCache();
		fireLayerEvent(new HideRowPositionsEvent(this, rowPositions));
	}

	/**
	 * Expands the tree node for the given row index.
	 * @param parentIndex The index of the row that shows the node that should be expanded
	 */
	public void expandTreeRow(int parentIndex) {
		List<Integer> rowIndexes = 	this.treeRowModel.expand(parentIndex);
		this.hiddenRowIndexes.removeAll(rowIndexes);
		invalidateCache();
		fireLayerEvent(new ShowRowPositionsEvent(this, rowIndexes));
	}
	
	/**
	 * Checks the underlying layer if the row is hidden by another layer.
	 * @param rowIndex The index of the row whose hidden state should be checked
	 * @return <code>true</code> if the row at the given index is hidden in the underlying layer
	 * 			<code>false</code> if not.
	 */
	private boolean isHiddenInUnderlyingLayer(int rowIndex) {
		IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
		return (underlyingLayer.getRowPositionByIndex(rowIndex) == -1);
	}
	
	@Override
	public boolean doCommand(ILayerCommand command) {
		//special command transformations are needed to hide also child nodes
		if (command instanceof RowHideCommand) {
			return handleRowHideCommand((RowHideCommand)command);
		} else if (command instanceof MultiRowHideCommand) {
			return handleMultiRowHideCommand((MultiRowHideCommand)command);
		}
		return super.doCommand(command);
	}
	
	/**
	 * Checks if the given command tries to hide a row that is a node that is not collapsed and has children.
	 * In that case also the child rows need to be hidden. 
	 * @param command The {@link RowHideCommand} to process
	 * @return <code>true</code> if the command has been handled, <code>false</code> otherwise
	 */
	protected boolean handleRowHideCommand(RowHideCommand command) {
		//transform position to index
		if (command.convertToTargetLayer(this)) {
			int rowIndex = getRowIndexByPosition(command.getRowPosition());
			if (this.treeRowModel.hasChildren(rowIndex) && !this.treeRowModel.isCollapsed(rowIndex)) {
				List<Integer> childIndexes = this.treeRowModel.getChildIndexes(rowIndex);
				int[] childPositions = new int[childIndexes.size()+1];
				childPositions[0] = command.getRowPosition();
				for (int i = 1; i < childIndexes.size()+1; i++) {
					int childPos = getRowPositionByIndex(childIndexes.get(i-1));
					childPositions[i] = childPos;
				}
				return super.doCommand(new MultiRowHideCommand(this, childPositions));
			}
		}
		return super.doCommand(command);
	}
	
	/**
	 * Checks if the given command tries to hide rows that are nodes that are not collapsed and have children.
	 * In that case also the child rows need to be hidden. 
	 * @param command The {@link MultiRowHideCommand} to process
	 * @return <code>true</code> if the command has been handled, <code>false</code> otherwise
	 */
	protected boolean handleMultiRowHideCommand(MultiRowHideCommand command) {
		//transform position to index
		if (command.convertToTargetLayer(this)) {
			List<Integer> rowPositionsToHide = new ArrayList<Integer>();
			for (Integer rowPos : command.getRowPositions()) {
				rowPositionsToHide.add(rowPos);
				int rowIndex = getRowIndexByPosition(rowPos);
				if (this.treeRowModel.hasChildren(rowIndex) && !this.treeRowModel.isCollapsed(rowIndex)) {
					List<Integer> childIndexes = this.treeRowModel.getChildIndexes(rowIndex);
					for (Integer childIndex : childIndexes) {
						rowPositionsToHide.add(getRowPositionByIndex(childIndex));
					}
				}
			}
			
			int[] childPositions = new int[rowPositionsToHide.size()];
			for (int i = 0; i < rowPositionsToHide.size(); i++) {
				childPositions[i] = rowPositionsToHide.get(i);
			}
			return super.doCommand(new MultiRowHideCommand(this, childPositions));
		}
		return super.doCommand(command);
	}
}
