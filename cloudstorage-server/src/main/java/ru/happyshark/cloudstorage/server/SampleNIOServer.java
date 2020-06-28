package ru.happyshark.cloudstorage.server;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class SampleNIOServer {

    private static class Server implements Runnable {

        private ServerSocketChannel serverChannel;
        private Selector selector;
        private Path path;

        public Server() throws IOException {
            path = Paths.get("./server-files/file_to_transfer.txt");
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(8189));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        }

        @Override
        public void run() {
            try {
                System.out.println("Server started");
                Iterator<SelectionKey> iterator;
                SelectionKey key;

                while(serverChannel.isOpen()) {
                    selector.select();
                    iterator = selector.selectedKeys().iterator();

                    while (iterator.hasNext()) {
                        key = iterator.next();
                        iterator.remove();

                        if (key.isAcceptable()) {
                            SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
                            System.out.println("Client connected");
                            sc.configureBlocking(false);
                            sc.register(selector, SelectionKey.OP_WRITE);
                        }

                        if (key.isWritable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            sendFile(sc, new RandomAccessFile(path.toFile(), "r"));
                            sc.close();
                        }
                    }
                }
                serverChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendFile(SocketChannel socketChannel, RandomAccessFile randomAccessFile) throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            FileChannel fileChannel = randomAccessFile.getChannel();
            while (fileChannel.read(buffer) > 0) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
            randomAccessFile.close();
        }
    }

    public static void main(String[] args) {
        try {
            new Thread(new Server()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
