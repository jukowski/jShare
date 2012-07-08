package jshare;

import java.util.HashMap;
import java.util.Map;

import jshare.types.ICallBack;
import jshare.types.IMessage;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.BayeuxClient.State;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.HttpClient;

public class Connection {
	String host;
	Map<String, Doc> docs;
	String id;
	String state;
	String lastError;
	BayeuxClient client;

	String lastReceivedDoc;

	public String getState() {
		return state;
	}

	private class HandshakeHandler implements ClientSessionChannel.MessageListener {

		@Override
		public void onMessage(ClientSessionChannel channel, Message message) {
			//System.out.println("starting to listen to /client/"+client.getId());
//			client.getChannel("/client/"+client.getId()).addListener(new MessageHandler());
		}
	}
	
	private class MessageHandler implements ClientSessionChannel.MessageListener {
		@Override
		public void onMessage(ClientSessionChannel channel, Message message) {
			System.out.println(message.getChannel()+" : Message "+message);
			if (message.containsKey("auth")) {
				state = "ok";
				id = (String) message.get("auth");
			} else {
				lastError = (String) message.get("error");
				disconnect();
			}
			IMessage msg = new CommMessage().unserialize(message);
			String docName = msg.getDoc();

			if (docName != null)
				lastReceivedDoc = docName;
			else
				docName = lastReceivedDoc;
			msg.setDoc(docName);

			if (docs.containsKey(docName))
				docs.get(docName)._onMessage(msg);
			else
				error("Unhandled message"+message+" "+msg.serialize());

		}
	}

	public void error(String str) {
		System.out.println(str);
	}

	public void disconnect() {

	}

	public Connection(String host) {
		HttpClient httpClient = new HttpClient();
		try {
			httpClient.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block                      
			e.printStackTrace();
		}

		docs = new HashMap<String, Doc>();

		// Prepare the transport                                    
		Map<String, Object> options = new HashMap<String, Object>();
		ClientTransport transport = LongPollingTransport.create(options, httpClient);

		client = new BayeuxClient(host, transport);
		client.getChannel(Channel.META_HANDSHAKE).addListener(new HandshakeHandler());

		client.handshake();
		client.getChannel("/**").addListener(new MessageHandler());
		open("test2", "etherpad", new ICallBack() {

			@Override
			public void emit(Object... data) {
				// TODO Auto-generated method stub
				
			}
		});
		client.waitFor(100, State.CONNECTED);
	}

	public void waitFor(int milis, State state) {
		client.waitFor(milis, state);
	}
	
	public Doc open(String docName, String type, ICallBack callback) {
		if (!client.isConnected()) {
			callback.emit("error", "connection closed");
			return null;
		}
		return makeDoc(docName, new DocMetadata().setCreate(true).setType(type), callback);
	}

	private Doc makeDoc(String name, IDocMetadata meta, ICallBack callback) {
		if (docs.containsKey(name)) {
			callback.emit("error", "Document "+name+" already open");
		}
		Doc doc = new Doc(this, name, meta);
		docs.put(name, doc);
		doc.open(callback);
		return doc;
	}

	void send(IMessage msg) {
		Map<String, Object> serialized = msg.serialize();
		serialized.put("clientId", client.getId());
		client.getChannel("/server").publish(serialized);
	}

	public String getId() {
		return id;
	}
}
