/*******************************************************************************
 *  
 *  Copyright (C) 2010 Jalian Systems Private Ltd.
 *  Copyright (C) 2010 Contributors to Marathon OSS Project
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 * 
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Project website: http://www.marathontesting.com
 *  Help: Marathon help forum @ http://groups.google.com/group/marathon-testing
 * 
 *******************************************************************************/
package net.sourceforge.marathon.ruby;

import org.jruby.ast.*;
import org.jruby.ast.visitor.NodeVisitor;

public class DefaultIteratorVisitor implements NodeVisitor {
    protected NodeVisitor _Payload;

    public DefaultIteratorVisitor(NodeVisitor iPayload) {
        _Payload = iPayload;
    }

    public Object visitAliasNode(AliasNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitAndNode(AndNode iVisited) {
        accept(iVisited.getFirstNode());
        iVisited.accept(_Payload);
        accept(iVisited.getSecondNode());
        return null;
    }

    public Object visitArgsNode(ArgsNode iVisited) {
        iVisited.accept(_Payload);
        if (iVisited.getOptArgs() != null) {
            accept(iVisited.getOptArgs());
        }
        return null;
    }

    public Object visitArgsCatNode(ArgsCatNode iVisited) {
        iVisited.accept(_Payload);
        if (iVisited.getFirstNode() != null) {
            accept(iVisited.getFirstNode());
        }
        if (iVisited.getSecondNode() != null) {
            accept(iVisited.getSecondNode());
        }
        return null;
    }

    public Object visitArgsPushNode(ArgsPushNode iVisited) {
        iVisited.accept(_Payload);
        if (iVisited.getFirstNode() != null) {
            accept(iVisited.getFirstNode());
        }
        if (iVisited.getSecondNode() != null) {
            accept(iVisited.getSecondNode());
        }
        return null;
    }

    public Object visitAttrAssignNode(AttrAssignNode iVisited) {
        iVisited.accept(_Payload);
        if (iVisited.getArgsNode() != null) {
            accept(iVisited.getArgsNode());
        }
        if (iVisited.getReceiverNode() != null) {
            accept(iVisited.getReceiverNode());
        }
        return null;
    }

    public Object visitArrayNode(ArrayNode iVisited) {
        iVisited.accept(_Payload);

        for (int i = 0; i < iVisited.size(); i++) {
            accept(iVisited.get(i));
        }

        return null;
    }

    public Object visitBackRefNode(BackRefNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitBeginNode(BeginNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitBlockArgNode(BlockArgNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitBlockNode(BlockNode iVisited) {
        iVisited.accept(_Payload);
        for (int i = 0; i < iVisited.size(); i++) {
            accept(iVisited.get(i));
        }
        return null;
    }

    public Object visitBlockPassNode(BlockPassNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitBreakNode(BreakNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitConstDeclNode(ConstDeclNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getValueNode());
        return null;
    }

    public Object visitClassVarAsgnNode(ClassVarAsgnNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getValueNode());
        return null;
    }

    public Object visitClassVarDeclNode(ClassVarDeclNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitClassVarNode(ClassVarNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitCallNode(CallNode iVisited) {
        accept(iVisited.getReceiverNode());
        if (iVisited.getArgsNode() != null) {
            accept(iVisited.getArgsNode());
        }
        if (iVisited.getIterNode() != null) {
            accept(iVisited.getIterNode());
        }
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitCaseNode(CaseNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitClassNode(ClassNode iVisited) {
        iVisited.accept(_Payload);
        if (iVisited.getSuperNode() != null) {
            accept(iVisited.getSuperNode());
        }
        // NOTE: suprised that this is not used
        // It can be used.
        accept(iVisited.getBodyNode());
        return null;
    }

    public Object visitColon2Node(Colon2Node iVisited) {
        if (iVisited.getLeftNode() != null) {
            accept(iVisited.getLeftNode());
        }
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitColon3Node(Colon3Node iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitConstNode(ConstNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitDAsgnNode(DAsgnNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getValueNode());
        return null;
    }

    public Object visitDRegxNode(DRegexpNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitDStrNode(DStrNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    /**
     * @see NodeVisitor#visitDSymbolNode(DSymbolNode)
     */
    public Object visitDSymbolNode(DSymbolNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitDVarNode(DVarNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitDXStrNode(DXStrNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitDefinedNode(DefinedNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitDefnNode(DefnNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getBodyNode());
        return null;
    }

    public Object visitDefsNode(DefsNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getReceiverNode());
        accept(iVisited.getBodyNode());
        return null;
    }

    public Object visitDotNode(DotNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitEnsureNode(EnsureNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitEvStrNode(EvStrNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitFCallNode(FCallNode iVisited) {
        iVisited.accept(_Payload);
        if (iVisited.getArgsNode() != null) {
            accept(iVisited.getArgsNode());
        }
        if (iVisited.getIterNode() != null) {
            accept(iVisited.getIterNode());
        }
        return null;
    }

    public Object visitFalseNode(FalseNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitFlipNode(FlipNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitForNode(ForNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitGlobalAsgnNode(GlobalAsgnNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getValueNode());
        return null;
    }

    public Object visitGlobalVarNode(GlobalVarNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitHashNode(HashNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitInstAsgnNode(InstAsgnNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getValueNode());
        return null;
    }

    public Object visitInstVarNode(InstVarNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitIfNode(IfNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getCondition());
        accept(iVisited.getThenBody());
        if (iVisited.getElseBody() != null) {
            accept(iVisited.getElseBody());
        }
        return null;
    }

    public Object visitIterNode(IterNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitLocalAsgnNode(LocalAsgnNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getValueNode());
        return null;
    }

    public Object visitLocalVarNode(LocalVarNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitMultipleAsgnNode(MultipleAsgnNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitMatch2Node(Match2Node iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitMatch3Node(Match3Node iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitMatchNode(MatchNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitModuleNode(ModuleNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getBodyNode());
        return null;
    }

    public Object visitNewlineNode(NewlineNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getNextNode());
        return null;
    }

    public Object visitNextNode(NextNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitNilNode(NilNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitNotNode(NotNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getConditionNode());
        return null;
    }

    public Object visitNthRefNode(NthRefNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitOpElementAsgnNode(OpElementAsgnNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitOpAsgnNode(OpAsgnNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitOpAsgnAndNode(OpAsgnAndNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitOpAsgnOrNode(OpAsgnOrNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitOrNode(OrNode iVisited) {
        accept(iVisited.getFirstNode());
        iVisited.accept(_Payload);
        accept(iVisited.getSecondNode());
        return null;
    }

    public Object visitPostExeNode(PostExeNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitPreExeNode(PreExeNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitRedoNode(RedoNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitRescueBodyNode(RescueBodyNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getBodyNode());
        return null;
    }

    public Object visitRescueNode(RescueNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitRetryNode(RetryNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitRootNode(RootNode iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getBodyNode());
        return null;
    }

    public Object visitReturnNode(ReturnNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitSClassNode(SClassNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitSelfNode(SelfNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitSplatNode(SplatNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitStrNode(StrNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitSValueNode(SValueNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitSuperNode(SuperNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitToAryNode(ToAryNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitTrueNode(TrueNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitUndefNode(UndefNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitUntilNode(UntilNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitVAliasNode(VAliasNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitVCallNode(VCallNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitWhenNode(WhenNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitWhileNode(WhileNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitXStrNode(XStrNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitYieldNode(YieldNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitZArrayNode(ZArrayNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitZSuperNode(ZSuperNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitBignumNode(BignumNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitFixnumNode(FixnumNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitFloatNode(FloatNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitRegexpNode(RegexpNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitSymbolNode(SymbolNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitBlockArg18Node(BlockArg18Node iVisited) {
        iVisited.accept(_Payload);
        accept(iVisited.getArgs());
        accept(iVisited.getBlockArg());
        return null;
    }

    public Object visitEncodingNode(EncodingNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitLiteralNode(LiteralNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitMultipleAsgnNode(MultipleAsgn19Node iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    public Object visitRestArgNode(RestArgNode iVisited) {
        iVisited.accept(_Payload);
        return null;
    }

    private void accept(Node node) {
        if (node != null)
            node.accept(this);
    }

}
