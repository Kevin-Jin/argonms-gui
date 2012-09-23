/*
 * ArgonMS Server Manager - a process launcher and organizer utilizing Swing.
 * Copyright (C) 2011-2012  GoldenKevin
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import argonms.gui.LaunchConfigDialog;
import argonms.gui.WindowSettingsDialog;
import argonms.gui.model.Model;

/**
 * 
 * @author GoldenKevin
 */
@SuppressWarnings("serial")
public class MainTab extends JPanel {
	private Model m;
	private JButton startAll, stopAll;
	private JPanel settingsPane;

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public MainTab(Model state) {
		setOpaque(false);
		setLayout(new BorderLayout());
		JPanel left = new JPanel(), top = new JPanel();
		left.setOpaque(false);
		left.add(constructSettingsPanel());
		add(left, BorderLayout.LINE_START);
		top.setOpaque(false);
		top.add(constructSwitchPanel());
		add(top, BorderLayout.PAGE_START);

		m = state;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructSettingsPanel() {
		settingsPane = new JPanel();
		settingsPane.setOpaque(false);
		settingsPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Settings"));
		settingsPane.setLayout(new GridBagLayout());

		JButton pathSettings = new JButton("Launch Configuration");
		pathSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog d = new LaunchConfigDialog(m);
				d.setResizable(false);
				d.pack();
				d.setVisible(true);
			}
		});
		JButton windowSettings = new JButton("Window Preferences");
		windowSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog d = new WindowSettingsDialog(m);
				d.setResizable(false);
				d.pack();
				d.setVisible(true);
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		settingsPane.add(pathSettings, c);
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		settingsPane.add(windowSettings, c);

		return settingsPane;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructSwitchPanel() {
		JPanel pane = new JPanel();
		pane.setOpaque(false);
		pane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Server Operation"));

		startAll = new JButton("Start All");
		startAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				m.startAllProcesses();
			}
		});
		startAll.setEnabled(true);
		stopAll = new JButton("Stop All");
		stopAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				m.stopAllProcesses();
			}
		});
		stopAll.setEnabled(false);

		pane.add(startAll);
		pane.add(stopAll);

		return pane;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void enableStartAllButton() {
		startAll.setEnabled(true);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void disableStartAllButton() {
		startAll.setEnabled(false);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void enableStopAllButton() {
		stopAll.setEnabled(true);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void disableStopAllButton() {
		stopAll.setEnabled(false);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void disableSettings() {
		settingsPane.setEnabled(false);
		settingsPane.setToolTipText("You must close all servers before you are allowed to change the settings.");
		for (Component comp : settingsPane.getComponents()) {
			comp.setEnabled(false);
			((JButton) comp).setToolTipText("You must close all servers before you are allowed to change the settings.");
		}
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void enableSettings() {
		settingsPane.setEnabled(true);
		settingsPane.setToolTipText(null);
		for (Component comp : settingsPane.getComponents()) {
			comp.setEnabled(true);
			((JButton) comp).setToolTipText(null);
		}
	}
}
