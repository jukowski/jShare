package jshare;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jshare.types.DocOpPair;
import jshare.types.ICallBack;
import jshare.types.IDocOp;
import jshare.types.IDocType;
import jshare.types.IInvert;
import jshare.types.IMessage;
import jshare.types.IMessageMetadata;
import jshare.types.INormalizable;
import jshare.types.ISnapshot;
import jshare.types.ITransformX;
import jshare.types.TransformSide;


public class Doc {
	Integer version;
	ISnapshot snapshot;
	IDocType type;
	Connection connection;

	boolean _create;
	boolean create;
	boolean created;
	boolean autoOpen;

	IDocOp inflightOp;
	List<ICallBack> inflightCallbacks;
	List<String> inflightSubmittedIds;

	IDocOp pendingOp;
	List<ICallBack> pendingCallbacks;

	ArrayList<IDocOp> serverOps;

	String state;
	String name;

	ICallBack _openCallback;
	ICallBack _closeCallback;

	public Doc(Connection connection, String name, IDocMetadata request) {
		this.connection = connection;
		this.name = name;
		if (request != null) {
			version = request.getVersion();
			snapshot = request.getSnapshot();
			type = request.getDocType();
			_create = request.getCreate();
		}
		this.state = "closed";
		this.autoOpen = false;
		inflightCallbacks = new LinkedList<ICallBack>();
		inflightSubmittedIds = new LinkedList<String>();
		pendingCallbacks = new LinkedList<ICallBack>();
		serverOps = new ArrayList<IDocOp>();
	}

	//  Transform a server op by a client op, and vice versa.
	DocOpPair _xf (IDocOp client, IDocOp server) {
		if (type  instanceof ITransformX) {
			return ((ITransformX) type ).transformX(client, server);
		} else {
			IDocOp client_ = type.transform(client, server, TransformSide.Left);
			IDocOp server_ = type.transform(server, client, TransformSide.Right);
			return new DocOpPair(client_, server_);
		}
	}

	void emit(String event, Object... args) {

	}

	void _otApply(IDocOp docOp, Boolean isRemote) {
		ISnapshot oldSnapshot = snapshot;
		snapshot = type.apply(snapshot, docOp);

		//     Its important that these event handlers are called with oldSnapshot.
		//     The reason is that the OT type APIs might need to access the snapshots to
		//     determine information about the received op.

		this.emit("change", docOp, oldSnapshot);
		if (isRemote) 
			this.emit("remoteop", docOp, oldSnapshot);
	}

	void _connectionStateChanged (String state, Object ...data) {
		if (state.equals("disconnected")) {
			this.state = "closed";
			//			# This is used by the server to make sure that when an op is resubmitted it
			//			# doesn't end up getting applied twice.
			if (inflightOp != null)
				inflightSubmittedIds.add(connection.id);

			this.emit("closed");
		} else if (state.equals("ok")) {
			//			# Might be able to do this when we're connecting... that would save a roundtrip.
			if (autoOpen) {
				open(null);
			}
		} else if (state.equals("stopped")) {
			if (this._openCallback != null)
				this._openCallback.emit(data);
		}
		this.emit(state, data);
	}

	/**
	 * some black magic goes on in the original method. Trying to go around.
	 * @param type
	 */
	public void _setType (String type) {
		this.type = DocTypeFactory.getType(type);
	}

	void handleError(String error) {
		System.out.println(error);
	}

	public void _onMessage(IMessage msg) {
		if (msg.getOpen()) {
			this.state = "open";
			this._create = false;
			if (this.created == false) {
				this.created = !!msg.getCreate();
			}
			if (msg.hasType()) {
				this._setType(msg.getType());
			}
			if (msg.getCreate()) {
				this.created = true;
				this.snapshot = this.type.create();
			} else {
				this.created = false;
				if (msg.hasSnapshot()) {
					this.snapshot = msg.getSnapshot();
				}
			}
			if (msg.hasV()) {
				this.version = msg.getV();
			}
			if (this.inflightOp != null) {
				IMessage response = new CommMessage();
				response.setDoc(this.name);
				response.setInflightOp(this.inflightOp);
				response.setV(this.version);
				if (this.inflightSubmittedIds.size()>0) {
					response.setDupIfSource(this.inflightSubmittedIds);
				}
				this.connection.send(response);
			} else {
				this.flush();
			}
			this.emit("open");
			if (this._openCallback != null) {
				this._openCallback.emit();
				return;
			}
			else 
				return;
		} else if (msg.getOpen() == false) {
			if (msg.getError()!=null) {
				handleError("Could not open document: " + msg.getError());
				this.emit("error", msg.getError());
				if (_openCallback != null) {
					this._openCallback.emit(msg.getError());
				}
			}
			this.state = "closed";
			this.emit("closed");
			if (this._closeCallback != null) {
				this._closeCallback.emit();
			}
			this._closeCallback = null;
		} else if (msg.getOp() == null) {

		} else if ((msg.getOp() == null && msg.getV() != 0) || (msg.getOp()!=null && (this.inflightSubmittedIds.contains(msg.getMeta().getSource())))) {
			IDocOp oldInflightOp = this.inflightOp;
			this.inflightOp = null;
			this.inflightSubmittedIds.clear();
			if (msg.getError()!=null) {
				if (this.type instanceof IInvert) {
					IDocOp undo = ((IInvert) this.type).invert(oldInflightOp);
					if (this.pendingOp != null) {
						DocOpPair _ref1 = this._xf(this.pendingOp, undo);
						this.pendingOp = _ref1.getOp1();
						undo = _ref1.getOp2();
					}
					this._otApply(undo, true);
				} else {
					this.emit("error", "Op apply failed (" + msg.getError() + ") and the op could not be reverted");
				}
				for (ICallBack callback : this.inflightCallbacks) {
					callback.emit(msg.getError());
				}
			} else {
				if (msg.getV() != this.version) {
					handleError("Invalid version from server");
					return;
				}
				this.serverOps.set(this.version, oldInflightOp);
				this.version++;
				this.emit("acknowledge", oldInflightOp);
				for (ICallBack callback : this.inflightCallbacks) {
					callback.emit(null, oldInflightOp);
				}
			}
			this.flush();
			return;
		} else if (msg.getOp()!=null) {
			if (msg.getV() < this.version) {
				return;
			}
			if (!msg.getDoc().equals(this.name)) {
				this.emit("error", "Expected docName '" + this.name + "' but got " + msg.getDoc());
				return;
			}
			if (msg.getV() != this.version) {
				this.emit("error", "Expected version " + this.version + " but got " + msg.getV());
				return;
			}
			IDocOp op = msg.getOp();
			this.serverOps.set(this.version, op);
			IDocOp docOp = op;
			if (this.inflightOp != null) {
				DocOpPair _ref4 = this._xf(this.inflightOp, docOp);
				this.inflightOp = _ref4.getOp1();
				docOp = _ref4.getOp2();
			}
			if (this.pendingOp != null) {
				DocOpPair _ref5 = this._xf(this.pendingOp, docOp);
				this.pendingOp = _ref5.getOp1();
				docOp = _ref5.getOp2();
			}
			this.version++;
			this._otApply(docOp, true);
			return;
		} else if (msg.getMeta() != null) {
			IMessageMetadata _ref6 = msg.getMeta(); 
			String []path = _ref6.getPath();
			String value = _ref6.getValue();
			if (path != null && path[0].equals("shout")) {
				this.emit("shout", value);
				return;
			} else
				handleError("Unhandled meta op:"+ msg);
		}
	}

	void flush() {
		if (!(this.connection.state.equals("ok") && this.inflightOp == null && this.pendingOp != null)) {
			return;
		}
		this.inflightOp = this.pendingOp;
		this.inflightCallbacks = this.pendingCallbacks;
		this.pendingOp = null;
		this.pendingCallbacks.clear();
		IMessage sendMessage = new CommMessage();
		sendMessage.setDoc(this.name);
		sendMessage.setOp(this.inflightOp);
		sendMessage.setV(this.version);
		this.connection.send(sendMessage);
		return ;
	}

	void submitOp(IDocOp op, ICallBack callback) {
		if (this.type instanceof INormalizable) {
			op = ((INormalizable) this.type).normalize(op);
		}
		this.snapshot = this.type.apply(this.snapshot, op);
		if (this.pendingOp != null) {
			this.pendingOp = this.type.compose(this.pendingOp, op);
		} else {
			this.pendingOp = op;
		}
		if (callback != null) {
			this.pendingCallbacks.add(callback);
		}
		this.emit("change", op);
		this.flush();
	};

	void shout(IMessage msg) {
		IMessage toSend = new CommMessage();
		toSend.setDoc(this.name);
		// TODO: This makes the Message structure completely unstructured. Maybe implement it later.
		/*		
		return this.connection.send({
			doc: this.name,
			meta: {
			path: ['shout'],
			value: msg
		}
		});
		 */
	};

	void open(final ICallBack callback) {
		IMessage message = new CommMessage();
		this.autoOpen = true;
		if (!this.state.equals("closed")) {
			return;
		}
		message.setDoc(this.name);
		message.setOpen(true);
		if (this.snapshot == null) {
			message.setSnapshot(null);
		}
		if (this.type != null) {
			message.setType(this.type.getName());
		}
		message.setV(this.version);
		if (this._create) {
			message.setCreate(true);
		}
		this.connection.send(message);
		this.state = "opening";
		this._openCallback = new ICallBack() {

			@Override
			public void emit(Object... error) {
				_openCallback = null;
				if (callback != null)
					callback.emit(error);
			}
		};
	};

	void close(ICallBack callback) {
		this.autoOpen = false;
		if (this.state.equals("closed")) {
			if (callback != null)
				callback.emit();
			return;
		}
		IMessage message = new CommMessage();
		message.setDoc(this.name);
		message.setOpen(false);
		this.connection.send(message);
		this.state = "closed";
		this.emit("closing");
		this._closeCallback = callback;
	};

}

