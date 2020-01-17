package org.graalvm.vm.trcview.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.Analysis;
import org.graalvm.vm.trcview.arch.io.TraceFileReader;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.ProgressListener;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class Server {
    private static final Logger log = Trace.create(Server.class);

    private ServerSocket socket;
    private ServerData data;

    public Server(int port, ServerData data) throws IOException {
        this.data = data;
        socket = new ServerSocket(port);
    }

    public void run() throws IOException {
        while (true) {
            Socket sock = socket.accept();
            System.out.println("Connection accepted: " + sock.getRemoteSocketAddress());
            ServerConnection con = new ServerConnection(sock, data);
            con.start();
        }
    }

    public static void main(String[] args) throws IOException {
        Trace.setupConsoleApplication();

        int port = Integer.parseInt(args[0]);
        String filename = args[1];

        ServerData data = null;
        File file = new File(filename);
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            long size = file.length();
            System.out.print("Loading " + file + " [" + size + " bytes] ");
            System.out.flush();
            TraceReader reader = new TraceFileReader(in);
            Analysis analysis = new Analysis(reader.getArchitecture());
            analysis.start();
            BlockNode root = BlockNode.read(reader, analysis, new ProgressInfo());
            analysis.finish(root);
            if (root == null || root.getFirstStep() == null) {
                System.out.println(" FAIL");
                log.log(Levels.ERROR, "Loading failed");
                return;
            } else {
                System.out.println(" OK");
                log.info("Loading succeeded");
            }
            data = new ServerData(reader.getArchitecture(), root, analysis);
        } catch (Throwable t) {
            log.log(Levels.ERROR, "Loading failed: " + t, t);
            throw t;
        }

        System.out.println("Starting server at port " + port + "...");
        Server server = new Server(port, data);
        System.out.println("Waiting for connections");
        server.run();
    }

    private static class ProgressInfo implements ProgressListener {
        private long last = 0;

        public void progressUpdate(long value) {
            long next = last + 16 * 1024 * 1024; // 16MB
            if (value >= next) {
                System.out.print(".");
                System.out.flush();
                last = value;
            }
        }
    }
}
