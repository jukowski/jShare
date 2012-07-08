package testing;

import java.util.HashMap;
import java.util.Map;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.BayeuxClient.State;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.HttpClient;

public class Test {

	/**
	 * @param args
	 */
	
	private static class MessageHandler implements ClientSessionChannel.MessageListener {

		@Override
		public void onMessage(ClientSessionChannel channel, Message message) {
			System.out.println("On "+message.getChannel());
			System.out.println("got"+message.toString());
		}
	}
	
	public static void main(String[] args) {
		BayeuxClient client;
		HttpClient httpClient = new HttpClient();
		try {
			httpClient.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block                      
			e.printStackTrace();
		}
		// Prepare the transport                                    
		Map<String, Object> options = new HashMap<String, Object>();
		ClientTransport transport = LongPollingTransport.create(options, httpClient);

		client = new BayeuxClient("http://localhost:8000/cometd", transport);
		client.setDebugEnabled(true);
		client.handshake();
		    
		client.getChannel("/**").addListener(new MessageHandler());
		client.getChannel("/server").publish("{msg:'hello'}");
		client.waitFor(10000, State.DISCONNECTED);

	}

}
