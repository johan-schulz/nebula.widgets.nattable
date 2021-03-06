/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.hideshow.command;


import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.RowHideShowLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ShowAllRowsCommandTest {

	private RowHideShowLayer rowHideShowLayer;

	@Before
	public void setup() {
		rowHideShowLayer = new RowHideShowLayerFixture();
	}

	@Test
	public void testHideColumnCommand() {
		Assert.assertEquals(5, rowHideShowLayer.getRowCount());

		rowHideShowLayer.doCommand(new ShowAllRowsCommand());

		Assert.assertEquals(7, rowHideShowLayer.getRowCount());

		Assert.assertEquals(4, rowHideShowLayer.getRowIndexByPosition(0));
		Assert.assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		Assert.assertEquals(0, rowHideShowLayer.getRowIndexByPosition(2));
		Assert.assertEquals(2, rowHideShowLayer.getRowIndexByPosition(3));
		Assert.assertEquals(3, rowHideShowLayer.getRowIndexByPosition(4));
		Assert.assertEquals(5, rowHideShowLayer.getRowIndexByPosition(5));
		Assert.assertEquals(6, rowHideShowLayer.getRowIndexByPosition(6));
	}

}
