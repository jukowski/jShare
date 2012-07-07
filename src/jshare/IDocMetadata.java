package jshare;

import jshare.types.IDocType;
import jshare.types.ISnapshot;

public interface IDocMetadata {
	Integer getVersion();
	ISnapshot getSnapshot();
	String getDocType();
	boolean getCreate();
	
}
