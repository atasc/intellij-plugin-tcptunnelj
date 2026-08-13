# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TcpTunnelJ is an IntelliJ Platform plugin that acts as a man-in-the-middle TCP tunnel: it listens on a local
source port, forwards every connection to a destination host:port, and shows both directions of the captured
traffic in a bottom tool window. It is a modernized fork of the original
[TunnelliJ](https://github.com/milanboruvka/tunnellij) plugin.

The code is Java (Kotlin is configured but only used by the test), written against the IntelliJ Platform SDK.

## Commands

    ./gradlew runIde                 # launch a sandbox IDE with the plugin loaded — the main dev loop
    ./gradlew buildPlugin            # build/distributions/intellij-plugin-tcptunnelj-<version>.zip
    ./gradlew compileJava            # fastest check that a change compiles
    ./gradlew test                   # BasePlatformTestCase tests (headless IDE, slow to start)
    ./gradlew test --tests "*TcpTunnelJPluginTest.testRename"   # single test
    ./gradlew verifyPlugin           # Plugin Verifier, binary compatibility against recommended IDEs
    ./gradlew publishPlugin          # Marketplace; needs PUBLISH_TOKEN, runs patchChangelog first

`doc/deploy.md` covers building and installing into the real IDE (as opposed to the sandbox).

The `.run/` directory ships equivalent IDE run configurations (Run Plugin, Run Tests, Run Verifications).

## Architecture

### Data flow

`Tunnel` (`net/Tunnel.java`) owns a `ServerSocket` on the source port. Its `start()` blocks in an accept loop, so
callers must run it off the EDT — `TunnelPanel.start()` does this on a single-thread `ExecutorService`. For each
accepted connection it opens a socket to the destination and hands both to a `ClientHandler` thread.

`ClientHandler` creates one `Call` and two `TunnelWriter` threads — client→destination and destination→client —
then joins both and stamps the end time. Each `TunnelWriter` pumps bytes in one direction with an 8 KB buffer,
writing simultaneously to the peer socket and to the `Call`'s in-memory logger, and firing a `DataListener` per
chunk.

A `Call` is one TCP connection, not one HTTP request: with keep-alive a single `Call` accumulates several
requests in the same buffer. Its two `ByteArrayOutputStream`s hold the whole conversation for the life of the
window, so long-running captures grow unbounded in memory. Beware the naming inversion: `getOutputLogger()` /
`getOutput()` is the **request** side (what the client sent), `getInputLogger()` / `getInput()` is the
**response** side.

### Events

`Tunnel` keeps a `List<TunnelListener>` and fires `tunnelStarted` / `tunnelStopped` / `newCall` / `endCall` /
`onDataReceived`. Both `CallsPanel` and `ControlPanel` register as listeners, and all these callbacks arrive on
tunnel threads, never on the EDT — that is why the UI implementations wrap their work in
`ApplicationManager.getApplication().invokeLater(...)`. Keep that discipline when adding listeners.

`ControlPanel` is also where the running flag lives in practice: its `tunnelStarted` / `tunnelStopped` callbacks
call back into `TunnelPanel.setRunning(...)` and toggle the editability of the address fields. Toolbar actions
read `TunnelPanel.isRunning()` in their `update()` to decide enablement.

### UI composition

`TcpTunnelPlugin.getContent()` is the assembly point: it builds the `TunnelPanel`, builds the toolbar action
group, and adds the toolbar to the WEST edge. `TunnelPanel` = `CallsPanel` (CENTER) + `ControlPanel` (SOUTH,
source port / destination host / destination port). `CallsPanel` is a vertical splitter with the `JBList` of
calls on top and `ViewersPanel` below, itself a horizontal splitter showing request text on the left and
response text on the right.

`TunnelPanel` is a thin delegator to `CallsPanel`; adding a list capability means adding a method in both.

### Actions

Toolbar actions live in `action/` and are instantiated in `TcpTunnelPlugin.initToolbarActionGroup()` — they are
**not** declared in `plugin.xml`, so a new action must be added to that method to appear. They extend
`BaseAction` / `BaseToggleAction`, which hold a `TcpTunnelPlugin` reference and pin
`getActionUpdateThread()` to EDT; each action reaches the UI via `tunnelPlugin.getTunnelPanel()` and gates
itself in `update()`. Icons are centralized in `ui/Icons.java` as `AllIcons` constants.

The calls list is in `MULTIPLE_INTERVAL_SELECTION` mode: anything acting on "the selection" must handle several
rows (see `CallsPanel.clearSelected()`, which removes indices back-to-front).

### Configuration

`TcpTunnelConfig` reads and writes a single flat `~/.tcptunnelj.properties` shared by every project. Keys are
namespaced with the normalized project name (lowercased, spaces to underscores), e.g.
`myproject.tcptunnelj.src.port`, which is how several open projects keep separate tunnel settings. It is loaded
eagerly in the constructor and only persisted when `store()` is called.

### Plugin lifecycle

`plugin.xml` registers only the tool window, the notification group, and two application listeners. One
`TcpTunnelPlugin` is created **per project** inside `TcpTunnelWindowFactory.createToolWindowContent()` and must
not be cached statically — sharing it across projects is the multi-instance bug fixed in 0.5.6. It closes the
tunnel on project close and on dispose.

`StartOnBootAction` is what actually implements auto-start: when the persisted flag is set, its constructor
schedules `TunnelPanel.start()` 1.5 s later.

## Gotchas

- **`plugin.xml` `<version>`, `<description>` and `<change-notes>` are dead text.** `patchPluginXml` overwrites
  them at build time from `gradle.properties`, the `<!-- Plugin description -->` block of `README.md`, and the
  `[Unreleased]` (or matching version) section of `CHANGELOG.md`. Edit those sources, not the manifest; the
  hardcoded values in `src/main/resources/META-INF/plugin.xml` have already drifted.
- Removing the description markers from `README.md` fails the build by design.
- `pluginUntilBuild` is commented out in `gradle.properties`, so builds carry no upper compatibility bound.
- `TunnelOld`, `TcpTunnelToolFactory`, `TcpTunnelWindow.getContent()` and `TcpTunnelProjectService` are leftovers
  from the plugin template / the pre-fork implementation and are not on any live path. `TcpTunnelWindowFactory`
  selects the real implementation through a hardcoded `windowToLoad = 3` switch. The test suite still exercises
  the template's `TcpTunnelProjectService`, so deleting it breaks `./gradlew test`.
- Error handling in `net/` largely prints to stdout/stderr rather than using `Logger`; `util/Tracer` exists for
  ad-hoc tracing.
