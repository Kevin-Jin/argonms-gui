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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import argonms.gui.model.Environment;

/**
 * 
 * @author GoldenKevin
 */
@SuppressWarnings("serial")
public abstract class ConsoleTab extends JPanel {
	private static class OutputBox extends JTextArea {
		/**
		 * This method is not thread-safe. It must be called from the Swing EDT.
		 */
		public OutputBox() {
			super(25, 80);
			((DefaultCaret) getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

			setFont(Environment.CONSOLE_FONT);
			setEditable(false);
			setLineWrap(true);

			setBackground(Color.WHITE);
			setForeground(Color.BLACK);
		}
	}

	private class InputBox extends JPanel {
		private JTextField textbox;

		/**
		 * This method is not thread-safe. It must be called from the Swing EDT.
		 */
		public InputBox() {
			setOpaque(false);

			textbox = new JTextField(80);
			textbox.setFont(Environment.CONSOLE_FONT);
			textbox.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) { }

				@Override
				public void keyReleased(KeyEvent e) { }

				@Override
				public void keyTyped(KeyEvent e) {
					if (e.getKeyChar() == KeyEvent.VK_ENTER) {
						String text = textbox.getText() + '\n';
						textbox.setText("");
						textEntered(text);
					}
				}
			});
			setEnabled(false);

			setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			add(new JLabel(">"));
			add(Box.createRigidArea(new Dimension(5, 0)));
			add(textbox);
		}

		@Override
		public final void setEnabled(boolean enabled) {
			textbox.setEnabled(enabled);
		}
	}

	protected final JPanel optionsPane;
	private final OutputBox output;
	private final InputBox input;

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public ConsoleTab() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setOpaque(false);

		optionsPane = new JPanel();
		optionsPane.setPreferredSize(new Dimension(0, 32));
		optionsPane.setOpaque(false);
		add(optionsPane);

		add(Box.createRigidArea(new Dimension(0, 5)));

		output = new OutputBox();
		add(new JScrollPane(output,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

		add(Box.createRigidArea(new Dimension(0, 5)));

		input = new InputBox();
		add(input);
	}

	protected void clearOutput() {
		output.setText("");
	}

	protected void outputEnabled() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				output.setBackground(Color.BLACK);
				output.setForeground(Color.WHITE);
			}
		});
	}

	protected void outputDisabled() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				output.setBackground(Color.WHITE);
				output.setForeground(Color.BLACK);
			}
		});
	}

	protected void inputEnabled() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				input.setEnabled(true);
			}
		});
	}

	protected void inputDisabled() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				input.setEnabled(false);
			}
		});
	}

	protected void writeToOutput(String text) {
		output.append(text);
	}

	protected abstract void textEntered(String text);
}
