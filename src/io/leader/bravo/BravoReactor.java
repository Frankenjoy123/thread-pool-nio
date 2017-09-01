package io.leader.bravo;

import io.leader.alpha.IOHandler;
import io.leader.util.ExecutorServiceUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
                selector.select();
                selectedKeys = selector.selectedKeys();

            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            for (SelectionKey key : selectedKeys){
                if (key.isAcceptable()){
                    new NIOAcceptor().run();

                }else if (key.attachment() instanceof BravoIOHandler){
                    BravoIOHandler ioHandler = (BravoIOHandler) key.attachment();
                    ExecutorServiceUtil.getInstance().submit(ioHandler);
//                    ioHandler.run();
                }

            }

            selectedKeys.clear();
        }
    }

    class NIOAcceptor {

        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                new BravoIOHandler(selector,socketChannel);
                System.out.println("Connection Accepted by Reactor "+Thread.currentThread().getName());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
