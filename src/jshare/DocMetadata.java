package jshare;

import jshare.types.ISnapshot;

public class DocMetadata implements IDocMetadata {

	Integer version;
	String type;
	boolean create;
	
	public DocMetadata() {
	}
	
	public DocMetadata setCreate(boolean create) {
		this.create = create;
		return this;
	}
	
	public DocMetadata setType(String type) {
		this.type = type;
		return this;
	}
	
	@Override
	public Integer getVersion() {
		return version;
	}

	@Override
	public ISnapshot getSnapshot() {
		return null;
	}

	@Override
	public String getDocType() {
		return type;
	}

	@Override
	public boolean getCreate() {
		return create;
	}

}
