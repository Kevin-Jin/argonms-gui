/*
 * ArgonMS Server Manager - a process launcher and organizer utilizing Swing.
 * Copyright (C) 2011-2013  GoldenKevin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package argonms.gui.tab;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import argonms.gui.model.Model;

//TODO: implement
/**
 * 
 * @author GoldenKevin
 */
@SuppressWarnings("serial")
public class TelnetTab extends ConsoleTab {
	private static final String CONNECT_PANEL_KEY = "off", STATUS_PANEL_KEY = "on";

	private CardLayout layout;
	private boolean connected;
	private JLabel status;

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public TelnetTab(Model state) {
		layout = new CardLayout();
		optionsPane.setLayout(layout);

		optionsPane.add(constructConnectPanel(), CONNECT_PANEL_KEY);
		optionsPane.add(constructStatusPanel(), STATUS_PANEL_KEY);

		layout.show(optionsPane, CONNECT_PANEL_KEY);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructConnectPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
		pane.setOpaque(false);
		return pane;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructStatusPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
		pane.setOpaque(false);

		status = new JLabel();
		status.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!connected)
					layout.show(optionsPane, CONNECT_PANEL_KEY);
			}

			@Override
			public void mouseEntered(MouseEvent e) { }

			@Override
			public void mouseExited(MouseEvent e) { }

			@Override
			public void mousePressed(MouseEvent e) { }

			@Override
			public void mouseReleased(MouseEvent e) { }
		});
		return pane;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private void changeStatus(String message) {
		status.setText("Connect Status: " + message);
	}

	@Override
	protected void textEntered(String text) {
		
	}
}
