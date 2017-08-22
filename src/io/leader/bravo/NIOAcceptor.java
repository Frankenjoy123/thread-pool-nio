package io.leader.bravo;

import io.leader.util.ExecutorServiceUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOAcceptor {

    private SelectionKey selectionKey;

    public NIOAcceptor(SelectionKey selectionKey) {
        this.selectionKey=selectionKey;
    }

    public void run(){
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();


        try {
            SocketChannel socketChannel = serverSocketChannel.accept();

            BravoIOHandler ioHandler = new BravoIOHandler(socketChannel,selectionKey.selector());

//            ExecutorServiceUtil.getInstance().submit(ioHandler);

        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
