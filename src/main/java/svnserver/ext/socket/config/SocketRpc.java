/**
 * This file is part of git-as-svn. It is subject to the license terms
 * in the LICENSE file found in the top-level directory of this distribution
 * and at http://www.gnu.org/licenses/gpl-2.0.html. No part of git-as-svn,
 * including this file, may be copied, modified, propagated, or distributed
 * except according to the terms contained in the LICENSE file.
 */
package svnserver.ext.socket.config;

import com.etsy.net.JUDS;
import com.etsy.net.UnixDomainSocket;
import com.etsy.net.UnixDomainSocketClient;
import com.etsy.net.UnixDomainSocketServer;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import svnserver.context.Shared;
import svnserver.context.SharedContext;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unix socket transport for API.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class SocketRpc implements Shared {
  @NotNull
  private static final Logger log = LoggerFactory.getLogger(SocketRpc.class);
  private static final long TIMEOUT = 10000;
  @NotNull
  private final SharedContext context;
  @NotNull
  private final File socketFile;
  @NotNull
  private final UnixDomainSocketServer socket;
  @NotNull
  private final AtomicBoolean stopped = new AtomicBoolean(false);
  @NotNull
  private final Thread thread;
  @NotNull
  private final ConcurrentHashSet<UnixDomainSocket> connections = new ConcurrentHashSet<>();
  @NotNull
  private final ExecutorService poolExecutor;

  public SocketRpc(@NotNull SharedContext context, @NotNull File socketFile) throws IOException {
    this.context = context;
    this.socketFile = socketFile;
    this.socket = new UnixDomainSocketServer(socketFile.getAbsolutePath(), JUDS.SOCK_STREAM);
    this.poolExecutor = Executors.newCachedThreadPool();
    thread = new Thread(SocketRpc.this::run, "unix-socket-listener");
    thread.run();
  }

  private void run() {
    log.info("Server API on unix socket: {}", socketFile);
    while (!stopped.get()) {
      final UnixDomainSocket client;
      try {
        client = socket.accept();
      } catch (IOException e) {
        if (stopped.get()) {
          log.info("Server Stopped");
          break;
        }
        log.error("Error accepting client connection", e);
        continue;
      }
      poolExecutor.execute(() -> {
        log.info("New connection");
        try {
          connections.add(client);
          serveClient(client);
        } catch (EOFException | SocketException ignore) {
          // client disconnect is not a error
        } catch (IOException e) {
          log.info("Client error:", e);
        } finally {
          connections.remove(client);
          client.close();
          log.info("Connection closed");
        }
      });
    }
  }

  private void serveClient(UnixDomainSocket client) throws IOException {

  }

  @Override
  public void close() throws Exception {
    poolExecutor.shutdown();
    stopped.set(true);
    socket.close();
    poolExecutor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
    thread.join();
  }
}
