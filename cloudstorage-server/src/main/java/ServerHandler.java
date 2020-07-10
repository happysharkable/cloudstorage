import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.happyshark.cloudstorage.library.LocalUtils;
import ru.happyshark.cloudstorage.library.NetworkUtils;
import sun.nio.ch.Net;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        IDLE,
        COMMAND_LENGTH, COMMAND, COMMAND_HANDLE,
        NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private String receivedCommand;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == (byte) 25) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else if (readed == (byte) 26) {
                    currentState = State.COMMAND_LENGTH;
                    System.out.println("STATE: Waiting for command");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            }

            if (currentState == State.COMMAND_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get command length");
                    nextLength = buf.readInt();
                    currentState = State.COMMAND;
                }
            }

            if (currentState == State.COMMAND) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] commandBytes = new byte[nextLength];
                    buf.readBytes(commandBytes);
                    receivedCommand = new String(commandBytes, StandardCharsets.UTF_8);
                    System.out.println("STATE: Command received - " + receivedCommand);
                    currentState = State.COMMAND_HANDLE;
                }
            }

            if (currentState == State.COMMAND_HANDLE) {
                if (receivedCommand.equals("/list")) {
                    List<String> fileList = LocalUtils.getFileListFromDirectory(Paths.get("./server-files"));
                    NetworkUtils.sendDataString(String.join(";", fileList), ctx.channel());
                }
                if (receivedCommand.contains("/file")) {
                    String requestedFile = receivedCommand.split(" ")[1];
                    NetworkUtils.sendFile(Paths.get("./server-files/" + requestedFile),ctx.channel(), future -> {
                        if (future.isSuccess()) System.out.println("File " + requestedFile + " transferred");
                    });
                }
                if (receivedCommand.contains("/delete")) {
                    String fileToDelete = receivedCommand.split(" ")[1];
                    Files.deleteIfExists(Paths.get("./server-files/" + fileToDelete));
                    NetworkUtils.sendCommand("/update", ctx.channel());
                }
                receivedCommand = null;
                currentState = State.IDLE;
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    nextLength = buf.readInt();
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received - _" + new String(fileName, StandardCharsets.UTF_8));
                    out = new BufferedOutputStream(new FileOutputStream("./server-files/" + new String(fileName)));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        System.out.println("File received");
                        out.close();
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
