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
package net.sourceforge.marathon.ruby.utils;

import org.jruby.ast.*;
import org.jruby.ast.visitor.NodeVisitor;

/**
 * This visitor calls by default the return visitNode method for each visited
 * Node.
 * 
 * @author jpetersen
 */
public abstract class AbstractVisitor implements NodeVisitor {

    /**
     * This method is called by default for each visited Node.
     */
    protected abstract Object visitNode(Node iVisited);

    public Object visitNullNode() {
        return visitNode(null);
    }

    public Object acceptNode(Node node) {
        if (node == null) {
            return visitNullNode();
        } else {
            return node.accept(this);
        }
    }

    public Object visitAliasNode(AliasNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitAndNode(AndNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitArgsCatNode(ArgsCatNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitArgsNode(ArgsNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitArrayNode(ArrayNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitBackRefNode(BackRefNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitBeginNode(BeginNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitBlockArgNode(BlockArgNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitBlockNode(BlockNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitBlockPassNode(BlockPassNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitBreakNode(BreakNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitConstDeclNode(ConstDeclNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitClassVarAsgnNode(ClassVarAsgnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitClassVarDeclNode(ClassVarDeclNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitClassVarNode(ClassVarNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitCallNode(CallNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitCaseNode(CaseNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitClassNode(ClassNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitColon2Node(Colon2Node iVisited) {
        return visitNode(iVisited);
    }

    public Object visitColon3Node(Colon3Node iVisited) {
        return visitNode(iVisited);
    }

    public Object visitConstNode(ConstNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDAsgnNode(DAsgnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDRegxNode(DRegexpNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDStrNode(DStrNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDSymbolNode(DSymbolNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDVarNode(DVarNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDXStrNode(DXStrNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDefinedNode(DefinedNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDefnNode(DefnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDefsNode(DefsNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitDotNode(DotNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitEnsureNode(EnsureNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitEvStrNode(EvStrNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitFCallNode(FCallNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitFalseNode(FalseNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitFlipNode(FlipNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitForNode(ForNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitGlobalAsgnNode(GlobalAsgnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitGlobalVarNode(GlobalVarNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitHashNode(HashNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitInstAsgnNode(InstAsgnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitInstVarNode(InstVarNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitIfNode(IfNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitIterNode(IterNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitLocalAsgnNode(LocalAsgnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitLocalVarNode(LocalVarNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitMultipleAsgnNode(MultipleAsgnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitMatch2Node(Match2Node iVisited) {
        return visitNode(iVisited);
    }

    public Object visitMatch3Node(Match3Node iVisited) {
        return visitNode(iVisited);
    }

    public Object visitMatchNode(MatchNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitModuleNode(ModuleNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitNewlineNode(NewlineNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitNextNode(NextNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitNilNode(NilNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitNotNode(NotNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitNthRefNode(NthRefNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitOpElementAsgnNode(OpElementAsgnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitOpAsgnNode(OpAsgnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitOpAsgnAndNode(OpAsgnAndNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitOpAsgnOrNode(OpAsgnOrNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitOrNode(OrNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitPostExeNode(PostExeNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitPreExeNode(PreExeNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitRedoNode(RedoNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitRescueBodyNode(RescueBodyNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitRescueNode(RescueNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitRetryNode(RetryNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitReturnNode(ReturnNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitRootNode(RootNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitSClassNode(SClassNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitSelfNode(SelfNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitSplatNode(SplatNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitStrNode(StrNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitSValueNode(SValueNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitSuperNode(SuperNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitToAryNode(ToAryNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitTrueNode(TrueNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitUndefNode(UndefNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitUntilNode(UntilNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitVAliasNode(VAliasNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitVCallNode(VCallNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitWhenNode(WhenNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitWhileNode(WhileNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitXStrNode(XStrNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitYieldNode(YieldNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitZArrayNode(ZArrayNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitZSuperNode(ZSuperNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitBignumNode(BignumNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitFixnumNode(FixnumNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitFloatNode(FloatNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitRegexpNode(RegexpNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitSymbolNode(SymbolNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitArgsPushNode(ArgsPushNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitAttrAssignNode(AttrAssignNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitBlockArg18Node(BlockArg18Node iVisited) {
        return visitNode(iVisited);
    }

    public Object visitEncodingNode(EncodingNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitLiteralNode(LiteralNode iVisited) {
        return visitNode(iVisited);
    }

    public Object visitMultipleAsgnNode(MultipleAsgn19Node iVisited) {
        return visitNode(iVisited);
    }

    public Object visitRestArgNode(RestArgNode iVisited) {
        return visitNode(iVisited);
    }
}
