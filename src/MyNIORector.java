import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Set;

public class MyNIORector extends Thread {
	final Selector selector;
	final ServerSocketChannel serverSocketChannel;

	public MyNIORector(int bindPort) throws IOException {
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
			for(SelectionKey selectedKey:selectedKeys)
			{

				doWithSelectionKey(selectedKey);



				if (selectedKey.isAcceptable()) {
                    new NIOAcceptor().run();
					} else {
						((IOHandler)selectedKey.attachment()).run();
					}
			}
			selectedKeys.clear();

		}
	}


	private void doWithSelectionKey(SelectionKey key){
		ServerSocketChannel serverSocketChannel = null;
		try{
			if (key.isValid()){
				if (key.isAcceptable()){
					serverSocketChannel = (ServerSocketChannel) key.channel();
					SocketChannel socketChannel = serverSocketChannel.accept();
					try {
						socketChannel.configureBlocking(false);
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						socketChannel.register(key.selector(),SelectionKey.OP_READ);
					} catch (ClosedChannelException e) {
						e.printStackTrace();
					}
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
