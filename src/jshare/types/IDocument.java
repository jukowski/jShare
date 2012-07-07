package jshare.types;

public interface IDocument {
	IDocType getDocType();
	void setDocType(IDocType docType);
	
	int getVersion();
	void setVersion(int version);

	ISnapshot getSnapshot();

}
