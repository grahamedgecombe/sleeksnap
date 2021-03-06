/**
 * Sleeksnap, the open source cross-platform screenshot uploader
 * Copyright (C) 2012 Nikki <nikki@nikkii.us>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sleeksnap.util;

import java.awt.Desktop;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.sleeksnap.Constants;
import org.sleeksnap.Constants.Application;
import org.sleeksnap.Constants.Version;

/**
 * A basic utility class
 * 
 * @author Nikki
 * 
 */
public class Util {
	/**
	 * An enum containing operating system types
	 * 
	 * @author Nikki
	 * 
	 */
	public static enum OperatingSystem {
		LINUX, SOLARIS, WINDOWS, MAC, UNKNOWN
	}

	/**
	 * The cached operating system
	 */
	public static final OperatingSystem SYSTEM = getPlatform();

	/**
	 * The saved working directory
	 */
	private static File workDir;

	/**
	 * A method to get the unix time...
	 * @return
	 * 		The current time in seconds
	 */
	public static long currentTimeSeconds() {
		return (System.currentTimeMillis() / 1000);
	}

	/**
	 * Get the last part of a class name
	 * 
	 * @param key
	 *            The full name
	 * @return The class name formatted
	 */
	public static String formatClassName(Class<?> key) {
		return key.getName().substring(key.getName().lastIndexOf('.') + 1);
	}

	/**
	 * Get the computer's user agent
	 * 
	 */
	public static String getHttpUserAgent() {
		return Application.NAME + " v" + Version.getVersionString();
	}

	/**
	 * Get the current platform
	 * 
	 * @return The current platform
	 */
	public static OperatingSystem getPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win"))
			return OperatingSystem.WINDOWS;
		if (osName.contains("mac"))
			return OperatingSystem.MAC;
		if (osName.contains("solaris"))
			return OperatingSystem.SOLARIS;
		if (osName.contains("sunos"))
			return OperatingSystem.SOLARIS;
		if (osName.contains("linux"))
			return OperatingSystem.LINUX;
		if (osName.contains("unix"))
			return OperatingSystem.LINUX;
		return OperatingSystem.UNKNOWN;
	}

	/**
	 * Get a resource, allows us to run it from source or jar
	 * 
	 * @param name
	 *            The resource name
	 * @return The URL of the resource, either in-jar or on the filesystem
	 */
	public static URL getResourceByName(String name) {
		if (Utils.class.getResource(name) != null) {
			return Utils.class.getResource(name);
		} else {
			File file = null;
			if ((file = new File("resources" + name)).exists() || (file = new File(Util.getWorkingDirectory(), "resources" + name)).exists()) {
				try {
					return file.toURI().toURL();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Get the system architecture
	 * 
	 * @return The system architecture integer
	 */
	public static int getSystemArch() {
		String archs = System.getProperty("os.arch");
		return Integer.parseInt(archs.substring(archs.length() - 2));
	}

	/**
	 * Get the current working directory
	 * 
	 * @return The working directory for the application
	 */
	public static File getWorkingDirectory() {
		if (workDir == null)
			workDir = getWorkingDirectory(Constants.Application.NAME
					.toLowerCase());
		return workDir;
	}

	/**
	 * Get the AppData directory
	 * 
	 * @param applicationName
	 *            The application name
	 * @return The working directory
	 */
	public static File getWorkingDirectory(String applicationName) {
		String userHome = System.getProperty("user.home", ".");
		File workingDirectory;
		switch (getPlatform()) {
		case LINUX:
		case SOLARIS:
			File config = new File(userHome, ".config");
			if(config.exists()) {
				workingDirectory = new File(config, applicationName + '/');
				workingDirectory.mkdirs();
			} else {
				workingDirectory = new File(userHome, '.' + applicationName + '/');
			}
			break;
		case WINDOWS:
			String applicationData = System.getenv("APPDATA");
			if (applicationData != null)
				workingDirectory = new File(applicationData, "."
						+ applicationName + '/');
			else
				workingDirectory = new File(userHome,
						'.' + applicationName + '/');
			break;
		case MAC:
			workingDirectory = new File(userHome,
					"Library/Application Support/" + applicationName);
			break;
		default:
			workingDirectory = new File(userHome, applicationName + '/');
			break;
		}
		if ((!workingDirectory.exists()) && (!workingDirectory.mkdirs()))
			throw new RuntimeException(
					"The working directory could not be created: "
							+ workingDirectory);
		return workingDirectory;
	}

	/**
	 * Parse commandline arguments
	 * 
	 * @param args
	 *            The arg array from the main method, or manual args
	 * @return The map containing the args
	 */
	public static HashMap<String, Object> parseArguments(String[] args) {
		HashMap<String, Object> arguments = new HashMap<String, Object>();
		for (String s : args) {
			if (s.startsWith("--")) {
				s = s.substring(2);
			} else if (s.startsWith("-")) {
				s = s.substring(1);
			}
			int eqIdx = s.indexOf('=');
			String key = s.substring(0, eqIdx != -1 ? eqIdx : s.length());
			Object value = true;
			if (eqIdx != -1) {
				value = s.substring(s.indexOf('=') + 1);
			}
			arguments.put(key, value);
		}
		return arguments;
	}

	/**
	 * Set the working directory, used when Sleeksnap is self-contained (Uses a
	 * directory which you can have on a flash drive)
	 * 
	 * @param workingDirectory
	 *            The directory
	 */
	public static void setWorkingDirectory(File workingDirectory) {
		workDir = workingDirectory;
	}

	/**
	 * Trim the string's start/finish of the specified character
	 * 
	 * @param str
	 *            The string
	 * @param ch
	 *            The character
	 * @return The trimmed string
	 */
	public static String trim(String str, final char ch) {
		if ((str == null) || str.isEmpty())
			return str;
		else if (str.length() == 1)
			return str.charAt(0) == ch ? "" : str;
		try {
			if (str.charAt(0) == ch)
				str = str.substring(1);
			final int l = str.length() - 1;
			if (str.charAt(l) == ch)
				str = str.substring(0, l);
			return str;
		} catch (final Exception e) {
			return str;
		}
	}

	/**
	 * Make each word in a string uppercase
	 * 
	 * @param string
	 *            The string to parse
	 * @return The formatted string
	 */
	public static String ucwords(String string) {
		StringBuilder out = new StringBuilder();
		String[] split = string.split(" ");
		for (int i = 0; i < split.length; i++) {
			String str = split[i];
			out.append(Character.toUpperCase(str.charAt(0)));
			if (str.length() > 1) {
				out.append(str.substring(1).toLowerCase());
			}
			if (i < (split.length - 1)) {
				out.append(" ");
			}
		}
		return out.toString();
	}

	/**
	 * Center a frame on the main display
	 * @param frame
	 * 			The frame to center
	 */
	public static void centerFrameOnMainDisplay(JFrame frame) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();
		if (screens.length < 1) {
			return; // Silently fail.
		}
		Rectangle screenBounds = screens[0].getDefaultConfiguration().getBounds();
	    int x = (int) ((screenBounds.getWidth() - frame.getWidth()) / 2);
	    int y = (int) ((screenBounds.getHeight() - frame.getHeight()) / 2);
	    frame.setLocation(x, y);
	}
	
	/**
	 * A list of popular browsers
	 */
	private static final String[] BROWSERS = new String[] {
		"xdg-open", "google-chrome", "chromium", "firefox", "opera", "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla"
	};
	
	/**
	 * A list of popular file managers
	 */
	private static final String[] FILE_MANAGERS = new String[] {
		"xdg-open", "nautilus", "dolphin", "thunar", "pcmanfm", "konqueror"
	};
	
	/**
	 * Finds the first supported program in the list (for UNIX-like platforms
	 * only).
	 * @param kind The kind of program, used in the exception message if no
	 * suitable program could be found.
	 * @param names The array of program names to try.
	 * @return The first supported program from the array of names.
	 * @throws Exception if no supported program could be found.
	 */
	private static String findSupportedProgram(String kind, String[] names) throws Exception {
		for (String name : names) {
			Process process = Runtime.getRuntime().exec(new String[] { "which", name });
			if (process.waitFor() == 0)
				return name;
		}

		throw new Exception("Unable to find supported " + kind);
	}
	
	/**
	 * Open a URL using java.awt.Desktop or a couple different manual methods
	 * @param url
	 * 			The URL to open
	 * @throws Exception
	 * 			If an error occurs attempting to open the url
	 */
	public static void openURL(URL url) throws Exception {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			desktop.browse(url.toURI());
		} else {
			OperatingSystem system = Util.getPlatform();
			switch (system) {
			case MAC:
				Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
		                  "openURL", new Class[] { String.class }).invoke(null,
		                  new Object[] { url.toString() });
				break;
			case WINDOWS:
				Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url.toString() });
				break;
			default:
				String browser = findSupportedProgram("browser", BROWSERS);
				Runtime.getRuntime().exec(new String[] { browser, url.toString() });
				break;
			}
		}
	}
	
	/**
	 * Open a file using {@link Desktop} if supported, or a manual
	 * platform-specific method if not.
	 * @param file The file to open.
	 * @throws Exception if the file could not be opened.
	 */
	public static void openFile(File file) throws Exception {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
			desktop.open(file);
		} else {
			OperatingSystem system = Util.getPlatform();
			switch (system) {
			case MAC:
			case WINDOWS:
				openURL(file.toURI().toURL());
				break;
			default:
				String fileManager = findSupportedProgram("file manager", FILE_MANAGERS);
				Runtime.getRuntime().exec(new String[] { fileManager, file.getAbsolutePath() });
				break;
			}
		}
	}
	
	/**
	 * A utility method used for parsing uploader data (like what is used in the Spinner type)
	 * @param defaults
	 * 			The string to parse
	 * @return
	 * 			The data map
	 */
	public static Map<String, String> parseDataList(String[] defaults) {
		Map<String, String> ret = new HashMap<String, String>();
		for(String s : defaults) {
			int idx = s.indexOf('=');
			if(idx != -1) {
				String key = s.substring(0, idx);
				String value = s.substring(idx + 1);
				
				ret.put(key, value);
			} else {
				ret.put(s, null);
			}
		}
		return ret;
	}
	
	/**
	 * A utility method for parsing response data (Useful for k: v etc)
	 * @param input
	 * 			The input string
	 * @param kvSeparator
	 * 			The value separating key and value
	 * @param entrySeparator
	 * 			The value separating entries
	 * @return
	 * 			The parsed map
	 */
	public static Map<String, String> parseKeyValues(String input, String kvSeparator, String entrySeparator) {
		Map<String, String> ret = new HashMap<String, String>();
		String[] split = input.split(entrySeparator);
		for(String s : split) {
			int idx = s.indexOf(kvSeparator);
			if(idx != -1) {
				String key = s.substring(0, idx);
				String value = s.substring(idx + 1);
				
				ret.put(key, value);
			} else {
				ret.put(s, null);
			}
		}
		return ret;
	}

	/**
	 * Implode a list of values (Like php's implode)
	 * @param list
	 * 			The list to implode
	 * @param glue
	 * 			The glue to use between values
	 * @return
	 * 			The imploded string
	 */
	public static String implodeList(List<?> list, String glue) {
		StringBuilder builder = new StringBuilder();
		for(Iterator<?> it = list.iterator(); it.hasNext();) {
			builder.append(it.next());
			if(it.hasNext()) {
				builder.append(glue);
			}
		}
		return builder.toString();
	}

	/**
	 * Gets the class name of the Swing look and feel which emulates the native
	 * style of the operating system.
	 * @return The system look and feel class name.
	 */
	public static String getSystemLookAndFeelClassName() {
		OperatingSystem platform = getPlatform();
		String clazz = UIManager.getSystemLookAndFeelClassName();
		
		/*
		 * On Linux, Java only attempts to use the GTK+ style if the desktop
		 * environment is GNOME. Otherwise, it falls back to the cross-platform
		 * style.
		 * 
		 * However, many other desktop environments use GTK+ (e.g. XFCE, LXDE,
		 * Unity, etc.) and even ones that don't (e.g. KDE) usually have some
		 * GTK+ compatibility so it still looks better than the cross-platform
		 * theme.
		 * 
		 * Therefore on Linux we override Java's choice and attempt to use GTK+
		 * if the cross-platform style was chosen.
		 */
		if (platform == OperatingSystem.LINUX && clazz.equals(UIManager.getCrossPlatformLookAndFeelClassName())) {
			clazz = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
		}
		
		return clazz;
	}
}
