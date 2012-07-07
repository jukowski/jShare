package jshare.types;

public class DocOpPair {
	IDocOp op1, op2;
	
	public DocOpPair() {
	}

	public DocOpPair(IDocOp op1, IDocOp op2) {
		this.op1 = op1;
		this.op2 = op2;
	}

	public IDocOp getOp1() {
		return op1;
	}
	public IDocOp getOp2() {
		return op2;
	}
	public void setOp1(IDocOp op1) {
		this.op1 = op1;
	}
	public void setOp2(IDocOp op2) {
		this.op2 = op2;
	}
}
