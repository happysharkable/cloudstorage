package ru.happyshark.cloudstorage.client;

import com.sun.xml.internal.rngom.digested.DContainerPattern;
import ru.happyshark.cloudstorage.library.NetworkUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public class Client {

    CountDownLatch networkStarter;

    public Client() {
        networkStarter = new CountDownLatch(1);
    }

    public void connect(Controller controller, String address, int port, String login, String password) throws Exception {
        System.out.println("Trying to connect to " + address + ":" + port);
        new Thread(() -> Network.getInstance().start(controller, networkStarter, address, port)).start();
        networkStarter.await();
    }

    public void disconnect() {
        Network.getInstance().stop();
    }

    public void sendFile(Path path) throws IOException {
        NetworkUtils.sendFile(path, Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            if (future.isSuccess()) {
                System.out.println("Файл успешно передан");
            }
        });
    }
}
