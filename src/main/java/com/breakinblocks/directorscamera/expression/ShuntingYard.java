package com.breakinblocks.directorscamera.expression;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class ShuntingYard {
    private static final int NEGATE_PRIORITY = 4;

    private ShuntingYard() {
    }

    public enum TokenType {
        OPERATOR,
        CLOSING_BRACKET,
        OPENING_BRACKET,
        NUMBER,
        FUNCTION,
        VARIABLE,
        COMMA,
        NEGATE
    }

    public record Token(String text, TokenType type) {
    }

    public static RpnExpression parse(String expression) {
        List<Token> tokens = tokenize(expression);
        List<SyNode> output = new ArrayList<>();
        Deque<Token> stack = new ArrayDeque<>();
        Token previous = null;
        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER -> output.add(new StaticValue(Float.parseFloat(token.text)));
                case VARIABLE -> output.add(new NamedVariable(token.text));
                case OPERATOR -> {
                    if (token.text.equals("-") && isUnaryPosition(previous)) {
                        stack.push(new Token("-", TokenType.NEGATE));
                        break;
                    }
                    if (token.text.equals("+") && isUnaryPosition(previous)) {
                        break;
                    }
                    int priority = FunctionRegistry.OPERATOR_PRIORITY.get(token.text);
                    while (!stack.isEmpty()) {
                        Token top = stack.peek();
                        if (top.type == TokenType.NEGATE || (top.type == TokenType.OPERATOR && FunctionRegistry.OPERATOR_PRIORITY.get(top.text) >= priority)) {
                            output.add(nodeFor(stack.pop()));
                        } else {
                            break;
                        }
                    }
                    stack.push(token);
                }
                case OPENING_BRACKET, FUNCTION -> stack.push(token);
                case COMMA -> {
                    while (!stack.isEmpty() && stack.peek().type != TokenType.FUNCTION && stack.peek().type != TokenType.OPENING_BRACKET) {
                        output.add(nodeFor(stack.pop()));
                    }
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Misplaced comma in expression: " + expression);
                    }
                }
                case CLOSING_BRACKET -> {
                    boolean closed = false;
                    while (!stack.isEmpty()) {
                        Token top = stack.pop();
                        if (top.type == TokenType.OPENING_BRACKET) {
                            closed = true;
                            break;
                        }
                        if (top.type == TokenType.FUNCTION) {
                            output.add(functionFor(top.text));
                            closed = true;
                            break;
                        }
                        output.add(nodeFor(top));
                    }
                    if (!closed) {
                        throw new IllegalArgumentException("Unbalanced closing bracket in expression: " + expression);
                    }
                }
                default -> throw new IllegalArgumentException("Unexpected token " + token.text);
            }
            previous = token;
        }
        while (!stack.isEmpty()) {
            Token top = stack.pop();
            if (top.type == TokenType.OPENING_BRACKET || top.type == TokenType.FUNCTION) {
                throw new IllegalArgumentException("Unclosed bracket in expression: " + expression);
            }
            output.add(nodeFor(top));
        }
        return new RpnExpression(output, expression);
    }

    private static boolean isUnaryPosition(Token previous) {
        return previous == null
            || previous.type == TokenType.OPERATOR
            || previous.type == TokenType.OPENING_BRACKET
            || previous.type == TokenType.FUNCTION
            || previous.type == TokenType.COMMA
            || previous.type == TokenType.NEGATE;
    }

    private static SyNode nodeFor(Token token) {
        if (token.type == TokenType.NEGATE) {
            return FunctionRegistry.NEGATE;
        }
        SyFunction operator = FunctionRegistry.OPERATORS.get(token.text);
        if (operator == null) {
            throw new IllegalArgumentException("Unknown operator: " + token.text);
        }
        return operator;
    }

    private static SyNode functionFor(String name) {
        SyFunction function = FunctionRegistry.getFunction(name);
        if (function == null) {
            throw new IllegalArgumentException("Unknown function: " + name);
        }
        return function;
    }

    public static List<Token> tokenize(String expression) {
        String text = expression.replace(" ", "");
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (FunctionRegistry.isOperator(c)) {
                tokens.add(new Token(String.valueOf(c), TokenType.OPERATOR));
                i++;
            } else if (c == '(') {
                tokens.add(new Token("(", TokenType.OPENING_BRACKET));
                i++;
            } else if (c == ')') {
                tokens.add(new Token(")", TokenType.CLOSING_BRACKET));
                i++;
            } else if (c == ',') {
                tokens.add(new Token(",", TokenType.COMMA));
                i++;
            } else if (Character.isLetter(c)) {
                int start = i;
                while (i < text.length() && (Character.isLetterOrDigit(text.charAt(i)) || text.charAt(i) == '.' || text.charAt(i) == '_')) {
                    i++;
                }
                String name = text.substring(start, i);
                if (i < text.length() && text.charAt(i) == '(') {
                    tokens.add(new Token(name, TokenType.FUNCTION));
                    i++;
                } else {
                    tokens.add(new Token(name, TokenType.VARIABLE));
                }
            } else if (Character.isDigit(c) || c == '.') {
                int start = i;
                while (i < text.length() && (Character.isDigit(text.charAt(i)) || text.charAt(i) == '.')) {
                    i++;
                }
                String number = text.substring(start, i);
                try {
                    Float.parseFloat(number);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Bad number '" + number + "' in expression: " + expression);
                }
                tokens.add(new Token(number, TokenType.NUMBER));
            } else {
                throw new IllegalArgumentException("Unexpected character '" + c + "' in expression: " + expression);
            }
        }
        return tokens;
    }
}
