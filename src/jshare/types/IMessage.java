package jshare.types;

import java.util.List;
import java.util.Map;

public interface IMessage {
    Map<String, Object> serialize();
    IMessage unserialize(Map<String, Object> msg);
	
	String getDoc();
	void setDoc(String doc);
	
	Boolean getOpen();
	void setOpen(Boolean value);
	
	Boolean getCreate();
	void setCreate(Boolean create);
	
	Boolean hasCreated();
	
	String getType();
	void setType(String type);
	boolean hasType();
	
	ISnapshot getSnapshot();
	void setSnapshot(ISnapshot snapshot);
	boolean hasSnapshot();
	
	Integer getV();
	void setV(Integer version);
	boolean hasV();
	
	IDocOp getInflightOp();
	void setInflightOp(IDocOp op);
	
	void setDupIfSource(List<String> inflightSubmittedIds);
	
	String getError();
	
	IDocOp getOp();
	void setOp(IDocOp value);
	
	IMessageMetadata getMeta();
}
