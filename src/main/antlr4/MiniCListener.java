// Generated from MiniC.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MiniCParser}.
 */
public interface MiniCListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MiniCParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(MiniCParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(MiniCParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(MiniCParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(MiniCParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#declaratorList}.
	 * @param ctx the parse tree
	 */
	void enterDeclaratorList(MiniCParser.DeclaratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#declaratorList}.
	 * @param ctx the parse tree
	 */
	void exitDeclaratorList(MiniCParser.DeclaratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#declarator}.
	 * @param ctx the parse tree
	 */
	void enterDeclarator(MiniCParser.DeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#declarator}.
	 * @param ctx the parse tree
	 */
	void exitDeclarator(MiniCParser.DeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeSpecifier(MiniCParser.TypeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeSpecifier(MiniCParser.TypeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#funcDef}.
	 * @param ctx the parse tree
	 */
	void enterFuncDef(MiniCParser.FuncDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#funcDef}.
	 * @param ctx the parse tree
	 */
	void exitFuncDef(MiniCParser.FuncDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#params}.
	 * @param ctx the parse tree
	 */
	void enterParams(MiniCParser.ParamsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#params}.
	 * @param ctx the parse tree
	 */
	void exitParams(MiniCParser.ParamsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(MiniCParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(MiniCParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#compoundStmt}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStmt(MiniCParser.CompoundStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#compoundStmt}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStmt(MiniCParser.CompoundStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(MiniCParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(MiniCParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#ifStmt}.
	 * @param ctx the parse tree
	 */
	void enterIfStmt(MiniCParser.IfStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#ifStmt}.
	 * @param ctx the parse tree
	 */
	void exitIfStmt(MiniCParser.IfStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#whileStmt}.
	 * @param ctx the parse tree
	 */
	void enterWhileStmt(MiniCParser.WhileStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#whileStmt}.
	 * @param ctx the parse tree
	 */
	void exitWhileStmt(MiniCParser.WhileStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#forStmt}.
	 * @param ctx the parse tree
	 */
	void enterForStmt(MiniCParser.ForStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#forStmt}.
	 * @param ctx the parse tree
	 */
	void exitForStmt(MiniCParser.ForStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#doWhileStmt}.
	 * @param ctx the parse tree
	 */
	void enterDoWhileStmt(MiniCParser.DoWhileStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#doWhileStmt}.
	 * @param ctx the parse tree
	 */
	void exitDoWhileStmt(MiniCParser.DoWhileStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#returnStmt}.
	 * @param ctx the parse tree
	 */
	void enterReturnStmt(MiniCParser.ReturnStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#returnStmt}.
	 * @param ctx the parse tree
	 */
	void exitReturnStmt(MiniCParser.ReturnStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#exprStmt}.
	 * @param ctx the parse tree
	 */
	void enterExprStmt(MiniCParser.ExprStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#exprStmt}.
	 * @param ctx the parse tree
	 */
	void exitExprStmt(MiniCParser.ExprStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(MiniCParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(MiniCParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CharLiteral}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCharLiteral(MiniCParser.CharLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CharLiteral}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCharLiteral(MiniCParser.CharLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringLiteralExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteralExpr(MiniCParser.StringLiteralExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringLiteralExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteralExpr(MiniCParser.StringLiteralExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RelationalExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpr(MiniCParser.RelationalExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RelationalExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpr(MiniCParser.RelationalExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(MiniCParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(MiniCParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(MiniCParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(MiniCParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AssignExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAssignExpr(MiniCParser.AssignExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AssignExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAssignExpr(MiniCParser.AssignExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FalseLiteral}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFalseLiteral(MiniCParser.FalseLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FalseLiteral}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFalseLiteral(MiniCParser.FalseLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MultiplicativeExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MultiplicativeExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EqualityExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpr(MiniCParser.EqualityExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EqualityExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpr(MiniCParser.EqualityExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TrueLiteral}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterTrueLiteral(MiniCParser.TrueLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TrueLiteral}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitTrueLiteral(MiniCParser.TrueLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AdditiveExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpr(MiniCParser.AdditiveExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AdditiveExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpr(MiniCParser.AdditiveExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LvalueExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLvalueExpr(MiniCParser.LvalueExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LvalueExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLvalueExpr(MiniCParser.LvalueExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CallExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCallExpr(MiniCParser.CallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CallExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCallExpr(MiniCParser.CallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IntLiteral}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIntLiteral(MiniCParser.IntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IntLiteral}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIntLiteral(MiniCParser.IntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(MiniCParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(MiniCParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void enterLvalue(MiniCParser.LvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void exitLvalue(MiniCParser.LvalueContext ctx);
}