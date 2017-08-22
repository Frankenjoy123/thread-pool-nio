package io.leader.alpha;

import io.leader.util.ExecutorServiceUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;

public class MyNIOReactor extends Thread {
	final Selector selector;
	final ServerSocketChannel serverSocketChannel;

	public MyNIOReactor(int bindPort) throws IOException {
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

			Iterator<SelectionKey> iterator = selectedKeys.iterator();
			while (iterator.hasNext()){

				SelectionKey selectedKey = iterator.next();

				if ( (selectedKey.readyOps()&SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT){

					ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectedKey.channel();


					try {
						SocketChannel socketChannel = serverSocketChannel.accept();

						if (socketChannel !=null){

							socketChannel.configureBlocking(false);

							socketChannel.register(selectedKey.selector(), SelectionKey.OP_READ);

							socketChannel.write(ByteBuffer.wrap("hello world \r\n".getBytes("utf-8")));
						}

					} catch (ClosedChannelException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}else if ((selectedKey.readyOps()& SelectionKey.OP_READ) == SelectionKey.OP_READ){

					SocketChannel socketChannel = (SocketChannel) selectedKey.channel();

					ByteBuffer byteBuffer = (ByteBuffer) selectedKey.attachment();

					if (byteBuffer==null || !byteBuffer.hasRemaining()){
						int size = 0;
						try {
							size = socketChannel.socket().getSendBufferSize();
						} catch (SocketException e) {
							e.printStackTrace();
						}

						byteBuffer = ByteBuffer.allocate(size*5+2);

						for (int i=0; i<byteBuffer.capacity()-2 ;i++){
							byteBuffer.put((byte) ('a'+i%25));
						}
						byteBuffer.put("\r\n".getBytes());

						byteBuffer.flip();
					}

					try {

						int writeSize = socketChannel.write(byteBuffer);
						System.out.println("write to channel : " +writeSize);

						if (byteBuffer.hasRemaining()){
							System.out.println("remaining : " + byteBuffer.remaining());
							byteBuffer.compact();
							selectedKey.attach(byteBuffer);
							//调整兴趣事件为write to socketchannel, 避免死循环
							selectedKey.interestOps(selectedKey.interestOps() | SelectionKey.OP_WRITE);
						}else {
							System.out.println("write to channel finished");
							selectedKey.attach(null);
							selectedKey.interestOps(selectedKey.interestOps() & ~SelectionKey.OP_WRITE);
						}


					} catch (IOException e) {
						e.printStackTrace();
					}

				}

				//监听 write to socketchannel , 若byteBuffer有remaining的，继续write to socketchannel
				else if ((selectedKey.readyOps()& SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE){

					ByteBuffer byteBuffer = (ByteBuffer) selectedKey.attachment();

					SocketChannel socketChannel = (SocketChannel) selectedKey.channel();

					if (byteBuffer!=null && byteBuffer.hasRemaining()){

						try {
							int writeSize = socketChannel.write(byteBuffer);
							System.out.println("write to socket channel :" + writeSize);

							if (byteBuffer.hasRemaining()){
								System.out.println("remaining size: " + byteBuffer.remaining());

								byteBuffer.compact();
								selectedKey.attach(byteBuffer);
								selectedKey.interestOps(selectedKey.interestOps() | SelectionKey.OP_WRITE);
							}else {
								System.out.println("write to channel finished");
								selectedKey.attach(null);
								selectedKey.interestOps(selectedKey.interestOps() & ~SelectionKey.OP_WRITE);
							}


						} catch (IOException e) {
							e.printStackTrace();
						}

					}

				}

				iterator.remove();
			}

		}
	}


	private void doWithSelectionKey(SelectionKey key){
		ServerSocketChannel serverSocketChannel = null;
		try{
			if (key.isValid()){
				if (key.isAcceptable()){
					serverSocketChannel = (ServerSocketChannel) key.channel();
					SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(key.selector(), SelectionKey.OP_READ);
				} else if (key.isReadable()){
                    ExecutorServiceUtil.getInstance().submit(new InboundHandler(key));

                }
			}

		}catch (Exception e){
			e.printStackTrace();
		}



	}


	class NIOAcceptor {

		public void run() {
			try {
				SocketChannel socketChannel = serverSocketChannel.accept();
				new IOHandler(selector,socketChannel);
				System.out.println("Connection Accepted by Reactor "+Thread.currentThread().getName());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
