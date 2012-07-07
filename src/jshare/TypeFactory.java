package jshare;

import jshare.types.Etherpad;
import jshare.types.IDocType;

public class TypeFactory {
	static IDocType getInstance(String type) {
		if (type.equals("etherpad"))
			return new Etherpad();
		return null;
	}
}
