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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import argonms.gui.model.Environment;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author GoldenKevin
 */
@SuppressWarnings("serial")
public abstract class ConsoleTab extends JPanel {
	private static class OutputBox extends JTextArea {
		private final KeyAdapter keyAdapter;
		private final MouseAdapter mouseAdapter;
		private JScrollBar scrollBar;
		private volatile boolean heldByScrollWheel;
		private boolean heldByClick; //should only be used in EDT, so we're safe
		private final Set<Integer> heldKeys;

		/**
		 * This method is not thread-safe. It must be called from the Swing EDT.
		 */
		public OutputBox() {
			super(25, 80);
			heldKeys = new HashSet<Integer>(); //should only be used in EDT, so we're safe

			setFont(Environment.CONSOLE_FONT);
			setEditable(false);
			getCaret().setVisible(true);
			setLineWrap(true);
			addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					getCaret().setVisible(true);
				}

				@Override
				public void focusLost(FocusEvent e) {
					getCaret().setVisible(true);
					heldKeys.clear();
				}
			});

			setBackground(Color.WHITE);
			setForeground(Color.BLACK);
			setCaretColor(Color.BLACK);

			mouseAdapter = new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					heldByClick = true;
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					heldByClick = false;
				}

				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					heldByScrollWheel = true;
				}
			};

			keyAdapter = new KeyAdapter() {
				private boolean isHandledKey(KeyEvent e) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_LEFT:
						case KeyEvent.VK_RIGHT:
						case KeyEvent.VK_UP:
						case KeyEvent.VK_DOWN:
							return true;
						default:
							return false;
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if (isHandledKey(e))
						heldKeys.add(Integer.valueOf(e.getKeyCode()));
				}

				@Override
				public void keyReleased(KeyEvent e) {
					if (isHandledKey(e))
						heldKeys.remove(Integer.valueOf(e.getKeyCode()));
				}
			};
		}

		private void addFirstMouseListener(Component c) {
			c.removeMouseListener(mouseAdapter);

			MouseListener[] existingClickListeners = c.getMouseListeners();
			for (int i = 0; i < existingClickListeners.length; i++)
				c.removeMouseListener(existingClickListeners[i]);

			c.addMouseListener(mouseAdapter);

			for (int i = 0; i < existingClickListeners.length; i++)
				c.addMouseListener(existingClickListeners[i]);
		}

		private void addLastMouseListener(Component c) {
			c.removeMouseListener(mouseAdapter);
			c.addMouseListener(mouseAdapter);
		}

		private void addLastKeyListener(Component c) {
			c.removeKeyListener(keyAdapter);
			c.addKeyListener(keyAdapter);
		}

		public void updateScrollBarInputListeners() {
			addLastKeyListener(this);
			addLastMouseListener(this);
			addLastMouseListener(scrollBar);
			for (Component c : scrollBar.getComponents())
				addFirstMouseListener(c);
		}

		private boolean scrollHold() {
			return heldByClick || heldByScrollWheel || !heldKeys.isEmpty();
		}

		public void setScrollBar(JScrollBar sb) {
			scrollBar = sb;
			updateScrollBarInputListeners();
			sb.addAdjustmentListener(new AdjustmentListener() {
				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					if (!scrollHold() && !e.getValueIsAdjusting())
						scrollBar.setValue(scrollBar.getMaximum());
				}
			});
		}

		@Override
		public void append(String str) {
			heldByScrollWheel = false;
			super.append(str);
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
			textbox.addKeyListener(new KeyAdapter() {
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
		JScrollPane outputPane = new JScrollPane(output,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		output.setScrollBar(outputPane.getVerticalScrollBar());
		outputPane.addMouseWheelListener(output.mouseAdapter);
		add(outputPane);

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
				output.setCaretColor(Color.WHITE);
			}
		});
	}

	protected void outputDisabled() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				output.setBackground(Color.WHITE);
				output.setForeground(Color.BLACK);
				output.setCaretColor(Color.BLACK);
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

	public void onLookAndFeelChanged() {
		output.updateScrollBarInputListeners();
	}
}
