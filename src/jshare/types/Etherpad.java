package jshare.types;

import java.util.Map;

public class Etherpad implements IDocType {

	@Override
	public String getName() {
		return "etherpad";
	}

	@Override
	public ISnapshot create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISnapshot apply(ISnapshot source, IDocOp op) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDocOp transform(IDocOp op1, IDocOp op2, TransformSide side) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDocOp compose(IDocOp op1, IDocOp op2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> serializeSnapshot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> serializeOp() {
		// TODO Auto-generated method stub
		return null;
	}

}
