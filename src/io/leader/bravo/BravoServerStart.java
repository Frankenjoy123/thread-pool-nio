package io.leader.bravo;

import java.io.IOException;

public class BravoServerStart {

	public static void main(String[] args) throws IOException {
		BravoReactor reactor=new BravoReactor(9000);
		reactor.start();
	}

}
