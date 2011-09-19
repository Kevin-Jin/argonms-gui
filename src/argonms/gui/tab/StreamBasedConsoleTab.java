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
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 * @author GoldenKevin
 */
@SuppressWarnings("serial")
public abstract class StreamBasedConsoleTab extends ConsoleTab {
	private final AtomicReference<Reader> openReader;
	private final AtomicReference<Writer> openWriter;

	protected StreamBasedConsoleTab() {
		openReader = new AtomicReference<Reader>(null);
		openWriter = new AtomicReference<Writer>(null);
	}

	private void readInput() {
		Reader inputConsumer = openReader.get();
		int c;
		try {
			while ((c = inputConsumer.read()) != -1)
				writeToOutput(Character.toString((char) c));
		} catch (IOException e) {
			//most likely just that the stream is closed
			System.err.println("Error in reading from reader stream");
			e.printStackTrace();
		} finally {
			//don't use the local variable - we need to fetch from the instance
			//variable to see if it is already null - if it is, we do not need
			//to close it since it's null if and only if the reader is already
			//closed or if the reader is in the process of being closed
			closeReader();
		}
	}

	@Override
	protected void textEntered(String text) {
		Writer outputListener = openWriter.get();
		try {
			outputListener.write(text);
			outputListener.flush();
		} catch (IOException ex) {
			//most likely just that the stream is closed
			System.err.println("Error in writing to writer stream");
			ex.printStackTrace();

			//don't use the local variable - we need to fetch from the instance
			//variable to see if it is already null - if it is, we do not need
			//to close it since it's null if and only if the writer is already
			//closed or if the writer is in the process of being closed
			closeWriter();
		}
	}

	private void closeReader() {
		Reader r = openReader.getAndSet(null);
		if (r != null) {
			try {
				r.close();
			} catch (IOException e) {
				System.err.println("Error in closing reader stream");
				e.printStackTrace();
			}
			outputDisabled();
			streamClosed();
		}
	}

	private void closeWriter() {
		Writer w = openWriter.getAndSet(null);
		if (w != null) {
			try {
				w.close();
			} catch (IOException e) {
				System.err.println("Error in closing writer stream");
				e.printStackTrace();
			}
			inputDisabled();
			streamClosed();
		}
	}

	protected void registerReaderStream(OutputStream stream) {
		openWriter.set(new BufferedWriter(new OutputStreamWriter(stream)));
		inputEnabled();
	}

	protected void registerWriterStream(final InputStream stream) {
		openReader.set(new BufferedReader(new InputStreamReader(stream)));
		outputEnabled();

		new Thread(new Runnable() {
			@Override
			public void run() {
				readInput();
			}
		}, "stream-reader").start();
	}

	protected void cleanup() {
		closeReader();
		closeWriter();
	}

	protected abstract void streamClosed();
}
