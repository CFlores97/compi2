grammar MiniC;

@header {
package org.example.antlr;
}

// ═══════════════════════════════════
//  REGLAS SINTÁCTICAS
// ═══════════════════════════════════

program         : (declaration | funcDef)* EOF ;

declaration     : typeSpecifier declaratorList ';' ;
declaratorList  : declarator (',' declarator)* ;
declarator      : Identifier ('[' IntegerConst ']')*
                | '*' Identifier ;

typeSpecifier   : 'int' | 'char' | 'bool' | 'void' | 'string' ;

funcDef         : typeSpecifier Identifier '(' params? ')' compoundStmt ;
params          : param (',' param)* ;
param           : typeSpecifier declarator ;

compoundStmt    : '{' (declaration | statement)* '}' ;

statement       : compoundStmt
                | ifStmt
                | whileStmt
                | forStmt
                | doWhileStmt
                | returnStmt
                | exprStmt ;

ifStmt          : 'if' '(' expr ')' statement ('else' statement)? ;
whileStmt       : 'while' '(' expr ')' statement ;
forStmt         : 'for' '(' exprStmt expr? ';' expr? ')' statement ;
doWhileStmt     : 'do' statement 'while' '(' expr ')' ';' ;
returnStmt      : 'return' expr? ';' ;
exprStmt        : expr? ';' ;

expr            : '(' expr ')'                                      # ParenExpr
                | ('!' | '-' | '*' | '&') expr                     # UnaryExpr
                | expr ('*' | '/' | '%') expr                      # MultiplicativeExpr
                | expr ('+' | '-') expr                            # AdditiveExpr
                | expr ('<' | '>' | '<=' | '>=') expr              # RelationalExpr
                | expr ('==' | '!=') expr                          # EqualityExpr
                | expr '&&' expr                                    # AndExpr
                | expr '||' expr                                    # OrExpr
                | lvalue '=' expr                                   # AssignExpr
                | Identifier '(' (expr (',' expr)*)? ')'           # CallExpr
                | lvalue                                            # LvalueExpr
                | IntegerConst                                      # IntLiteral
                | CharConst                                         # CharLiteral
                | StringLiteral                                     # StringLiteralExpr
                | 'true'                                            # TrueLiteral
                | 'false'                                           # FalseLiteral
                ;

lvalue          : Identifier ('[' expr ']')* ;

// ═══════════════════════════════════
//  TOKENS (LÉXICO)
// ═══════════════════════════════════

Identifier    : [A-Za-z_][A-Za-z0-9_]* ;
IntegerConst  : [0-9]+ ;
CharConst     : '\'' . '\'' ;
StringLiteral : '"' (~["\\\r\n])* '"' ;

WS            : [ \t\r\n]+         -> skip ;
LINE_COMMENT  : '//' ~[\r\n]*      -> skip ;
BLOCK_COMMENT : '/*' .*? '*/'      -> skip ;