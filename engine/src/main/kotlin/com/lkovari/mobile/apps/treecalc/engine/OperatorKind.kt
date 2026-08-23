package com.lkovari.mobile.apps.treecalc.engine

enum class OperatorArity {
    UNARY,
    BINARY
}

enum class Associativity {
    LEFT,
    RIGHT
}

enum class OperatorKind(
    val displaySymbol: String,
    val treeLabel: String,
    val precedence: Int,
    val arity: OperatorArity,
    val associativity: Associativity = Associativity.LEFT
) {
    ADD("+", "+", 11, OperatorArity.BINARY),
    SUB("-", "-", 11, OperatorArity.BINARY),
    MUL("×", "×", 12, OperatorArity.BINARY),
    DIV("÷", "÷", 12, OperatorArity.BINARY),
    MOD("mod", "mod", 12, OperatorArity.BINARY),
    POW("^", "^", 99, OperatorArity.BINARY, Associativity.RIGHT),
    AND("and", "and", 7, OperatorArity.BINARY),
    OR("or", "or", 5, OperatorArity.BINARY),
    XOR("xor", "xor", 6, OperatorArity.BINARY),
    LSH("lsh", "lsh", 12, OperatorArity.BINARY),
    NOT("not", "not", 14, OperatorArity.UNARY),
    NEG("−", "neg", 99, OperatorArity.UNARY, Associativity.RIGHT),
    SQRT("√", "√", 99, OperatorArity.UNARY),
    REC("1/", "1/", 99, OperatorArity.UNARY),
    SIN("sin", "sin", 12, OperatorArity.UNARY),
    COS("cos", "cos", 12, OperatorArity.UNARY),
    TAN("tan", "tan", 12, OperatorArity.UNARY),
    LN("ln", "ln", 12, OperatorArity.UNARY),
    LOG("log", "log", 12, OperatorArity.UNARY),
    EXP("exp", "exp", 12, OperatorArity.UNARY),
    SQ("²", "²", 99, OperatorArity.UNARY),
    CUBE("³", "³", 99, OperatorArity.UNARY),
    FACT("!", "!", 99, OperatorArity.UNARY)
}
