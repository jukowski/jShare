package jshare;

import jshare.types.ICallBack;

import org.junit.Test;

public class ConnectionTest {

	Connection conn;
	
	@Test
	public void test() {
		conn = new Connection("http://localhost:8000/cometd");
		conn.open("test2", "etherpad", new ICallBack() {
			
			@Override
			public void emit(Object... data) {
				if (data[0].equals("error")) {
					System.out.println("Error opening "+ (String)data[1]);
					return;
				}
				System.out.println(data[0].getClass().getName());
			}
		});
	}

}
