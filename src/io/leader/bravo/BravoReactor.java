package io.leader.bravo;

import io.leader.util.ExecutorServiceUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;

public class BravoReactor extends Thread {
    final Selector selector;
    final ServerSocketChannel serverSocketChannel;

    public BravoReactor(int bindPort) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(bindPort);
        serverSocketChannel.socket().bind(address);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("started at "+address);
    }

    @Override
    public void run() {

        while (true) {
            Set<SelectionKey> selectedKeys = null;
            try {
                selector.select(1000);
                selectedKeys = selector.selectedKeys();

            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            for (SelectionKey key : selectedKeys){
                if (key.isAcceptable()){
                    new NIOAcceptor(key).run();

                }else if (key.attachment() instanceof BravoIOHandler){
                    BravoIOHandler ioHandler = (BravoIOHandler) key.attachment();
                    ExecutorServiceUtil.getInstance().submit(ioHandler);
                }

            }

            selectedKeys.clear();
        }
    }

}
