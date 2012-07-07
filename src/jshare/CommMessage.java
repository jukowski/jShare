package jshare;	

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jshare.types.IDocOp;
import jshare.types.IMessage;
import jshare.types.IMessageMetadata;
import jshare.types.ISnapshot;

public class CommMessage implements IMessage {

	Boolean open;
	Boolean create;
	ISnapshot snapshot;
	Integer v;
	IDocOp inflightOp;
	IDocOp op;
	
	List<String> dupIfSource;
	String type;
	String doc;
	String error;
	
	@Override
	public Boolean getOpen() {
		return open;
	}

	@Override
	public Boolean getCreate() {
		return create;
	}

	@Override
	public Boolean hasCreated() {
		return create != null;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean hasType() {
		return type != null;
	}

	@Override
	public ISnapshot getSnapshot() {
		return snapshot;
	}

	@Override
	public boolean hasSnapshot() {
		return snapshot != null;
	}

	@Override
	public Integer getV() {
		return v;
	}

	@Override
	public boolean hasV() {
		return v != null;
	}

	@Override
	public String getDoc() {
		return doc;
	}

	@Override
	public void setDoc(String doc) {
		this.doc = doc;
	}

	@Override
	public void setV(Integer version) {
		this.v = version;
		
	}

	@Override
	public IDocOp getInflightOp() {
		return inflightOp;
	}

	@Override
	public void setInflightOp(IDocOp op) {
		this.inflightOp = op;
		
	}

	@Override
	public void setDupIfSource(List<String> inflightSubmittedIds) {
		this.dupIfSource = inflightSubmittedIds;
		
	}

	@Override
	public String getError() {
		return error;
	}

	@Override
	public IDocOp getOp() {
		return op;
	}

	@Override
	public IMessageMetadata getMeta() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOpen(Boolean value) {
		open = value;
	}

	@Override
	public void setCreate(Boolean create) {
		this.create = create;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void setSnapshot(ISnapshot snapshot) {
		this.snapshot = snapshot;
	}

	@Override
	public void setOp(IDocOp value) {
		this.op = value;
	}

	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> result = new HashMap<String, Object>();
		if (open!=null) result.put("open", true);
		if (create!=null) result.put("create", true);
		if (snapshot!=null) result.put("snapshot", snapshot.serialize());
		if (v!=null) result.put("v", v);
		if (inflightOp!=null) result.put("inflightOp", inflightOp.serialize());
		if (op!=null) result.put("op", op.serialize());
		if (dupIfSource != null) result.put("dupIfSource", dupIfSource);
		if (type!=null) result.put("type",type);
		if (doc!=null) result.put("doc",doc);
		if (error!=null) result.put("error",error);
		return result;
	}

	@Override
	public IMessage unserialize(Map<String, Object> msg) {
		if (msg.containsKey("open")) this.open = (Boolean) msg.get("open");
		if (msg.containsKey("create")) this.create = (Boolean) msg.get("create");
		if (msg.containsKey("snapshot")) this.snapshot = (ISnapshot) msg.get("snapshot");
		return this;
	}

}
