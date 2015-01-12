package net.akehurst.language.core.parser;

public interface INode extends IParseTreeVisitable{

	INodeType getNodeType() throws ParseTreeException;

	String getName();
	
	int getLength();
	
	INode deepClone();
	
}
