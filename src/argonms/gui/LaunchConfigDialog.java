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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import argonms.gui.model.Model;

/**
 * 
 * @author GoldenKevin
 */
@SuppressWarnings("serial")
public class LaunchConfigDialog extends JDialog {
	private final Model m;
	private final Map<String, String> changes;
	private JButton applyButton;

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public LaunchConfigDialog(Model model) {
		super(model.getFrame(), "ArgonMS Launch Configuration", true);
		m = model;
		changes = new HashMap<String, String>();

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (!changes.isEmpty()) {
					int option = JOptionPane.showConfirmDialog(LaunchConfigDialog.this,
							"You have modified some settings without saving.\n"
									+ "Would you like to save the unsaved changes?",
							"Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
					switch (option) {
						case JOptionPane.YES_OPTION:
							applyChanges();
							dispose();
							break;
						case JOptionPane.NO_OPTION:
							dispose();
							break;
						case JOptionPane.CANCEL_OPTION:
							break;
					}
				} else {
					dispose();
				}
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		//left column
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		add(constructCenterServerPanel(), c);
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 1;
		c.gridheight = 4;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0;
		add(constructGameServersPanel(), c);
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 5;
		c.weightx = 0;
		add(constructClasspathPanel(), c);
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 6;
		c.weightx = 0;
		add(constructWzPathPanel(), c);
		c.anchor = GridBagConstraints.LAST_LINE_START;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 7;
		c.weightx = 0;
		add(constructScriptPathPanel(), c);

		//right column
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0;
		add(constructLoginServerPanel(), c);
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 1;
		c.gridheight = 4;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		add(constructShopServerPanel(), c);
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 5;
		c.weightx = 1;
		add(constructDatabasePropertiesPanel(), c);
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 6;
		c.weightx = 1;
		add(constructLoggerPropertiesPanel(), c);
		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 7;
		c.weightx = 1;
		add(constructMacBanBlacklistPathPanel(), c);

		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 8;
		c.weightx = 1;
		add(constructDialogButtonsPanel(), c);
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructSimplePathSubPanel(String title, String labelText, JTextField textbox, String textboxDefaultText, MouseListener clickHandler) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), title));
		JLabel label = new JLabel(labelText);

		textbox.setText(textboxDefaultText);
		textbox.setColumns(10);
		textbox.setEditable(false);
		textbox.addMouseListener(clickHandler);

		panel.add(label);
		panel.add(textbox);
		return panel;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructCenterServerPanel() {
		final JTextField textbox = new JTextField();
		return constructSimplePathSubPanel("Center", "Center Properties:", textbox, m.getConfig().getCenterServerPropertiesPath(), new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					String selected = m.getConfig().promptForPropsFile(LaunchConfigDialog.this, "Select your center.properties file");
					if (selected != null) {
						textbox.setText(selected);
						changes.put("argonms.gui.center.properties", selected);
						applyButton.setEnabled(true);
					}
				} catch (IOException ex) {
					System.err.println("Error in selecting center.properties");
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
						LaunchConfigDialog.this,
						"Error while changing settings: " + ex,
						"Settings Changing Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructLoginServerPanel() {
		final JTextField textbox = new JTextField();
		return constructSimplePathSubPanel("Login", "Login Properties:", textbox, m.getConfig().getLoginServerPropertiesPath(), new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					String selected = m.getConfig().promptForPropsFile(LaunchConfigDialog.this, "Select your login.properties file");
					if (selected != null) {
						textbox.setText(selected);
						changes.put("argonms.gui.login.properties", selected);
						applyButton.setEnabled(true);
					}
				} catch (IOException ex) {
					System.err.println("Error in selecting login.properties");
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
						LaunchConfigDialog.this,
						"Error while changing settings: " + ex,
						"Settings Changing Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructShopServerPanel() {
		JPanel shopServerPanel = new JPanel();
		shopServerPanel.setLayout(new GridLayout(5, 1, 5, 5));
		shopServerPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Shop"));
		final JCheckBox chkRun = new JCheckBox("Run shop server", null, m.getConfig().isShopEnabled());
		final JLabel shopPropsLbl = new JLabel("Shop Properties:");
		final JTextField shopPropsUrl = new JTextField(m.getConfig().getShopServerPropertiesPath(), 10);
		final JLabel blockedSerialsLbl = new JLabel("Blocked SNs:");
		final JTextField blockedSerialsUrl = new JTextField(m.getConfig().getCashShopBlockedSerialsPath(), 10);
		final JLabel commodityOverridesLbl = new JLabel("Commodity overrides:");
		final JTextField commodityOverridesUrl = new JTextField(m.getConfig().getCashShopCommodityOverridesPath(), 10);
		final JLabel limitedCommoditiesLbl = new JLabel("Limited commodities:");
		final JTextField limitedCommoditiesUrl = new JTextField(m.getConfig().getCashShopLimitedCommoditiesPath(), 10);
		chkRun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chkRun.isSelected()) {
					try {
						String selected = m.getConfig().getShopServerPropertiesPath();
						if (selected == null)
							selected = m.getConfig().promptForPropsFile(LaunchConfigDialog.this, "Select your shop.properties file");
						if (selected != null) {
							shopPropsUrl.setEnabled(true);
							shopPropsLbl.setEnabled(true);
							shopPropsUrl.setText(selected);
							changes.put("argonms.gui.shop.properties", selected);
							applyButton.setEnabled(true);

							selected = m.getConfig().getCashShopBlockedSerialsPath();
							if (selected == null)
								selected = m.getConfig().promptForTextFile(LaunchConfigDialog.this, "Select your cashshopblockedserialnumbers.txt file");
							if (selected != null) {
								blockedSerialsUrl.setEnabled(true);
								blockedSerialsLbl.setEnabled(true);
								blockedSerialsUrl.setText(selected);
								changes.put("argonms.gui.shop.blockedserials", selected);
								applyButton.setEnabled(true);

								selected = m.getConfig().getCashShopCommodityOverridesPath();
								if (selected == null)
									selected = m.getConfig().promptForTextFile(LaunchConfigDialog.this, "Select your cashshopcommodityoverrides.txt file");
								if (selected != null) {
									commodityOverridesUrl.setEnabled(true);
									commodityOverridesLbl.setEnabled(true);
									commodityOverridesUrl.setText(selected);
									changes.put("argonms.gui.shop.commodityoverride", selected);
									applyButton.setEnabled(true);

									selected = m.getConfig().getCashShopLimitedCommoditiesPath();
									if (selected == null)
										selected = m.getConfig().promptForTextFile(LaunchConfigDialog.this, "Select your cashshoplimitedcommodities.txt file");
									if (selected != null) {
										limitedCommoditiesUrl.setEnabled(true);
										limitedCommoditiesLbl.setEnabled(true);
										changes.put("argonms.gui.shop.run", "true");
										limitedCommoditiesUrl.setText(selected);
										changes.put("argonms.gui.shop.limitedcommodity", selected);
										applyButton.setEnabled(true);
									} else {
										limitedCommoditiesUrl.setEnabled(false);
										limitedCommoditiesLbl.setEnabled(false);
										limitedCommoditiesUrl.setText(null);
										chkRun.setSelected(false);
									}
								} else {
									commodityOverridesUrl.setEnabled(false);
									commodityOverridesLbl.setEnabled(false);
									commodityOverridesUrl.setText(null);
									chkRun.setSelected(false);
								}
							} else {
								blockedSerialsUrl.setEnabled(false);
								blockedSerialsLbl.setEnabled(false);
								blockedSerialsUrl.setText(null);
								chkRun.setSelected(false);
							}
						} else {
							shopPropsUrl.setEnabled(false);
							shopPropsLbl.setEnabled(false);
							shopPropsUrl.setText(null);
							chkRun.setSelected(false);
						}
					} catch (IOException ex) {
						System.err.println("Error in selecting shop.properties");
						ex.printStackTrace();
						JOptionPane.showMessageDialog(
							LaunchConfigDialog.this,
							"Error while changing settings: " + ex,
							"Settings Changing Error",
							JOptionPane.ERROR_MESSAGE
						);
						chkRun.setSelected(false);
					}
				} else {
					shopPropsUrl.setEnabled(false);
					shopPropsLbl.setEnabled(false);
					blockedSerialsUrl.setEnabled(false);
					blockedSerialsLbl.setEnabled(false);
					commodityOverridesUrl.setEnabled(false);
					commodityOverridesLbl.setEnabled(false);
					limitedCommoditiesUrl.setEnabled(false);
					limitedCommoditiesLbl.setEnabled(false);
					changes.put("argonms.gui.shop.run", "false");
					applyButton.setEnabled(true);
				}
			}
		});
		if (chkRun.isSelected()) {
			shopPropsUrl.setEnabled(true);
			shopPropsLbl.setEnabled(true);
			blockedSerialsUrl.setEnabled(true);
			blockedSerialsLbl.setEnabled(true);
			commodityOverridesUrl.setEnabled(true);
			commodityOverridesLbl.setEnabled(true);
			limitedCommoditiesUrl.setEnabled(true);
			limitedCommoditiesLbl.setEnabled(true);
		} else {
			shopPropsUrl.setEnabled(false);
			shopPropsLbl.setEnabled(false);
			blockedSerialsUrl.setEnabled(false);
			blockedSerialsLbl.setEnabled(false);
			commodityOverridesUrl.setEnabled(false);
			commodityOverridesLbl.setEnabled(false);
			limitedCommoditiesUrl.setEnabled(false);
			limitedCommoditiesLbl.setEnabled(false);
		}
		shopPropsUrl.setEditable(false);
		shopPropsUrl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (shopPropsUrl.isEnabled()) {
					try {
						String selected = m.getConfig().promptForPropsFile(LaunchConfigDialog.this, "Select your shop.properties file");
						if (selected != null) {
							shopPropsUrl.setText(selected);
							changes.put("argonms.gui.shop.properties", selected);
							applyButton.setEnabled(true);
						}
					} catch (IOException ex) {
						System.err.println("Error in selecting shop.properties");
						ex.printStackTrace();
						JOptionPane.showMessageDialog(
							LaunchConfigDialog.this,
							"Error while changing settings: " + ex,
							"Settings Changing Error",
							JOptionPane.ERROR_MESSAGE
						);
					}
				}
			}
		});

		blockedSerialsUrl.setEditable(false);
		blockedSerialsUrl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (blockedSerialsUrl.isEnabled()) {
					try {
						String selected = m.getConfig().promptForTextFile(LaunchConfigDialog.this, "Select your cashshopblockedserialnumbers.txt file");
						if (selected != null) {
							blockedSerialsUrl.setText(selected);
							changes.put("argonms.gui.shop.blockedserials", selected);
							applyButton.setEnabled(true);
						}
					} catch (IOException ex) {
						System.err.println("Error in selecting cashshopblockedserialnumbers.txt");
						ex.printStackTrace();
						JOptionPane.showMessageDialog(
							LaunchConfigDialog.this,
							"Error while changing settings: " + ex,
							"Settings Changing Error",
							JOptionPane.ERROR_MESSAGE
						);
					}
				}
			}
		});

		commodityOverridesUrl.setEditable(false);
		commodityOverridesUrl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (commodityOverridesUrl.isEnabled()) {
					try {
						String selected = m.getConfig().promptForTextFile(LaunchConfigDialog.this, "Select your cashshopcommodityoverrides.txt file");
						if (selected != null) {
							commodityOverridesUrl.setText(selected);
							changes.put("argonms.gui.shop.commodityoverride", selected);
							applyButton.setEnabled(true);
						}
					} catch (IOException ex) {
						System.err.println("Error in selecting cashshopcommodityoverrides.txt");
						ex.printStackTrace();
						JOptionPane.showMessageDialog(
							LaunchConfigDialog.this,
							"Error while changing settings: " + ex,
							"Settings Changing Error",
							JOptionPane.ERROR_MESSAGE
						);
					}
				}
			}
		});

		limitedCommoditiesUrl.setEditable(false);
		limitedCommoditiesUrl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (commodityOverridesUrl.isEnabled()) {
					try {
						String selected = m.getConfig().promptForTextFile(LaunchConfigDialog.this, "Select your cashshoplimitedcommodities.txt file");
						if (selected != null) {
							limitedCommoditiesUrl.setText(selected);
							changes.put("argonms.gui.shop.limitedcommodity", selected);
							applyButton.setEnabled(true);
						}
					} catch (IOException ex) {
						System.err.println("Error in selecting cashshoplimitedcommodities.txt");
						ex.printStackTrace();
						JOptionPane.showMessageDialog(
							LaunchConfigDialog.this,
							"Error while changing settings: " + ex,
							"Settings Changing Error",
							JOptionPane.ERROR_MESSAGE
						);
					}
				}
			}
		});

		//layout and place on panel
		shopServerPanel.add(chkRun);
		JPanel urlPanel = new JPanel();
		urlPanel.setLayout(new GridLayout(1, 2));
		urlPanel.add(shopPropsLbl);
		urlPanel.add(shopPropsUrl);
		shopServerPanel.add(urlPanel);
		urlPanel = new JPanel();
		urlPanel.setLayout(new GridLayout(1, 2));
		urlPanel.add(blockedSerialsLbl);
		urlPanel.add(blockedSerialsUrl);
		shopServerPanel.add(urlPanel);
		urlPanel = new JPanel();
		urlPanel.setLayout(new GridLayout(1, 2));
		urlPanel.add(commodityOverridesLbl);
		urlPanel.add(commodityOverridesUrl);
		shopServerPanel.add(urlPanel);
		urlPanel = new JPanel();
		urlPanel.setLayout(new GridLayout(1, 2));
		urlPanel.add(limitedCommoditiesLbl);
		urlPanel.add(limitedCommoditiesUrl);
		shopServerPanel.add(urlPanel);
		return shopServerPanel;
	}

	private static boolean isAllowedGameId(String str) {
		byte b;
		try {
			b = Byte.parseByte(str);
			if (b < 0)
				return false;
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static String createListWithAddition(byte[] original, byte add) {
		StringBuilder sb = new StringBuilder();
		for (byte element : original)
			sb.append(element).append(',');
		sb.append(add);
		return sb.toString();
	}

	private static <T> String createListWithAddition(T[] original, T add) {
		StringBuilder sb = new StringBuilder();
		for (T element : original)
			sb.append(element).append(',');
		if (add != null) {
			sb.append(add);
			return sb.toString();
		}
		return sb.length() != 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
	}

	private static String createListWithExclusion(byte[] original, byte exclude) {
		StringBuilder sb = new StringBuilder();
		for (byte element : original)
			if (element != exclude)
				sb.append(element).append(',');
		return sb.length() != 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
	}

	private static <T> String createListWithExclusion(T[] original, T exclude) {
		StringBuilder sb = new StringBuilder();
		for (T element : original)
			if (!element.equals(exclude))
				sb.append(element).append(',');
		return sb.length() != 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
	}

	private static boolean contains(byte[] array, byte search) {
		for (byte b : array)
			if (b == search)
				return true;
		return false;
	}

	//TODO: use a JComboBox instead of a JList and JTextBox. listen for every key typed.
	//if value typed equals a value of the dropdown, then disable Add and enable Remove and Edit.
	//else enable Add and disable Remove and Edit.
	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructGameServersPanel() {
		//set up the panel and its border
		JPanel gameServersPanel = new JPanel();
		gameServersPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Game"));

		//size formatting and all
		final JTextField gameServerIdEntry = new JTextField(3);
		JButton editServerButton = new JButton("Edit Game Server Settings");
		JButton removeServerButton = new JButton("Remove Game Server");
		JButton addServerButton = new JButton("Add Game Server");
		final DefaultListModel gameServersList = new DefaultListModel();
		for (byte serverId : m.getConfig().getEnabledGameServers())
			gameServersList.addElement(Byte.valueOf(serverId));
		final JList activeGameServers = new JList(gameServersList);
		activeGameServers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		activeGameServers.setVisibleRowCount(5);
		JScrollPane activeGameServersPane = new JScrollPane(activeGameServers);
		activeGameServersPane.setPreferredSize(new Dimension(gameServerIdEntry.getPreferredSize().width + activeGameServersPane.getVerticalScrollBar().getPreferredSize().width, activeGameServersPane.getPreferredSize().height));

		//add functionality
		editServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = activeGameServers.getSelectedIndex();
				if (selectedIndex == -1)
					return;
				String selected = gameServersList.get(selectedIndex).toString();
				JDialog d = new GameServerSettings(Byte.parseByte(selected));
				d.setResizable(false);
				d.pack();
				d.setVisible(true);
			}
		});
		removeServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = activeGameServers.getSelectedIndex();
				if (selectedIndex == -1)
					return;
				String selected = gameServersList.get(selectedIndex).toString();
				String prevChange = changes.get("argonms.gui.game.run");
				String newList = null;
				if (prevChange == null) {
					newList = createListWithExclusion(m.getConfig().getEnabledGameServers(), Byte.parseByte(selected));
				} else {
					int start = prevChange.indexOf(selected);
					int end = start + selected.length();
					if (end + 1 < prevChange.length())
						end++; //remove comma after element if not at the end
					else if (start - 1 >= 0)
						start--; //if at end, remove comma before element if not at the front too (i.e., a single element)
					newList = prevChange.substring(0, start) + prevChange.substring(end, prevChange.length());
				}
				if (newList != null) {
					gameServersList.remove(selectedIndex);
					changes.put("argonms.gui.game.run", newList);
					applyButton.setEnabled(true);
				}
			}
		});
		addServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = gameServerIdEntry.getText();
				if (isAllowedGameId(text)) {
					byte serverId = Byte.parseByte(text);
					String prevChange = changes.get("argonms.gui.game.run");
					String newList = null;
					if (prevChange == null) {
						if (!contains(m.getConfig().getEnabledGameServers(), serverId))
							newList = createListWithAddition(m.getConfig().getEnabledGameServers(), serverId);
					} else {
						boolean allowed = true;
						for (String id : prevChange.split(",")) {
							if (id.equals(text)) {
								allowed = false;
								break;
							}
						}
						if (allowed)
							newList = (prevChange.length() > 0 ? prevChange + ',' : "") + text;
					}
					if (newList != null) {
						try {
							String selected = m.getConfig().getGameServerPropertiesPath(serverId);
							if (selected == null)
								selected = m.getConfig().promptForPropsFile(LaunchConfigDialog.this, "Select your game" + text + ".properties file");
							if (selected != null) {
								gameServersList.addElement(text);
								changes.put("argonms.gui.game.run", newList);
								changes.put("argonms.gui.game." + text + ".properties", selected);
								applyButton.setEnabled(true);
							}
						} catch (IOException ex) {
							System.err.println("Error in selecting game" + text + ".properties");
							ex.printStackTrace();
							JOptionPane.showMessageDialog(
								LaunchConfigDialog.this,
								"Error while changing settings: " + ex,
								"Settings Changing Error",
								JOptionPane.ERROR_MESSAGE
							);
						}
					}
				}
			}
		});

		//layout and place on panel
		gameServersPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		gameServersPanel.add(new JLabel("Active Game Servers:"), c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		gameServersPanel.add(activeGameServersPane, c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		gameServersPanel.add(editServerButton, c);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LAST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		gameServersPanel.add(removeServerButton, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		gameServersPanel.add(gameServerIdEntry, c);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		gameServersPanel.add(addServerButton, c);

		return gameServersPanel;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructClasspathPanel() {
		final JTextField textbox = new JTextField();
		return constructSimplePathSubPanel("Classpath", "Classpath:", textbox, m.getConfig().getClasspath(), new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JDialog d = new ClasspathDialog();
				d.setResizable(false);
				d.pack();
				d.setVisible(true);
			}
		});
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructWzPathPanel() {
		final JTextField textbox = new JTextField();
		return constructSimplePathSubPanel("Wz Path", "Wz Folder:", textbox, m.getConfig().getWzPath(), new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					String selected = m.getConfig().promptForDirectory(LaunchConfigDialog.this, "Select your WZ data folder");
					if (selected != null) {
						textbox.setText(selected);
						changes.put("argonms.gui.wz.location", selected);
						applyButton.setEnabled(true);
					}
				} catch (IOException ex) {
					System.err.println("Error in selecting WZ folder");
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
						LaunchConfigDialog.this,
						"Error while changing settings: " + ex,
						"Settings Changing Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructScriptPathPanel() {
		final JTextField textbox = new JTextField();
		return constructSimplePathSubPanel("Script Path", "Scripts Folder:", textbox, m.getConfig().getScriptsPath(), new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					String selected = m.getConfig().promptForDirectory(LaunchConfigDialog.this, "Select your scripts folder");
					if (selected != null) {
						textbox.setText(selected);
						changes.put("argonms.gui.scripts.location", selected);
						applyButton.setEnabled(true);
					}
				} catch (IOException ex) {
					System.err.println("Error in selecting scripts folder");
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
						LaunchConfigDialog.this,
						"Error while changing settings: " + ex,
						"Settings Changing Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructDatabasePropertiesPanel() {
		final JTextField textbox = new JTextField();
		return constructSimplePathSubPanel("Database", "Database Properties:", textbox, m.getConfig().getDatabasePropertiesPath(), new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					String selected = m.getConfig().promptForPropsFile(LaunchConfigDialog.this, "Select your db.properties file");
					if (selected != null) {
						textbox.setText(selected);
						changes.put("argonms.gui.database.properties", selected);
						applyButton.setEnabled(true);
					}
				} catch (IOException ex) {
					System.err.println("Error in selecting db.properties");
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
						LaunchConfigDialog.this,
						"Error while changing settings: " + ex,
						"Settings Changing Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructLoggerPropertiesPanel() {
		final JTextField textbox = new JTextField();
		return constructSimplePathSubPanel("Logger", "Logging Properties:", textbox, m.getConfig().getLoggerPropertiesPath(), new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					String selected = m.getConfig().promptForPropsFile(LaunchConfigDialog.this, "Select your logging.properties file");
					if (selected != null) {
						textbox.setText(selected);
						changes.put("argonms.gui.logger.properties", selected);
						applyButton.setEnabled(true);
					}
				} catch (IOException ex) {
					System.err.println("Error in selecting logging.properties");
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
						LaunchConfigDialog.this,
						"Error while changing settings: " + ex,
						"Settings Changing Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructMacBanBlacklistPathPanel() {
		final JTextField textbox = new JTextField();
		return constructSimplePathSubPanel("Cheat Tracker", "MAC Ban Blacklist:", textbox, m.getConfig().getMacBanBlacklistPath(), new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					String selected = m.getConfig().promptForTextFile(LaunchConfigDialog.this, "Select your macbanblacklist.txt file");
					if (selected != null) {
						textbox.setText(selected);
						changes.put("argonms.gui.cheattracker.macbanblacklist", selected);
						applyButton.setEnabled(true);
					}
				} catch (IOException ex) {
					System.err.println("Error in selecting macbanblacklist.txt");
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
						LaunchConfigDialog.this,
						"Error while changing settings: " + ex,
						"Settings Changing Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private JPanel constructDialogButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		applyButton = new JButton("Apply");

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyChanges();
				dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyChanges();
			}
		});
		applyButton.setEnabled(false);

		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(applyButton);
		return buttonsPanel;
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private void applyChanges() {
		byte[] oldGameServerList = null;
		boolean shopWasEnabled = false;
		if (changes.containsKey("argonms.gui.game.run"))
			oldGameServerList = m.getConfig().getEnabledGameServers();
		if (changes.containsKey("argonms.gui.shop.run"))
			shopWasEnabled = m.getConfig().isShopEnabled();
		m.getConfig().updateProps(changes, this);
		m.getConfig().initialize(this);
		if (oldGameServerList != null) {
			byte[] newGameServerList = m.getConfig().getEnabledGameServers();
			//perform a set difference on enabled game servers
			//(new list - old list) to find tabs to add
			for (byte serverId : newGameServerList)
				if (!contains(oldGameServerList, serverId))
					m.addGameTab(serverId);
			//perform a set difference on enabled game servers
			//(old list - new list) to find tabs to remove
			for (byte serverId : oldGameServerList)
				if (!contains(newGameServerList, serverId))
					m.removeGameTab(serverId);
		}
		if (changes.containsKey("argonms.gui.shop.run")) {
			if (m.getConfig().isShopEnabled()) {
				if (!shopWasEnabled)
					m.addShopTab();
			} else {
				if (shopWasEnabled)
					m.removeShopTab();
			}
		}
		changes.clear();
		applyButton.setEnabled(false);
	}

	private class GameServerSettings extends JDialog {
		private String selected;

		/**
		 * This method is not thread-safe. It must be called from the Swing EDT.
		 */
		public GameServerSettings(final byte id) {
			super(LaunchConfigDialog.this, "Game" + id + " Settings", true);

			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (selected != null) {
						int option = JOptionPane.showConfirmDialog(GameServerSettings.this,
								"You have modified the path to your game" + id + " properties file.\n"
										+ "Would you like to save it before returning?",
								"Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
						switch (option) {
							case JOptionPane.YES_OPTION:
								changes.put("argonms.gui.game." + id + ".properties", selected);
								applyButton.setEnabled(true);
								dispose();
								break;
							case JOptionPane.NO_OPTION:
								dispose();
								break;
							case JOptionPane.CANCEL_OPTION:
								break;
						}
					} else {
						dispose();
					}
				}
			});
			setLayout(new FlowLayout());

			final JLabel gamePropsLbl = new JLabel("Game" + id + " Properties:");
			String text = changes.get("argonms.gui.game." + id + ".properties");
			if (text == null)
				text = m.getConfig().getGameServerPropertiesPath(id);
			final JTextField gamePropsUrl = new JTextField(text, 10);
			JButton okButton = new JButton("OK");
			JButton cancelButton = new JButton("Cancel");
			gamePropsUrl.setEditable(false);
			gamePropsUrl.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						selected = m.getConfig().promptForPropsFile(GameServerSettings.this, "Select your game" + id + ".properties file");
						if (selected != null)
							gamePropsUrl.setText(selected);
					} catch (IOException ex) {
						System.err.println("Error in selecting game" + id + ".properties");
						ex.printStackTrace();
						JOptionPane.showMessageDialog(
							LaunchConfigDialog.this,
							"Error while changing settings: " + ex,
							"Settings Changing Error",
							JOptionPane.ERROR_MESSAGE
						);
					}
				}
			});
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (selected != null) {
						changes.put("argonms.gui.game." + id + ".properties", selected);
						applyButton.setEnabled(true);
					}
					dispose();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			JPanel urlPanel = new JPanel();
			urlPanel.setLayout(new GridLayout(1, 2));
			urlPanel.add(gamePropsLbl);
			urlPanel.add(gamePropsUrl);
			add(urlPanel);
			add(okButton);
			add(cancelButton);
		}
	}

	private class ClasspathDialog extends JDialog {
		private static final int CHARS_PER_LINE = 25;

		/**
		 * This method is not thread-safe. It must be called from the Swing EDT.
		 */
		public ClasspathDialog() {
			super(LaunchConfigDialog.this, "Classpath Modifier", true);

			setLayout(new GridBagLayout());

			final DefaultListModel pathList = new DefaultListModel();
			final String[] originalPaths = m.getConfig().getClasspathElements();
			for (String bin : originalPaths)
				pathList.addElement(bin);
			final JList paths = new JList(pathList);
			paths.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			paths.setVisibleRowCount(5);
			JScrollPane pathsPane = new JScrollPane(paths);
			pathsPane.setPreferredSize(new Dimension(paths.getFontMetrics(paths.getFont()).charWidth('m') * CHARS_PER_LINE, pathsPane.getPreferredSize().height));
			JButton removeButton = new JButton("Remove");
			JButton addButton = new JButton("Add");

			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int selectedIndex = paths.getSelectedIndex();
					if (selectedIndex == -1)
						return;
					String selected = pathList.get(selectedIndex).toString();
					String newList = null;
					String prevChange = changes.get("argonms.gui.bin.classpath");
					if (prevChange == null) {
						newList = createListWithExclusion(originalPaths, selected);
					} else {
						int start = prevChange.indexOf(selected);
						int end = start + selected.length();
						if (end + 1 < prevChange.length())
							end++; //remove comma after element if not at the end
						else if (start - 1 >= 0)
							start--; //if at end, remove comma before element if not at the front too (i.e., a single element)
						newList = prevChange.substring(0, start) + prevChange.substring(end, prevChange.length());
					}
					if (newList != null) {
						pathList.remove(selectedIndex);
						changes.put("argonms.gui.bin.classpath", newList);
						applyButton.setEnabled(true);
					}
				}
			});
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						String[] selected = m.getConfig().promptForClasspath(ClasspathDialog.this, "Append to classpath");
						String newList = changes.get("argonms.gui.bin.classpath");
						if (newList == null)
							newList = createListWithAddition(originalPaths, null);
						if (selected != null) {
							for (String bin : selected) {
								newList += (newList.length() > 0 ? "," : "") + bin;
								pathList.addElement(bin);
							}
							changes.put("argonms.gui.bin.classpath", newList);
							applyButton.setEnabled(true);
						}
					} catch (IOException ex) {
						System.err.println("Error in selecting classpath");
						ex.printStackTrace();
						JOptionPane.showMessageDialog(
							LaunchConfigDialog.this,
							"Error while changing settings: " + ex,
							"Settings Changing Error",
							JOptionPane.ERROR_MESSAGE
						);
					}
				}
			});

			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(5, 5, 5, 5);
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			c.gridheight = 3;
			c.weightx = 0;
			add(pathsPane, c);
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.PAGE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 2;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 1;
			add(removeButton, c);
			c.anchor = GridBagConstraints.PAGE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 2;
			c.gridy = 2;
			c.gridwidth = 1;
			c.gridheight = 1;
			add(addButton, c);
		}
	}
}
