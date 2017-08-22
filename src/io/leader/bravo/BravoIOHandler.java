package io.leader.bravo;

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

    private int lastMessagePos;


    public BravoIOHandler(final Selector selector , SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;

        socketChannel.configureBlocking(false);
        selectionKey = socketChannel.register(selector, 0);
        selectionKey.interestOps(SelectionKey.OP_READ);

        writeBuffer = ByteBuffer.allocate(1024*2);
        readBuffer = ByteBuffer.allocate(100);

        //绑定会话
        selectionKey.attach(this);
        writeBuffer.put("Welcome Leader.us Power Man Java Course ...\\r\\nTelnet".getBytes());
        writeBuffer.flip();

        doWriteData();

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
                selectionKey.cancel();
                try {
                    socketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private void doWriteData() throws IOException {

        int writeSize = socketChannel.write(writeBuffer);
        System.out.println("write to channel : " +writeSize);

        if (writeBuffer.hasRemaining()){
            System.out.println("writed "+writeSize+" not write finished  so bind to session ,remains "+ writeBuffer.remaining());

            //增加兴趣事件为write to socketchannel
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);

        }else {
            System.out.println("write to channel finished");
            writeBuffer.clear();
            //对写不感兴趣，对读感兴趣
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        }

    }

    private void doReadData() throws IOException {

        String readLine = null;

        int readSize = socketChannel.read(readBuffer);
        System.out.println("readSize : " +readSize);

        int readEndPos = readBuffer.position();

        for (int i=lastMessagePos ;i< readEndPos ;i++){
            if (readBuffer.get(i)==13){
                byte[] lineBytes = new byte[i-lastMessagePos];
                readBuffer.position(lastMessagePos);
                readBuffer.get(lineBytes);

                lastMessagePos=i;

                readLine = new String(lineBytes);
                System.out.println("received line ,lenth:"+readLine.length()+" value "+readLine);
                break;

            }

        }

        if (readLine !=null){
            selectionKey.interestOps(selectionKey.interestOps()&~SelectionKey.OP_READ);

            processCommand(readLine);

        }

        if (readBuffer.position()>readBuffer.capacity()/2){
            System.out.println(" rewind read byte buffer ,get more space  "+readBuffer.position());

            //压缩，并保留还未处理的数据
            readBuffer.limit(readBuffer.position());
            readBuffer.position(lastMessagePos);

            readBuffer.compact();
            lastMessagePos=0;
        }


    }

    private void processCommand(String readedLine) throws IOException {
        if(readedLine.startsWith("dir"))
        {
            readedLine="cmd  /c "+readedLine;
            String result=LocalCmandUtil.callCmdAndgetResult(readedLine);
            writeBuffer.put(result.getBytes("GBK"));
            writeBuffer.put("\r\nTelnet>".getBytes());
        }else
        {
            for (int i = 0; i < writeBuffer.capacity()-10 ; i++) {
                writeBuffer.put((byte) ('a' + i % 25));
            }
            writeBuffer.put("\r\nTelnet>".getBytes());
        }
        writeBuffer.flip();
        doWriteData();
    }
}
