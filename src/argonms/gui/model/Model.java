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

package argonms.gui.model;

import java.awt.Window;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import argonms.gui.tab.CenterServerTab;
import argonms.gui.tab.ConsoleTab;
import argonms.gui.tab.GameServerTab;
import argonms.gui.tab.LoginServerTab;
import argonms.gui.tab.MainTab;
import argonms.gui.tab.ServerTab;
import argonms.gui.tab.ShopServerTab;
import argonms.gui.tab.TelnetTab;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author GoldenKevin
 */
public class Model {
	private final Configuration config;
	private JTabbedPane view;
	private JFrame frame;
	private final Set<ServerTab> runningServers;
	private final Set<ServerTab> idleServers;
	private final SortedMap<Byte, GameServerTab> gameTabs;
	private MainTab startTab;
	private LoginServerTab loginTab;
	private ShopServerTab shopTab;
	private CenterServerTab centerTab;
	private TelnetTab telnetTab;

	public Model(Configuration cfg) {
		config = cfg;
		//not for thread safety, but for the weakly consistent iterators
		//(we may remove an element while iterating over it on the same thread)
		runningServers = Collections.newSetFromMap(new ConcurrentHashMap<ServerTab, Boolean>());
		idleServers = Collections.newSetFromMap(new ConcurrentHashMap<ServerTab, Boolean>());
		gameTabs = new TreeMap<Byte, GameServerTab>();
	}

	public void setView(JTabbedPane pane) {
		view = pane;
	}

	public void setFrame(JFrame window) {
		frame = window;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void updateLookAndFeel(String laf, Window... additional) {
		try {
			UIManager.setLookAndFeel(laf);
			SwingUtilities.updateComponentTreeUI(frame);
			frame.pack();
			config.lookAndFeelUpdated();
			for (Window w : additional) {
				SwingUtilities.updateComponentTreeUI(w);
				w.pack();
			}
			for (ConsoleTab tab : idleServers)
				tab.onLookAndFeelChanged();
			for (ConsoleTab tab : runningServers)
				tab.onLookAndFeelChanged();
			telnetTab.onLookAndFeelChanged();
		} catch (ClassNotFoundException e) {
			System.err.println("Error refreshing look and feel");
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Look and feel not found: " + e.getMessage(), "Could not change PLAF", JOptionPane.ERROR_MESSAGE);
		} catch (ClassCastException e) {
			System.err.println("Error refreshing look and feel");
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Look and feel not found: " + e.getMessage(), "Could not change PLAF", JOptionPane.ERROR_MESSAGE);
		} catch (InstantiationException e) {
			System.err.println("Error refreshing look and feel");
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Look and feel could not be created: " + e.getMessage(), "Could not change PLAF", JOptionPane.ERROR_MESSAGE);
		} catch (IllegalAccessException e) {
			System.err.println("Error refreshing look and feel");
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Look and feel could not be created: " + e.getMessage(), "Could not change PLAF", JOptionPane.ERROR_MESSAGE);
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println("Error refreshing look and feel");
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Look and feel not supported: " + e.getMessage(), "Could not change PLAF", JOptionPane.ERROR_MESSAGE);
		}
	}

	public JTabbedPane getView() {
		return view;
	}

	public JFrame getFrame() {
		return frame;
	}

	public Configuration getConfig() {
		return config;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void addMainTab() {
		startTab = new MainTab(this);
		view.insertTab("Main", null, startTab, null, 0);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void addCenterTab() {
		centerTab = new CenterServerTab(this);
		view.insertTab("Center", null, centerTab, "Not running", 1);
		idleServers.add(centerTab);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void addLoginTab() {
		loginTab = new LoginServerTab(this);
		view.insertTab("Login", null, loginTab, "Not running", 2);
		idleServers.add(loginTab);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void addGameTab(byte serverId) {
		GameServerTab tab = new GameServerTab(this, serverId);
		gameTabs.put(Byte.valueOf(serverId), tab);
		int index = 3 + gameTabs.headMap(Byte.valueOf(serverId)).size();
		view.insertTab("Game" + serverId, null, tab, "Not running", index);
		idleServers.add(tab);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void removeGameTab(byte serverId) {
		idleServers.remove(gameTabs.remove(Byte.valueOf(serverId)));
		int index = 3 + gameTabs.headMap(Byte.valueOf(serverId)).size();
		view.removeTabAt(index);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void addShopTab() {
		shopTab = new ShopServerTab(this);
		int index = 3 + gameTabs.size();
		view.insertTab("Shop", null, shopTab, "Not running", index);
		idleServers.add(shopTab);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void removeShopTab() {
		idleServers.remove(shopTab);
		shopTab = null;
		int index = 3 + gameTabs.size();
		view.removeTabAt(index);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void addTelnetTab() {
		telnetTab = new TelnetTab(this);
		int index = 3 + gameTabs.size() + (config.isShopEnabled() ? 1 : 0);
		view.insertTab("Telnet", null, telnetTab, "Not connected", index);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void removeTelnetTab() {
		telnetTab = null;
		int index = 3 + gameTabs.size() + (config.isShopEnabled() ? 1 : 0);
		view.removeTabAt(index);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void startAllProcesses() {
		for (Iterator<ServerTab> iter = idleServers.iterator(); iter.hasNext(); ) {
			ServerTab tab = iter.next();
			tab.startProcess();
		}
	}

	public void stopAllProcesses() {
		for (Iterator<ServerTab> iter = runningServers.iterator(); iter.hasNext(); ) {
			ServerTab tab = iter.next();
			tab.stopProcess();
		}
	}

	public boolean allOffline() {
		return runningServers.isEmpty();
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void processStarted(ServerTab tab) {
		idleServers.remove(tab);
		runningServers.add(tab);
		view.setToolTipTextAt(view.indexOfComponent(tab), "Running");
		startTab.enableStopAllButton();
		startTab.disableSettings();
		if (idleServers.isEmpty())
			startTab.disableStartAllButton();
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void processEnded(ServerTab tab) {
		runningServers.remove(tab);
		idleServers.add(tab);
		view.setToolTipTextAt(view.indexOfComponent(tab), "Not running");
		startTab.enableStartAllButton();
		if (runningServers.isEmpty()) {
			startTab.disableStopAllButton();
			startTab.enableSettings();
		}
	}
}
