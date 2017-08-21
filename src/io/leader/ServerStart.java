package io.leader;

import java.io.IOException;

public class ServerStart {

	public static void main(String[] args) throws IOException {
		MyNIORector reacot=new MyNIORector(9000);
		reacot.start();
	}

}
