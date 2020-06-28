package ru.happyshark.cloudstorage.client;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class SampleNIOClient {

    public static void main(String[] args) throws IOException {

        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 8189));
        ByteBuffer buffer = ByteBuffer.allocate(256);

        RandomAccessFile randomAccessFile = new RandomAccessFile("./client-files/downloaded.txt", "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();

        while (socketChannel.read(buffer) > 0) {
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
        }

        fileChannel.close();
        randomAccessFile.close();
        socketChannel.close();
    }
}
