package io.leader.alpha;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by frank on 2017/8/21.
 */
public class InboundHandler implements Runnable
{
    private final SelectionKey selectionKey;

    private ByteBuffer readBuffer;

    private ByteBuffer writeBuffer;

    public InboundHandler(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        readBuffer = ByteBuffer.allocate(100);
        writeBuffer = ByteBuffer.allocate(1024*2);
    }

    @Override
    public void run() {

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        int len;
        try {
            len=socketChannel.read(readBuffer);
            if (len>0){
                readBuffer.flip();
                byte[] temp =new byte[readBuffer.remaining()];
                readBuffer.get(temp);
                System.out.println("receive message : " + new String(temp,"utf-8"));

                //查看是否还有未接受完的数据
//                readBuffer = (ByteBuffer) selectionKey.attachment();
//                if (readBuffer == null || !readBuffer.hasRemaining()){
//                    for (int i=0 ; i<writeBuffer.capacity()-2 ;i++){
//                        writeBuffer.put((byte) ('a'+i%25));
//                    }
//                    writeBuffer.flip();
//                    socketChannel.write(writeBuffer);
//
//                    //记录是否写完，留着下次写
//                    if (&writeBuffer.hasRemaining()){
//                        System.out.println("remain : " + writeBuffer.remaining());
//                        writeBuffer.compact();
//                        writeBuffer.mark();
//                        selectionKey.attach(writeBuffer);
//                        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
//                    }
//                } else {
//
//                }



            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
