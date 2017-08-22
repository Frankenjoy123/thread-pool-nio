package io.leader.bravo;

import com.sun.org.apache.xpath.internal.operations.String;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class BravoIOHandler implements Runnable{

    private final SocketChannel socketChannel;
    private final SelectionKey selectionKey;

    private ByteBuffer writeBuffer;
    private ByteBuffer readBuffer;

    private int lastReadedPos;


    public BravoIOHandler(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel;

        socketChannel.configureBlocking(false);
        selectionKey = socketChannel.register(selector, 0);
        selectionKey.interestOps(SelectionKey.OP_READ);

        writeBuffer = ByteBuffer.allocate(1024*2);
        readBuffer = ByteBuffer.allocate(100);

        selectionKey.attach(this);
        writeBuffer.put("you are connected to 9000 port\r\n".getBytes());
        writeBuffer.flip();

        doWriteData();

    }

    private void doWriteData() throws IOException {

        int writeSize = socketChannel.write(writeBuffer);
        System.out.println("write to channel : " +writeSize);

        if (writeBuffer.hasRemaining()){
            System.out.println("remaining : " + writeBuffer.remaining());
            writeBuffer.compact();

            selectionKey.attach(writeBuffer);

            //增加兴趣事件为write to socketchannel
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);

        }else {
            System.out.println("write to channel finished");
            writeBuffer.clear();
            //对写不感兴趣，对读感兴趣
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        }

    }

    @Override
    public void run() {

        if (selectionKey.isReadable()){
            try {
                doReadData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (selectionKey.isWritable()){
            try {
                doWriteData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void doReadData() throws IOException {

        int lastMessagePos = 0;
        java.lang.String readLine = null;

        int readSize = socketChannel.read(readBuffer);
        System.out.println("readSize : " +readSize);

        int readEndPos = readBuffer.position();

        for (int i=lastMessagePos ;i< readEndPos ;i++){
            if (readBuffer.get(i)==13){
                byte[] lineBytes = new byte[i-lastMessagePos];
                readBuffer.position(lastMessagePos);
                readBuffer.get(lineBytes);

                lastMessagePos=i;

                readLine = new java.lang.String(lineBytes);
                System.out.println("you type : " + readLine);

            }

        }

        if (readLine !=null){
            selectionKey.interestOps(selectionKey.interestOps()&~SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            processCommand(readLine);

        }

        if (readBuffer.position()>readBuffer.capacity()/2){
            System.out.println("recycle read buffer");

            //压缩，并保留还未处理的数据
            readBuffer.limit(readBuffer.position());
            readBuffer.position(lastMessagePos);

            readBuffer.compact();
            lastMessagePos=0;
        }


    }

    private void processCommand(java.lang.String readLine) {

    }
}
