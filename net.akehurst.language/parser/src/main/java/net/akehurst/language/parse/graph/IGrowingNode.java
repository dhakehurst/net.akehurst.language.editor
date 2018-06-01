package net.akehurst.language.parse.graph;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.akehurst.language.api.sppt.FixedList;
import net.akehurst.language.grammar.parser.runtime.RuntimeRule;

public interface IGrowingNode {
    RuntimeRule getRuntimeRule();

    int getRuntimeRuleNumber();

    int getStartPosition();

    int getNextInputPosition();

    int getNextItemIndex();

    int getPriority();

    int getMatchedTextLength();

    boolean isEmptyRuleMatch();

    boolean isSkip();

    boolean getHasCompleteChildren();

    boolean getCanGrowWidth();

    boolean getCanGraftBack(IGrowingNode.PreviousInfo previous);

    List<RuntimeRule> getNextExpectedTerminals();

    boolean getCanGrowWidthWithSkip();

    boolean hasNextExpectedItem();

    boolean getExpectsItemAt(RuntimeRule runtimeRule, int atPosition);

    // boolean getIsStacked();

    public static final class PreviousInfo {
        public PreviousInfo(final IGrowingNode node, final int atPosition) {
            this.node = node;
            this.atPosition = atPosition;
            // a skip node can have a previous at -1
            // assert atPosition >= 0;
            this.hashCode_cache = Objects.hash(node, atPosition);
        }

        public IGrowingNode node;
        public int atPosition;

        int hashCode_cache;

        @Override
        public int hashCode() {
            return this.hashCode_cache;
        }

        @Override
        public boolean equals(final Object arg) {
            if (!(arg instanceof PreviousInfo)) {
                return false;
            }
            final PreviousInfo other = (PreviousInfo) arg;
            return this.atPosition == other.atPosition && this.node == other.node;
        }

        @Override
        public String toString() {
            return "(".concat(Integer.toString(this.atPosition)).concat("|").concat(this.node.toString()).concat(")");
        }
    }

    Set<PreviousInfo> getPrevious();

    void newPrevious();

    void addPrevious(IGrowingNode previousNode, int atPosition);

    Set<IGrowingNode> getNext();

    void addNext(IGrowingNode value);

    void removeNext(IGrowingNode value);

    List<RuntimeRule> getNextExpectedItem();

    boolean isLeaf();

    boolean isEmptyLeaf();

    FixedList<ICompleteNode> getGrowingChildren();

    String toStringTree(boolean withChildren, boolean withPrevious);

    String toStringId();

}
