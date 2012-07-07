package jshare.types;

import java.util.Map;

public interface IDocType {
	String getName();
	ISnapshot create();
	ISnapshot apply(final ISnapshot source, final IDocOp op);
	IDocOp transform(IDocOp op1, IDocOp op2, TransformSide side);
	IDocOp compose(IDocOp op1, IDocOp op2);
	Map<String, Object> serializeSnapshot();
	Map<String, Object> serializeOp();
}
