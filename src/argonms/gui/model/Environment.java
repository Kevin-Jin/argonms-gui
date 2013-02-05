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

import java.awt.Font;

/**
 *
 * @author GoldenKevin
 */
public class Environment {
	public enum OperatingSystem { WINDOWS, LINUX, MAC_OS_X, UNIX }

	public static final String DEFAULT_PROPS_FILE = "launcher.properties";
	public static final Font CONSOLE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

	public static final int MAX_HEAP_SIZE = 600; //in mb
	public static final String DIR_DELIMIT;
	public static final String LIST_DELIMIT;
	public static final String JAVA_DIR;
	private static final OperatingSystem os;
	private static final String[] commandTemplate;

	static {
		DIR_DELIMIT = System.getProperty("file.separator");
		LIST_DELIMIT = System.getProperty("path.separator");
		JAVA_DIR = System.getProperty("java.home") + DIR_DELIMIT + "bin" + DIR_DELIMIT + "java";
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows")) {
			os = OperatingSystem.WINDOWS;
		} else if (osName.equals("Linux")) {
			os = OperatingSystem.LINUX;
		} else if (osName.equals("Mac OS X")) {
			os = OperatingSystem.MAC_OS_X;
		} else if (osName.toUpperCase().contains("IX") || osName.equals("Solaris")
				|| osName.equals("HP UX") || osName.equals("FreeBSD")) {
			os = OperatingSystem.UNIX;
		} else {
			os = null;
		}
		switch (os) {
			case WINDOWS:
				// { "cmd.exe", "/C" } doesn't work because cmd is quite stupid in dealing with spaces
				//we will have to perform different behavior for Windows and Unix-like OSes...
				commandTemplate = new String[0];
				break;
			case LINUX:
			case UNIX:
				commandTemplate = new String[] { "/bin/bash", "-c", null };
				break;
			case MAC_OS_X:
				commandTemplate = new String[] { "/bin/tcsh", "-c", null };
				break;
			default:
				commandTemplate = null;
		}
	}

	public static OperatingSystem getOs() {
		return os;
	}

	public static String[] buildCommand(String[] args) {
		switch (os) {
			case WINDOWS:
				return args;
			default:
				StringBuilder progParams = new StringBuilder(args[0]);
				for (int i = 1; i < args.length; i++)
					progParams.append(' ').append(args[i]);
				commandTemplate[commandTemplate.length - 1] = progParams.toString();
				return commandTemplate;
		}
	}
}
