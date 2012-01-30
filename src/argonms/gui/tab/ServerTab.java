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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import argonms.gui.model.Environment;
import argonms.gui.model.Model;

/**
 * 
 * @author GoldenKevin
 */
@SuppressWarnings("serial")
public abstract class ServerTab extends StreamBasedConsoleTab {
	private class StartStopButton extends JButton {
		private boolean isOn;

		/**
		 * This method is not thread-safe. It must be called from the Swing EDT.
		 */
		public StartStopButton() {
			setPreferredSize(new Dimension(96, 32));
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (isOn)
						stopProcess();
					else
						startProcess();
				}
			});

			setOff();
		}

		/**
		 * This method is not thread-safe. It must be called from the Swing EDT.
		 */
		public final void setOff() {
			isOn = false;
			setText("Start");
		}

		/**
		 * This method is not thread-safe. It must be called from the Swing EDT.
		 */
		public final void setOn() {
			isOn = true;
			setText("Stop");
		}
	}

	protected Model state;

	private final AtomicBoolean alreadyCleanedUp;
	private JLabel status;
	private StartStopButton button;

	private Process proc;

	protected ServerTab(Model state) {
		alreadyCleanedUp = new AtomicBoolean(true);

		optionsPane.setLayout(new GridBagLayout());

		this.state = state;

		status = new JLabel();
		button = new StartStopButton();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		optionsPane.add(status, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		optionsPane.add(button, c);

		changeStatus("Ready to start");
	}

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	private void changeStatus(String message) {
		status.setText("Server Status: " + message);
	}

	protected abstract String getDescription();
	protected abstract String[] getCommand();

	/**
	 * This method is not thread-safe. It must be called from the Swing EDT.
	 */
	public void startProcess() {
		clearOutput();
		button.setOn();
		changeStatus("Running");

		try {
			ProcessBuilder pb = new ProcessBuilder(Environment.buildCommand(getCommand()));
			pb.redirectErrorStream(true);
			alreadyCleanedUp.set(false);
			proc = pb.start();
			registerReaderStream(proc.getOutputStream());
			registerWriterStream(proc.getInputStream());
		} catch (IOException e) {
			System.err.println("Error starting up " + getDescription() + " server");
			e.printStackTrace();
			JOptionPane.showMessageDialog(
				this,
				"Could not start up " + getDescription() + " server: " + e,
				getDescription() + " server startup failed",
				JOptionPane.ERROR_MESSAGE
			);
		}
		state.processStarted(this);
	}

	public void stopProcess() {
		proc.destroy();
	}

	@Override
	protected void streamClosed() {
		if (alreadyCleanedUp.compareAndSet(false, true)) {
			cleanup();
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				//propagate it further upwards
				Thread.currentThread().interrupt();
			}
			SwingUtilities.invokeLater(new Runnable () {
				@Override
				public void run() {
					button.setOff();
					changeStatus("Exited with status code " + proc.exitValue());
					state.processEnded(ServerTab.this);
				}
			});
		}
	}
}
