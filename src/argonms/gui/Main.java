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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import argonms.gui.model.Configuration;
import argonms.gui.model.Environment;
import argonms.gui.model.Model;

/**
 * 
 * @author GoldenKevin
 */
public class Main {
	private static final Dimension SPLASH_SIZE = new Dimension(400, 250);

	private static void close(Model m) {
		m.stopAllProcesses();
		System.exit(0);
	}

	private static Model load(Container popup, final JLabel message, final String[] args) throws InterruptedException, InvocationTargetException {
		//wrapper object that we can pass to the anonymous classes.
		//this allows us to access objects created inside them.
		final Configuration[] config = new Configuration[1];
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				message.setText("Reading properties...");
				config[0] = new Configuration(args.length > 0 ? args[0] : Environment.DEFAULT_PROPS_FILE);
			}
		});
		config[0].initialize(popup);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				message.setText("Assigning uninitialized properties...");
			}
		});
		if (!config[0].checkUnsetProperties(popup))
			return null;
		final Model m = new Model(config[0]);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				message.setText("Applying properties...");
				JTabbedPane pane = new JTabbedPane();
				m.setView(pane);
				config[0].apply(m);
			}
		});
		return m;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private static void deiconify(JFrame window) {
		window.setVisible(true);
		int state = window.getExtendedState();
		state &= ~JFrame.ICONIFIED;
		window.setExtendedState(state);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private static boolean addTrayIcon(final JFrame window, final Model m) {
		SystemTray tray = SystemTray.getSystemTray();
		URL resource = Main.class.getResource("/argonms/gui/icon.png");
		if (resource == null) {
			JOptionPane.showMessageDialog(
				window,
				"TrayIcon could not be added: icon image does not exist!\n"
						+ "Continuing without using system tray functionalities...",
				"Tray Icon Warning",
				JOptionPane.WARNING_MESSAGE
			);
			return false;
		}
		Dimension trayIconSize = tray.getTrayIconSize();
		Image unscaled = Toolkit.getDefaultToolkit().getImage(resource);
		//TODO: get proper size for title bar icon - perhaps a square with sides that have a length equal to the height of the titlebar?
		window.setIconImage(unscaled.getScaledInstance(trayIconSize.width, trayIconSize.height, 0));
		PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(unscaled.getScaledInstance(trayIconSize.width, trayIconSize.height, 0), "ArgonMS Server Manager", popup);
		MenuItem mnuItem = new MenuItem("Show");
		mnuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deiconify(window);
			}
		});
		popup.add(mnuItem);
		mnuItem = new MenuItem("Hide");
		mnuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.setVisible(false);
			}
		});
		popup.add(mnuItem);
		popup.addSeparator();

		mnuItem = new MenuItem("Start All");
		mnuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m.startAllProcesses();
			}
		});
		popup.add(mnuItem);
		mnuItem = new MenuItem("Stop All");
		mnuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m.stopAllProcesses();
			}
		});
		popup.add(mnuItem);
		popup.addSeparator();

		mnuItem = new MenuItem("Exit");
		mnuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.dispose();
				trayIcon.displayMessage("ArgonMS Server Manager",
						"Shutting down all running servers...",
						TrayIcon.MessageType.INFO);
			}
		});
		popup.add(mnuItem);
		trayIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deiconify(window);
			}
		});
		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.err.println("Error in adding tray icon");
			e.printStackTrace();
			JOptionPane.showMessageDialog(
				window,
				"TrayIcon could not be added: " + e + "\nContinuing without using system tray functionalities...",
				"Tray Icon Warning",
				JOptionPane.WARNING_MESSAGE
			);
			return false;
		}
		return true;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private static JFrame constructWindow(final Model m, JLabel message) {
		final JFrame window = new JFrame("ArgonMS Server Manager");
		message.setText("Adding tray icon...");
		final boolean useTray = SystemTray.isSupported() && addTrayIcon(window, m);
		message.setText("Constructing main window...");
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.setResizable(false);

		window.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) { }

			@Override
			public void windowClosed(WindowEvent arg0) {
				close(m);
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				if (m.allOffline() || JOptionPane.showConfirmDialog(window,
						"Servers are currently running. Closing will shutdown the servers.\n"
								+ (useTray ? "You may minimize this window to hide it and remove it from the taskbar.\n" : "")
								+ "Do you really wish to exit?",
								"ArgonMS Server Manager",
								JOptionPane.YES_NO_OPTION
						) == JOptionPane.YES_OPTION)
					close(m);
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) { }

			@Override
			public void windowDeiconified(WindowEvent arg0) { }

			@Override
			public void windowIconified(WindowEvent arg0) {
				if (useTray)
					window.setVisible(false);
			}

			@Override
			public void windowOpened(WindowEvent arg0) { }
		});

		window.getContentPane().add(m.getView());
		return window;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private static JWindow constructSplash(JLabel message) {
		JWindow splash = new JWindow();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		splash.getContentPane().add(new JLabel("ArgonMS Server Manager"), BorderLayout.PAGE_START);
		splash.getContentPane().add(message, BorderLayout.PAGE_END);
		splash.setSize(SPLASH_SIZE);
		splash.setLocation((screenSize.width / 2) - (SPLASH_SIZE.width / 2),
				(screenSize.height / 2) - (SPLASH_SIZE.height / 2));
		return splash;
	}

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		//some wrapper objects that we can pass to the anonymous classes.
		//this allows us to access objects created inside them.
		final JWindow[] splash = new JWindow[1];
		final JLabel[] progressIndicator = new JLabel[1];
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				JLabel message = new JLabel("Please wait...");
				JWindow popup = constructSplash(message);
				popup.setVisible(true);

				splash[0] = popup;
				progressIndicator[0] = message;
			}
		});
		final Model m = load(splash[0], progressIndicator[0], args);

		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				if (m != null) {
					JFrame window = constructWindow(m, progressIndicator[0]);
					m.setFrame(window);
					window.pack();
					window.setVisible(true);
				}
				splash[0].setVisible(false);
				splash[0].dispose();
			}
		});
	}
}
