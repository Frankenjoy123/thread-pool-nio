package io.leader.alpha;

import java.io.IOException;

public class ServerStart {

	public static void main(String[] args) throws IOException {
		MyNIOReactor reactor=new MyNIOReactor(9000);
		reactor.start();
	}

}
