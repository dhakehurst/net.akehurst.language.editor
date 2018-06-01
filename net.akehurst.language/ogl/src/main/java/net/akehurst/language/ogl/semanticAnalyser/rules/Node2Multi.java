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
import net.akehurst.language.ogl.semanticStructure.Multi;
import net.akehurst.language.ogl.semanticStructure.SimpleItem;
import net.akehurst.transform.binary.api.BinaryRule;
import net.akehurst.transform.binary.api.BinaryTransformer;

public class Node2Multi extends AbstractNode2ConcatenationItem<Multi> {

    @Override
    public String getNodeName() {
        return "multi";
    }

    @Override
    public boolean isValidForRight2Left(final Multi right) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAMatch(final SPPTBranch left, final Multi right, final BinaryTransformer transformer) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Multi constructLeft2Right(final SPPTBranch left, final BinaryTransformer transformer) {

        final SPPTNode itemNode = left.getChild(0);
        final SPPTNode multiplicityNode = left.getChild(1);

        final SimpleItem item = transformer.transformLeft2Right((Class<BinaryRule<SPPTNode, SimpleItem>>) (Class<?>) Node2SimpleItem.class, itemNode);

        // TODO: this should really be done with transform rules!
        Multi right = null;
        final String multiplicityString = ((SPPTBranch) multiplicityNode).getChild(0).getName();
        if ("*".equals(multiplicityString)) {
            final int min = 0;
            final int max = -1;
            right = new Multi(min, max, item);
        } else if ("+".equals(multiplicityString)) {
            final int min = 1;
            final int max = -1;
            right = new Multi(min, max, item);
        } else if ("?".equals(multiplicityString)) {
            final int min = 0;
            final int max = 1;
            right = new Multi(min, max, item);
        }
        return right;

    }

    @Override
    public SPPTBranch constructRight2Left(final Multi right, final BinaryTransformer transformer) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateLeft2Right(final SPPTBranch left, final Multi right, final BinaryTransformer transformer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRight2Left(final SPPTBranch left, final Multi right, final BinaryTransformer transformer) {
        // TODO Auto-generated method stub

    }

}
