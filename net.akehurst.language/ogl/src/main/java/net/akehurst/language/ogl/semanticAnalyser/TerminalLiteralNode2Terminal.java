/**
 * Copyright (C) 2015 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.language.ogl.semanticAnalyser;

import net.akehurst.language.core.parser.IBranch;
import net.akehurst.language.core.parser.ILeaf;
import net.akehurst.language.core.parser.INode;
import net.akehurst.language.ogl.semanticStructure.TerminalLiteral;
import net.akehurst.transform.binary.Relation;
import net.akehurst.transform.binary.Transformer;

public class TerminalLiteralNode2Terminal extends AbstractNode2Terminal<TerminalLiteral> {

	@Override
	public String getNodeName() {
		return "LITERAL";
	}

	@Override
	public boolean isValidForRight2Left(TerminalLiteral right) {
		return true;
	}

	@Override
	public TerminalLiteral constructLeft2Right(INode left, Transformer transformer) {
		INode child = ((IBranch) left).getChildren().get(0);
		ILeaf leaf = (ILeaf) child;
		String text = leaf.getMatchedText();
		String literal = text.substring(1, text.length() - 1);
		TerminalLiteral right = new TerminalLiteral(literal);
		return right;
	}

	@Override
	public IBranch constructRight2Left(TerminalLiteral left, Transformer right) {
		return null;
	}

	@Override
	public void configureLeft2Right(INode left, TerminalLiteral right, Transformer arg2) {

	}

	@Override
	public void configureRight2Left(INode left, TerminalLiteral right, Transformer arg2) {
	}

}
