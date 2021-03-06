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
package org.eclipse.nebula.widgets.nattable.edit.config;

import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.ControlDecorationProvider;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;

/**
 * {@link IEditErrorHandler} implementation that will directly change the rendering
 * of the value inside the editor control.
 */
public class RenderErrorHandling extends AbstractEditErrorHandler {

	/**
	 * The default error styling used for rendering an error.
	 */
	protected IStyle defaultErrorStyle;
	{
        this.defaultErrorStyle = new Style();
        this.defaultErrorStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_RED);
	}
	
	/**
	 * The original foreground color, needed to be able to restore the
	 * normal rendering if the error is removed.
	 */
	protected Color originalFgColor;
	/**
	 * The original background color, needed to be able to restore the
	 * normal rendering if the error is removed.
	 */
	protected Color originalBgColor;
	/**
	 * The original font, needed to be able to restore the
	 * normal rendering if the error is removed.
	 */
	protected Font originalFont;
	/**
	 * The style that should be used to render an error.
	 */
	protected IStyle errorStyle;
	/**
	 * The decoration provider that should be used for decorating the
	 * editor control on error.
	 */
	protected final ControlDecorationProvider decorationProvider;
	
	/**
	 * Create a {@link RenderErrorHandling} with no underlying error handler
	 * and no decoration provider. 
	 */
	public RenderErrorHandling() {
		this(null);
	}
	
	/**
	 * Create a {@link RenderErrorHandling} with no underlying error handler
	 * and the specified decoration provider.
	 * @param decorationProvider The decoration provider that should be used for decorating the
	 * 			editor control on error.
	 */
	public RenderErrorHandling(ControlDecorationProvider decorationProvider) {
        this(null, decorationProvider);
	}
	
	/**
	 * Create a {@link RenderErrorHandling} with the underlying error handler
	 * and the specified decoration provider.
	 * By default the error style is set to render the value in the editor control
	 * with red foreground color. You can override that style by calling setErrorStyle(IStyle)
	 * @param underlyingErrorHandler The underlying error handler.
	 * @param decorationProvider The decoration provider that should be used for decorating the
	 * 			editor control on error.
	 */
    public RenderErrorHandling(IEditErrorHandler underlyingErrorHandler, ControlDecorationProvider decorationProvider) {
		super(underlyingErrorHandler);
        this.decorationProvider = decorationProvider;
        this.errorStyle = this.defaultErrorStyle;
	}
	
    /**
	 * {@inheritDoc}
	 * After the error remove is handled by its underlying {@link IEditErrorHandler},
	 * the original style will be applied to the editor control.
     */
	@Override
	public void removeError(ICellEditor cellEditor) {
		super.removeError(cellEditor);
		Control editorControl = cellEditor.getEditorControl();

		//reset the rendering information to normal
		editorControl.setBackground(originalBgColor);
		editorControl.setForeground(originalFgColor);
		editorControl.setFont(originalFont);
		
		//ensure to reset the stored original values so possible
		//dynamic rendering aspects are also covered
		originalBgColor = null;
		originalFgColor = null;
		originalFont = null;
		
		if (decorationProvider != null) {
			decorationProvider.hideDecoration();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * After the error is handled by its underlying {@link IEditErrorHandler},
	 * the configured error style will be applied to the editor control.
	 */
	@Override
	public void displayError(ICellEditor cellEditor, Exception e) {
		super.displayError(cellEditor, e);
		Control editorControl = cellEditor.getEditorControl();
		
		//store the current rendering information to be able to reset again
		originalBgColor = editorControl.getBackground();
		originalFgColor = editorControl.getForeground();
		originalFont = editorControl.getFont();
		
		//set the rendering information out of the error style
		editorControl.setBackground(this.errorStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		editorControl.setForeground(this.errorStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		editorControl.setFont(this.errorStyle.getAttributeValue(CellStyleAttributes.FONT));

		if (decorationProvider != null) {
			decorationProvider.showDecoration();
		}
	}

	/**
	 * @param errorStyle The style that should be used to render an error.
	 * 			Supported style attributes are foreground color, background color and font.
	 */
	public void setErrorStyle(IStyle errorStyle) {
		this.errorStyle = errorStyle != null ? errorStyle : this.defaultErrorStyle;
	}

}
