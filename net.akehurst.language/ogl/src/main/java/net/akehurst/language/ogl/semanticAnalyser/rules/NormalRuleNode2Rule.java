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
package net.akehurst.language.ogl.semanticAnalyser.rules;

import net.akehurst.language.core.sppt.SPPTBranch;
import net.akehurst.language.core.sppt.SPPTNode;
import net.akehurst.language.ogl.semanticStructure.AbstractChoice;
import net.akehurst.language.ogl.semanticStructure.GrammarStructure;
import net.akehurst.language.ogl.semanticStructure.Rule;
import net.akehurst.transform.binary.api.BinaryRule;
import net.akehurst.transform.binary.api.BinaryTransformer;

public class NormalRuleNode2Rule implements BinaryRule<SPPTNode, Rule> {

    @Override
    public boolean isValidForLeft2Right(final SPPTNode left) {
        return "normalRule".equals(left.getName());
    }

    @Override
    public boolean isValidForRight2Left(final Rule right) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAMatch(final SPPTNode left, final Rule right, final BinaryTransformer transformer) {
        return true;
    }

    @Override
    public Rule constructLeft2Right(final SPPTNode left, final BinaryTransformer transformer) {
        final SPPTNode grammarNode = left.getParent().getParent().getParent().getParent();
        final GrammarStructure grammar = transformer.transformLeft2Right(GrammarDefinitionBranch2Grammar.class, grammarNode);
        final String name = transformer.transformLeft2Right(IDENTIFIERBranch2String.class, ((SPPTBranch) left).getChild(0));
        final Rule right = new Rule(grammar, name);
        return right;
    }

    @Override
    public SPPTNode constructRight2Left(final Rule right, final BinaryTransformer transformer) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateLeft2Right(final SPPTNode left, final Rule right, final BinaryTransformer transformer) {
        final SPPTNode rhsNode = ((SPPTBranch) left).getChild(2);
        final SPPTNode item = ((SPPTBranch) rhsNode).getChild(0);
        final AbstractChoice ruleItem = transformer.transformLeft2Right((Class<BinaryRule<SPPTNode, AbstractChoice>>) (Class<?>) AbstractNode2Choice.class,
                item);
        right.setRhs(ruleItem);
    }

    @Override
    public void updateRight2Left(final SPPTNode left, final Rule right, final BinaryTransformer transformer) {
        // TODO Auto-generated method stub

    }

}
