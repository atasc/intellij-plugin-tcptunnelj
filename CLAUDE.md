# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TcpTunnelJ is an IntelliJ Platform plugin that functions as a TCP tunnel for network development. It allows developers to monitor and intercept TCP connections, displaying captured data directly within the IDE. This is an enhanced version of the original TunnelliJ plugin with additional features.

## Build System & Commands

This project uses Gradle with the IntelliJ Platform Gradle Plugin.

**Building and Running:**
- `./gradlew buildPlugin` - Builds the plugin ZIP for distribution
- `./gradlew runIde` - Launches IntelliJ IDE with the plugin loaded for testing
- `./gradlew test` - Runs tests

**Plugin Development:**
- `./gradlew patchPluginXml` - Patches plugin.xml with values from gradle.properties
- `./gradlew prepareSandbox` - Prepares sandbox environment with plugin and dependencies
- `./gradlew verifyPlugin` - Runs Plugin Verifier to check binary compatibility
- `./gradlew verifyPluginStructure` - Validates plugin.xml and archive structure
- `./gradlew buildSearchableOptions` - Builds searchable options index for UI components

**Publishing:**
- `./gradlew signPlugin` - Signs the ZIP with Marketplace ZIP Signer
- `./gradlew publishPlugin` - Publishes plugin to JetBrains Marketplace (requires PUBLISH_TOKEN env var)

## Architecture

### Core Networking Components (io.atasc.intellij.tcptunnelj.net)

**Tunnel**: The main TCP tunnel implementation that accepts connections on a source port and forwards them to a destination host:port. Uses ServerSocket to listen for incoming connections and spawns ClientHandler threads for each connection. Implements observer pattern via TunnelListener for notifying UI components of tunnel events.

**ClientHandler**: Thread that manages bidirectional data flow between client and destination sockets. Creates two TunnelWriter instances (client→destination and destination→client) and coordinates their lifecycle.

**Call**: Represents a single TCP connection session, capturing source/destination addresses, ports, timestamps, and all data transferred in both directions using ByteArrayOutputStream loggers.

**TunnelWriter**: Handles data transfer between input/output streams in a separate thread, logging all transferred data to Call's output loggers and notifying DataListeners.

### UI Components (io.atasc.intellij.tcptunnelj.ui)

**TunnelPanel**: Main UI panel containing CallsPanel (displays list of captured calls) and ControlPanel (start/stop/configuration controls). Manages tunnel lifecycle using ExecutorService.

**CallsPanel**: Displays list of Call objects and implements TunnelListener to receive tunnel events. Shows captured network traffic.

**ControlPanel**: Provides UI controls for configuring source port, destination host, destination port, and start/stop buttons. Also implements TunnelListener to update UI state.

### Plugin Integration (io.atasc.intellij.tcptunnelj.toolWindow)

**TcpTunnelWindowFactory**: Factory that creates the tool window for the plugin (registered in plugin.xml as bottom-anchored tool window).

**TcpTunnelWindow**: Tool window content provider with notification support for displaying temporary notifications to users.

### Configuration (io.atasc.intellij.tcptunnelj)

**TcpTunnelConfig**: Manages per-project configuration stored in `~/.tcptunnelj.properties`. Stores source port, destination host/port, and "start on boot" setting using project-specific property keys (project name is normalized and used as prefix).

### Plugin Lifecycle (io.atasc.intellij.tcptunnelj.listeners)

- **TcpTunnelApplicationActivationListener**: Responds to application activation events
- **TcpTunnelProjectManagerListener**: Handles project open/close events

## Key Technical Details

- **Target Platform**: IntelliJ Community Edition (IC) 2023.3.8+
- **JVM Version**: Java 17
- **Language**: Java with Kotlin support
- **Threading**: Uses ExecutorService for tunnel lifecycle management; each connection spawns ClientHandler threads
- **Configuration Storage**: Properties file at `~/.tcptunnelj.properties` with project-specific namespaced keys
- **Buffer Size**: 4KB (defined in TcpTunnelConfig.BUFFER_LENGTH)

## Plugin.xml Configuration

The plugin registers:
- Tool window with ID "TcpTunnelJ" (bottom-anchored, icon at `/icons/toolWindow.svg`)
- Notification group "TcpTunnelJ Notifications" (balloon display type)
- Application listeners for activation and project lifecycle events
- Resource bundle at `messages.TcpTunnelPluginBundle`

## Version Information

Plugin version and IDE compatibility ranges are defined in gradle.properties:
- `pluginVersion`: Current plugin version (SemVer)
- `pluginSinceBuild`: Minimum supported IDE build (233 = 2023.3)
- `platformVersion`: Target IDE version for development (2023.3.8)