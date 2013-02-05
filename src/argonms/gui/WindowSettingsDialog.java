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

package argonms.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import argonms.gui.model.Model;

//TODO: add an option to change font, and maybe an option to change Nimbus'
//color scheme if that theme is enabled, and an option to enable/disable
//telnet tab.
/**
 * 
 * @author GoldenKevin
 */
@SuppressWarnings("serial")
public class WindowSettingsDialog extends JDialog {
	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public WindowSettingsDialog(final Model model) {
		super(model.getFrame(), "GUI Preferences", true);

		JPanel lafPanel = new JPanel();
		ButtonGroup lafSelect = new ButtonGroup();
		lafPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Look & Feel"));

		final Map<JRadioButton, String> lookAndFeels = new HashMap<JRadioButton, String>();
		Map<String, String> parenthetical = new HashMap<String, String>();
		parenthetical.put(model.getConfig().getLookAndFeelClass(), "current");
		parenthetical.put(UIManager.getCrossPlatformLookAndFeelClassName(), "default");
		parenthetical.put(UIManager.getSystemLookAndFeelClassName(), "native");
		ActionListener lafListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String className = lookAndFeels.get(e.getSource());
				if (className != null) {
					model.getConfig().updateProps(Collections.singletonMap("argonms.gui.plaf", className), WindowSettingsDialog.this);
					model.getConfig().setLookAndFeelClass(className);
					model.updateLookAndFeel(className, WindowSettingsDialog.this);
				}
			}
		};
		for (final LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
			JRadioButton select = new JRadioButton();
			String text = lafInfo.getName();
			String extra = parenthetical.remove(lafInfo.getClassName());
			if (extra != null) {
				text += " (" + extra + ")";
				if (extra.equals("current")) {
					select.setSelected(true);
				}
			}
			select.setText(text);
			select.addActionListener(lafListener);
			lafSelect.add(select);
			lafPanel.add(select);
			lookAndFeels.put(select, lafInfo.getClassName());
		}
		getContentPane().add(lafPanel);
	}
}
