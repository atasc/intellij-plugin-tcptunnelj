package io.atasc.intellij.tcptunnelj.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Resolves well-known OS directories.
 *
 * @author atasc
 * @since
 */
public class SystemDirectories {
  private static final String USER_DIRS_RELATIVE_PATH = "user-dirs.dirs";
  private static final String DOWNLOAD_KEY = "XDG_DOWNLOAD_DIR";
  private static final String DEFAULT_DOWNLOADS_NAME = "Downloads";

  /**
   * The directory the OS is configured to download into, so that a save dialog opens where the user
   * expects it. Resolution order:
   * <ol>
   *   <li>the {@code XDG_DOWNLOAD_DIR} environment variable, when exported;</li>
   *   <li>{@code $XDG_CONFIG_HOME/user-dirs.dirs} (defaults to {@code ~/.config}), which is where
   *       the Linux desktops persist the — possibly localized, possibly relocated — choice;</li>
   *   <li>{@code ~/Downloads}, the default on Windows and macOS.</li>
   * </ol>
   * Falls back to the user home if none of them is an existing directory, so the returned file is
   * always usable as a chooser's current directory.
   *
   * <p>Note: on Windows a Downloads folder moved through Explorer lives in the registry only and is
   * not detected here.
   */
  public static File getDownloadsDirectory() {
    File dir = fromEnvironment();

    if (dir == null) {
      dir = fromXdgUserDirs();
    }

    if (dir == null) {
      dir = new File(getUserHome(), DEFAULT_DOWNLOADS_NAME);
    }

    return dir.isDirectory() ? dir : getUserHome();
  }

  public static File getUserHome() {
    return new File(System.getProperty("user.home", "."));
  }

  private static File fromEnvironment() {
    return toExistingDirectory(System.getenv(DOWNLOAD_KEY));
  }

  /**
   * Parses the {@code XDG_DOWNLOAD_DIR="$HOME/..."} line of the XDG user dirs config. The file is a
   * shell snippet, but the desktops only ever write literal assignments optionally prefixed with
   * {@code $HOME}, so a plain parse is enough and avoids spawning {@code xdg-user-dir}.
   */
  private static File fromXdgUserDirs() {
    Path configFile = getXdgConfigHome().resolve(USER_DIRS_RELATIVE_PATH);
    if (!Files.isReadable(configFile)) {
      return null;
    }

    try {
      List<String> lines = Files.readAllLines(configFile, StandardCharsets.UTF_8);

      for (String line : lines) {
        String trimmed = line.trim();
        if (trimmed.startsWith("#") || !trimmed.startsWith(DOWNLOAD_KEY)) {
          continue;
        }

        int separator = trimmed.indexOf('=');
        if (separator < 0) {
          continue;
        }

        String value = unquote(trimmed.substring(separator + 1).trim());
        if (value.isEmpty()) {
          continue;
        }

        return toExistingDirectory(expandHome(value));
      }
    } catch (Exception e) {
      // unreadable or malformed config: the caller just falls back to the next candidate
      System.err.println("Cannot read " + configFile + ": " + e.getMessage());
    }

    return null;
  }

  private static Path getXdgConfigHome() {
    String configHome = System.getenv("XDG_CONFIG_HOME");

    if (configHome != null && !configHome.isEmpty()) {
      return Paths.get(configHome);
    }

    return Paths.get(getUserHome().getAbsolutePath(), ".config");
  }

  private static String expandHome(String value) {
    String home = getUserHome().getAbsolutePath();

    if (value.startsWith("$HOME")) {
      return home + value.substring("$HOME".length());
    }

    if (value.startsWith("${HOME}")) {
      return home + value.substring("${HOME}".length());
    }

    if (value.startsWith("~")) {
      return home + value.substring(1);
    }

    return value;
  }

  private static String unquote(String value) {
    if (value.length() > 1
        && ((value.startsWith("\"") && value.endsWith("\""))
        || (value.startsWith("'") && value.endsWith("'")))) {
      return value.substring(1, value.length() - 1);
    }

    return value;
  }

  private static File toExistingDirectory(String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }

    File dir = new File(path);

    return dir.isDirectory() ? dir : null;
  }
}
