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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * @author GoldenKevin
 */
public class Configuration {
	@SuppressWarnings("serial")
	private static class SelectedFilesList extends JPanel {
		private static final File[] EMPTY_SELECTION = { new File("") };

		private Set<File> selected;
		private JList list;
		private JFileChooser choose;

		/**
		 * This method is not thread-safe. It must be called from the Swing EDT.
		 */
		public SelectedFilesList(final JFileChooser choose) {
			this.selected = new LinkedHashSet<File>();
			this.choose = choose;
			setPreferredSize(new Dimension(300, 200));
			JButton add = new JButton("Add ->");
			final DefaultListModel listModel = new DefaultListModel();
			add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<File> list = Arrays.asList(choose.getSelectedFiles());
					selected.addAll(list);
					listModel.clear();
					for (File file : selected)
						listModel.addElement(file.getName());
					choose.setSelectedFiles(EMPTY_SELECTION);
				}
			});
			add(add);
			list = new JList(listModel);
			list.setPreferredSize(new Dimension(250, 100));
			JScrollPane listPane = new JScrollPane(list);
			listPane.setPreferredSize(new Dimension(275, 125));
			add(listPane);
			JButton remove = new JButton("<- Remove");
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (Object v : list.getSelectedValues())
						listModel.removeElement(v);
				}
			});
			add(remove);
		}

		public String[] getSelected() throws IOException {
			List<File> list = Arrays.asList(choose.getSelectedFiles());
			selected.addAll(list);
			String[] ret = new String[selected.size()];
			int i = 0;
			for (File file : selected)
				ret[i++] = file.getCanonicalPath();
			return ret;
		}
	}

	private final String propsFileName;
	private final JFileChooser fChoose;
	private final FileFilter propsFilter;
	private final FileFilter textFileFilter;
	private final FileFilter classPathFilter;
	private final Properties props;

	private String[] classPath;
	private String dbPropPath;
	private String loggerPropPath;
	private String macBanBlacklistPath;
	private String wzPath;
	private String scriptsPath;
	private byte[] enabledGameServers;
	private final Map<Byte, String> gamePropPaths;
	private String loginPropPath;
	private boolean shopEnabled;
	private String shopPropPath;
	private String blockedCsSnsPath;
	private String centerPropPath;
	private String lookAndFeel;

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public Configuration(String propsFile) {
		propsFileName = propsFile;
		fChoose = new JFileChooser();
		propsFilter = new FileNameExtensionFilter("Java Properties Files (.properties)", "properties");
		textFileFilter = new FileNameExtensionFilter("Plain text file (.txt)", "txt");
		classPathFilter = new FileNameExtensionFilter("Java Archives (.jar) and class folders", "jar");
		props = new Properties();
		gamePropPaths = new HashMap<Byte, String>();
	}

	private boolean is7bitUnsignedNumber(String decimal) {
		if (decimal.length() > 3)
			return false;
		char[] array = decimal.toCharArray();
		for (char c : array)
			if (c < '0' || c > '9')
				return false;
		if (decimal.length() == 3) {
			if (array[0] > '1')
				return false;
			if (array[0] == '1') {
				if (array[1] > '2')
					return false;
				if (array[1] == '2')
					if (array[2] > '7')
						return false;
			}
		}
		return true;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void lookAndFeelUpdated() {
		SwingUtilities.updateComponentTreeUI(fChoose);
	}

	public void initialize(final Container parent) {
		try {
			props.load(new FileReader(propsFileName));
		} catch (FileNotFoundException e) {
			//this is expected...
		} catch (IOException e) {
			System.err.println("Error reading " + propsFileName);
			e.printStackTrace();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(parent, "Could not load from properties file.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		for (Entry<Object, Object> prop : props.entrySet()) {
			String key = (String) prop.getKey();
			if (!key.startsWith("argonms.gui"))
				continue;
			String[] splittedKey = key.split("\\.");
			if (splittedKey[2].equals("bin")) {
				if (splittedKey[3].equals("classpath"))
					classPath = ((String) prop.getValue()).split(",");
			} else if (splittedKey[2].equals("database")) {
				if (splittedKey[3].equals("properties"))
					dbPropPath = (String) prop.getValue();
			} else if (splittedKey[2].equals("logger")) {
				if (splittedKey[3].equals("properties"))
					loggerPropPath = (String) prop.getValue();
			} else if (splittedKey[2].equals("cheattracker")) {
				if (splittedKey[3].equals("macbanblacklist"))
					macBanBlacklistPath = (String) prop.getValue();
			} else if (splittedKey[2].equals("wz")) {
				if (splittedKey[3].equals("location"))
					wzPath = (String) prop.getValue();
			} else if (splittedKey[2].equals("scripts")) {
				if (splittedKey[3].equals("location"))
					scriptsPath = (String) prop.getValue();
			} else if (splittedKey[2].equals("game")) {
				if (splittedKey[3].equals("run")) {
					String value = ((String) prop.getValue());
					if (!value.isEmpty()) {
						String[] serverIds = value.split(",");
						enabledGameServers = new byte[serverIds.length];
						for (byte i = 0; i < serverIds.length; i++)
							enabledGameServers[i] = Byte.parseByte(serverIds[i]);
					} else {
						enabledGameServers = new byte[0];
					}
				} else if (is7bitUnsignedNumber(splittedKey[3])){
					byte serverId = Byte.parseByte(splittedKey[3]);
					if (splittedKey[4].equals("properties"))
						gamePropPaths.put(Byte.valueOf(serverId), (String) prop.getValue());
				}
			} else if (splittedKey[2].equals("login")) {
				if (splittedKey[3].equals("properties"))
					loginPropPath = (String) prop.getValue();
			} else if (splittedKey[2].equals("shop")) {
				if (splittedKey[3].equals("run"))
					shopEnabled = Boolean.parseBoolean((String) prop.getValue());
				else if (splittedKey[3].equals("properties"))
					shopPropPath = (String) prop.getValue();
				else if (splittedKey[3].equals("blockedserials"))
					blockedCsSnsPath = (String) prop.getValue();
			} else if (splittedKey[2].equals("center")) {
				if (splittedKey[3].equals("properties"))
					centerPropPath = (String) prop.getValue();
			} else if (splittedKey[2].equals("plaf")) {
				lookAndFeel = (String) prop.getValue();
				try {
					UIManager.setLookAndFeel(lookAndFeel);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							lookAndFeelUpdated();
						}
					});
				} catch (Exception e) {
					System.err.println("Error setting look and feel");
					e.printStackTrace();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(parent, "Look and feel saved in properties file could not be found.\nUsing default instead.", "Invalid PLAF", JOptionPane.WARNING_MESSAGE);
						}
					});
				}
			}
		}
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public String promptForDirectory(Component parent, String prompt) throws IOException {
		fChoose.setDialogTitle(prompt);
		fChoose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fChoose.showDialog(parent, "Select");
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return fChoose.getSelectedFile().getCanonicalPath() + Environment.DIR_DELIMIT;
		else
			return null;
	}

	/**
	 * Do not invoke this from the Swing EDT, or else a deadlock may result.
	 */
	private String safePromptForDirectory(final Component parent, final String prompt) throws Throwable {
		//some wrapper objects that we can pass to the anonymous classes.
		//this allows us to access objects created inside them.
		final String[] result = new String[1];
		final Throwable[] exception = new Throwable[1];
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						result[0] = promptForDirectory(parent, prompt);
					} catch (IOException e) {
						exception[0] = e;
					}
				}
			});
		} catch (InterruptedException e) {
			exception[0] = e;
		} catch (InvocationTargetException e) {
			exception[0] = e.getCause();
		}
		if (exception[0] != null)
			throw exception[0];
		return result[0];
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public String promptForPropsFile(Component parent, String prompt) throws IOException {
		fChoose.setDialogTitle(prompt);
		fChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fChoose.addChoosableFileFilter(propsFilter);
		int returnVal = fChoose.showDialog(parent, "Select");
		try {
			if (returnVal == JFileChooser.APPROVE_OPTION)
				return fChoose.getSelectedFile().getCanonicalPath();
			else
				return null;
		} finally {
			fChoose.removeChoosableFileFilter(propsFilter);
		}
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public String promptForTextFile(Component parent, String prompt) throws IOException {
		fChoose.setDialogTitle(prompt);
		fChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fChoose.addChoosableFileFilter(textFileFilter);
		int returnVal = fChoose.showDialog(parent, "Select");
		try {
			if (returnVal == JFileChooser.APPROVE_OPTION)
				return fChoose.getSelectedFile().getCanonicalPath();
			else
				return null;
		} finally {
			fChoose.removeChoosableFileFilter(propsFilter);
		}
	}

	/**
	 * Do not invoke this from the Swing EDT, or else a deadlock may result.
	 */
	private String safePromptForTextFile(final Component parent, final String prompt) throws Throwable {
		//some wrapper objects that we can pass to the anonymous classes.
		//this allows us to access objects created inside them.
		final String[] result = new String[1];
		final Throwable[] exception = new Throwable[1];
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						result[0] = promptForTextFile(parent, prompt);
					} catch (IOException e) {
						exception[0] = e;
					}
				}
			});
		} catch (InterruptedException e) {
			exception[0] = e;
		} catch (InvocationTargetException e) {
			exception[0] = e.getCause();
		}
		if (exception[0] != null)
			throw exception[0];
		return result[0];
	}

	/**
	 * Do not invoke this from the Swing EDT, or else a deadlock may result.
	 */
	private String safePromptForPropsFile(final Component parent, final String prompt) throws Throwable {
		//some wrapper objects that we can pass to the anonymous classes.
		//this allows us to access objects created inside them.
		final String[] result = new String[1];
		final Throwable[] exception = new Throwable[1];
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						result[0] = promptForPropsFile(parent, prompt);
					} catch (IOException e) {
						exception[0] = e;
					}
				}
			});
		} catch (InterruptedException e) {
			exception[0] = e;
		} catch (InvocationTargetException e) {
			exception[0] = e.getCause();
		}
		if (exception[0] != null)
			throw exception[0];
		return result[0];
	}

	//TODO: allow approve when there is no selection
	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public String[] promptForClasspath(Component parent, String prompt) throws IOException {
		fChoose.addChoosableFileFilter(classPathFilter);
		SelectedFilesList list = new SelectedFilesList(fChoose);
		fChoose.setDialogTitle(prompt);
		fChoose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fChoose.setAccessory(list);
		fChoose.setMultiSelectionEnabled(true);
		int returnVal = fChoose.showDialog(parent, "Finish");
		try {
			if (returnVal == JFileChooser.APPROVE_OPTION)
				return list.getSelected();
			else
				return null;
		} finally {
			fChoose.removeChoosableFileFilter(classPathFilter);
			fChoose.setAccessory(null);
			fChoose.setMultiSelectionEnabled(false);
		}
	}

	/**
	 * Do not invoke this from the Swing EDT, or else a deadlock may result.
	 */
	private String[] safePromptForClasspath(final Component parent, final String prompt) throws Throwable {
		//some wrapper objects that we can pass to the anonymous classes.
		//this allows us to access objects created inside them.
		final String[][] result = new String[1][];
		final Throwable[] exception = new Throwable[1];
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						result[0] = promptForClasspath(parent, prompt);
					} catch (IOException e) {
						exception[0] = e;
					}
				}
			});
		} catch (InterruptedException e) {
			exception[0] = e;
		} catch (InvocationTargetException e) {
			exception[0] = e.getCause();
		}
		if (exception[0] != null)
			throw exception[0];
		return result[0];
	}

	private String createList(String[] list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.length; i++)
			sb.append(list[i]).append(',');
		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * Do not invoke this from the Swing EDT, or else a deadlock may result.
	 * @param parent
	 * @return
	 */
	public boolean checkUnsetProperties(final Container parent) {
		boolean doStore = false;
		try {
			try {
				if (classPath == null) {
					String[] selected = safePromptForClasspath(parent, "Select your classpath");
					if (selected == null)
						return false;
					classPath = selected;
					props.setProperty("argonms.gui.bin.classpath", createList(selected));
					doStore = true;
				}
				if (wzPath == null) {
					String selected = safePromptForDirectory(parent, "Select your WZ data folder");
					if (selected == null)
						return false;
					wzPath = selected;
					props.setProperty("argonms.gui.wz.location", selected);
					doStore = true;
				}
				if (scriptsPath == null) {
					String selected = safePromptForDirectory(parent, "Select your scripts folder");
					if (selected == null)
						return false;
					scriptsPath = selected;
					props.setProperty("argonms.gui.scripts.location", selected);
					doStore = true;
				}
				if (dbPropPath == null) {
					String selected = safePromptForPropsFile(parent, "Select your db.properties file");
					if (selected == null)
						return false;
					dbPropPath = selected;
					props.setProperty("argonms.gui.database.properties", selected);
					doStore = true;
				}
				if (loggerPropPath == null) {
					String selected = safePromptForPropsFile(parent, "Select your logging.properties file");
					if (selected == null)
						return false;
					loggerPropPath = selected;
					props.setProperty("argonms.gui.logger.properties", selected);
					doStore = true;
				}
				if (macBanBlacklistPath == null) {
					String selected = safePromptForTextFile(parent, "Select your macbanblacklist.txt file");
					if (selected == null)
						return false;
					macBanBlacklistPath = selected;
					props.setProperty("argonms.gui.cheattracker.macbanblacklist", selected);
					doStore = true;
				}
				if (enabledGameServers == null) {
					props.setProperty("argonms.gui.game.run", "0");
					enabledGameServers = new byte[] { 0 };
					doStore = true;
				}
				for (byte serverId : enabledGameServers) {
					if (!gamePropPaths.containsKey(Byte.valueOf(serverId))) {
						String selected = safePromptForPropsFile(parent, "Select your game" + serverId + ".properties file");
						if (selected == null)
							return false;
						gamePropPaths.put(Byte.valueOf(serverId), selected);
						props.setProperty("argonms.gui.game." + serverId + ".properties", selected);
						doStore = true;
					}
				}
				if (loginPropPath == null) {
					String selected = safePromptForPropsFile(parent, "Select your login.properties file");
					if (selected == null)
						return false;
					loginPropPath = selected;
					props.setProperty("argonms.gui.login.properties", selected);
					doStore = true;
				}
				if (shopEnabled && shopPropPath == null) {
					String selected = safePromptForPropsFile(parent, "Select your shop.properties file");
					if (selected == null)
						return false;
					shopPropPath = selected;
					props.setProperty("argonms.gui.shop.properties", selected);
					doStore = true;
				}
				if (shopEnabled && blockedCsSnsPath == null) {
					String selected = safePromptForTextFile(parent, "Select your blockedcashshopserialnumbers.txt file");
					if (selected == null)
						return false;
					blockedCsSnsPath = selected;
					props.setProperty("argonms.gui.shop.blockedserials", selected);
					doStore = true;
				}
				if (centerPropPath == null) {
					String selected = safePromptForPropsFile(parent, "Select your center.properties file");
					if (selected == null)
						return false;
					centerPropPath = selected;
					props.setProperty("argonms.gui.center.properties", selected);
					doStore = true;
				}
				if (lookAndFeel == null) {
					lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
					props.setProperty("argonms.gui.plaf", lookAndFeel);
					doStore = true;
				}
			} finally {
				if (doStore)
					props.store(new FileWriter(propsFileName), null);
			}
			return true;
		} catch (final Throwable e) {
			System.err.println("Error assigning properties");
			e.printStackTrace();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(parent, "Could not set uninitialized properties: " + e.getMessage(), "Could not update properties", JOptionPane.ERROR_MESSAGE);
				}
			});
			return false;
		}
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void apply(final Model m) {
		m.addMainTab();
		m.addCenterTab();
		m.addLoginTab();
		for (byte serverId : enabledGameServers)
			m.addGameTab(serverId);
		if (shopEnabled)
			m.addShopTab();
		m.addTelnetTab();
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void updateProps(Map<String, String> updatedProps, Container parent) {
		if (updatedProps.size() > 0) {
			for (Entry<String, String> entry : updatedProps.entrySet())
				props.setProperty(entry.getKey(), entry.getValue());
			try {
				props.store(new FileWriter(propsFileName), null);
			} catch (IOException e) {
				System.err.println("Error updating properties");
				e.printStackTrace();
				JOptionPane.showMessageDialog(parent, "Could not set updated properties: " + e.getMessage(), "Could not update properties", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public byte[] getEnabledGameServers() {
		return enabledGameServers;
	}

	public boolean isShopEnabled() {
		return shopEnabled;
	}

	public String getClasspath() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < classPath.length; i++)
			sb.append(classPath[i]).append(Environment.LIST_DELIMIT);
		return sb.substring(0, sb.length() - Environment.LIST_DELIMIT.length());
	}

	public String[] getClasspathElements() {
		return classPath;
	}

	public String getDatabasePropertiesPath() {
		return dbPropPath;
	}

	public String getLoggerPropertiesPath() {
		return loggerPropPath;
	}

	public String getMacBanBlacklistPath() {
		return macBanBlacklistPath;
	}

	public String getWzPath() {
		return wzPath;
	}

	public String getScriptsPath() {
		return scriptsPath;
	}

	public String getGameServerPropertiesPath(byte serverId) {
		return gamePropPaths.get(Byte.valueOf(serverId));
	}

	public String getLoginServerPropertiesPath() {
		return loginPropPath;
	}

	public String getShopServerPropertiesPath() {
		return shopPropPath;
	}

	public String getBlockedCashSerialsPath() {
		return blockedCsSnsPath;
	}

	public String getCenterServerPropertiesPath() {
		return centerPropPath;
	}

	public String getLookAndFeelClass() {
		return lookAndFeel;
	}

	public void setLookAndFeelClass(String className) {
		lookAndFeel = className;
	}
}
