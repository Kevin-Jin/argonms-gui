/*
 * ArgonMS Server Manager - a process launcher and organizer utilizing Swing.
 * Copyright (C) 2011  GoldenKevin
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author GoldenKevin
 */
@SuppressWarnings("serial")
public abstract class StreamBasedConsoleTab extends ConsoleTab {
	private final List<Reader> openReaders;
	private final List<Writer> openWriters;

	protected StreamBasedConsoleTab() {
		openReaders = new ArrayList<Reader>();
		openWriters = new ArrayList<Writer>();
	}

	private void readInput(Reader reader) {
		int c;
		try {
			while ((c = reader.read()) != -1)
				writeToOutput(Character.toString((char) c));
		} catch (IOException e) {
			//most likely just that the stream is closed
			System.err.println("Error in reading from reader stream");
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				System.err.println("Error in closing reader stream");
				e.printStackTrace();
			}
			inputStreamClosed(reader);
		}
	}

	@Override
	protected void textEntered(String text) {
		for (Writer outputListener : openWriters) {
			try {
				outputListener.write(text);
				outputListener.flush();
			} catch (IOException ex) {
				//most likely just that the stream is closed
				System.err.println("Error in writing to writer stream");
				ex.printStackTrace();
				try {
					outputListener.close();
					streamsClosed();
				} catch (IOException e) {
					System.err.println("Error in closing writer stream");
					e.printStackTrace();
				}
			}
		}
	}

	private void inputStreamOpened(Reader reader) {
		boolean firstStreamOpened;
		synchronized (openReaders) {
			openReaders.add(reader);
			firstStreamOpened = (openReaders.size() == 1);
		}
		if (firstStreamOpened)
			outputEnabled();
	}

	private void inputStreamClosed(Reader reader) {
		boolean lastStreamClosed;
		synchronized (openReaders) {
			openReaders.remove(reader);
			lastStreamClosed = (openReaders.size() == 0);
		}
		if (lastStreamClosed) {
			outputDisabled();
			streamsClosed();
		}
	}

	protected void registerReaderStream(String name, OutputStream stream) {
		synchronized (openWriters) {
			openWriters.add(new BufferedWriter(new OutputStreamWriter(stream)));
		}
	}

	protected void registerWriterStream(String name, final InputStream stream) {
		final Reader reader = new BufferedReader(new InputStreamReader(stream));
		inputStreamOpened(reader);
		new Thread(new Runnable() {
			@Override
			public void run() {
				readInput(reader);
			}
		}, name + "-reader").start();
	}

	protected void cleanup() {
		try {
			synchronized (openReaders) {
				for (Reader openReader : openReaders)
					openReader.close();
			}
			synchronized (openWriters) {
				for (Writer openWriter : openWriters)
					openWriter.close();
			}
		} catch (IOException e) {
			System.err.println("Error in cleanup");
			e.printStackTrace();
		}
	}

	protected abstract void streamsClosed();
}
