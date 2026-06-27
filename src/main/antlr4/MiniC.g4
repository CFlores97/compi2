grammar MiniC;

@header {
package org.example.antlr;
}

// ============================================================
// Parser
// ============================================================

program
    : (declaration | funcDef)* EOF
    ;

declaration
    : typeSpecifier initDeclaratorList ';'
    ;

initDeclaratorList
    : initDeclarator (',' initDeclarator)*
    ;

initDeclarator
    : declarator ('=' expr)?
    ;

// Supports: x, *p, **pp, a[10], m[10][5]
declarator
    : '*'* Identifier ('[' IntegerConst ']')*
    ;

typeSpecifier
    : 'int'
    | 'char'
    | 'bool'
    | 'void'
    | 'string'
    ;

funcDef
    : typeSpecifier Identifier '(' params? ')' compoundStmt
    ;

params
    : param (',' param)*
    ;

param
    : typeSpecifier declarator
    ;

compoundStmt
    : '{' (declaration | statement)* '}'
    ;

statement
    : compoundStmt
    | ifStmt
    | whileStmt
    | forStmt
    | doWhileStmt
    | assignStmt
    | returnStmt
    | exprStmt
    ;

ifStmt
    : 'if' '(' expr ')' statement ('else' statement)?
    ;

whileStmt
    : 'while' '(' expr ')' statement
    ;

// The initializer and update may be an assignment or an expression.
forStmt
    : 'for' '(' forInit ';' expr? ';' forStep ')' statement
    ;

forInit
    : assignNoSemi
    | expr?
    ;

forStep
    : assignNoSemi
    | expr?
    ;

doWhileStmt
    : 'do' statement 'while' '(' expr ')' ';'
    ;

assignStmt
    : lvalue '=' expr ';'
    ;

assignNoSemi
    : lvalue '=' expr
    ;

returnStmt
    : 'return' expr? ';'
    ;

exprStmt
    : expr? ';'
    ;

expr
    : '(' expr ')'                                      # ParenExpr
    | ('!' | '-' | '*' | '&') expr                     # UnaryExpr
    | expr ('*' | '/' | '%') expr                      # MultiplicativeExpr
    | expr ('+' | '-') expr                            # AdditiveExpr
    | expr ('<' | '>' | '<=' | '>=') expr              # RelationalExpr
    | expr ('==' | '!=') expr                          # EqualityExpr
    | expr '&&' expr                                   # AndExpr
    | expr '||' expr                                   # OrExpr
    | Identifier '(' (expr (',' expr)*)? ')'           # CallExpr
    | lvalue                                            # LvalueExpr
    | IntegerConst                                      # IntLiteral
    | CharConst                                         # CharLiteral
    | StringLiteral                                     # StringLiteralExpr
    | 'true'                                            # TrueLiteral
    | 'false'                                           # FalseLiteral
    ;

// A dereference is valid on the left side: *p = 5.
lvalue
    : Identifier ('[' expr ']')*
    | '*' lvalue
    ;

// ============================================================
// Lexer
// ============================================================

Identifier
    : [A-Za-z_] [A-Za-z0-9_]*
    ;

IntegerConst
    : [0-9]+
    ;

CharConst
    : '\'' ( '\\' . | ~['\\\r\n] ) '\''
    ;

StringLiteral
    : '"' ( '\\' . | ~["\\\r\n] )* '"'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

