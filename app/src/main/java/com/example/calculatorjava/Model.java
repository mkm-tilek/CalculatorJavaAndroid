package com.example.calculatorjava;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Model {
    private MainActivity viewer;
    private ExpressionParser parser;
    private Boolean action;
    private Integer countScopes;
    private String temp;
    private String expression;
    private String log;

    private String numbers = "0123456789";

    public Model(MainActivity viewer) {
        this.viewer = viewer;
        this.parser = new ExpressionParser();
        expression = "";
        temp = "";
        log  = "";
        action = false;
        countScopes = 0;
    }

    public void doAction(String value) {

        if (numbers.contains(value)) {
            if (expression.length() > 0 && !parser.isOperator(expression.substring(expression.length() - 1))) {
                expression = "";
            }

            if (value.equals("0")) {
                if (temp.equals("") || temp.equals("0")) {
                    temp = "0";
                } else {
                    temp = temp + value;
                }
            } else {
                if (temp.equals("0")) {
                    temp = value;
                } else {
                    temp = temp + value;
                }
            }
            action = true;
        } else if (parser.isOperator(value)) {

            if (action || value.equals("√")) {
                if (value.equals("√")) {
                    if (temp.equals("0")) temp = "";
                    if (expression.equals("0")) expression = "";
                }

                if (expression.equals("")) {
                    expression = temp + " " + value;
                } else {
                    expression = expression + " " + temp + " " + value;
                }
                System.out.println(expression);
                temp = "";
                action = false;
            } else if (expression.length() > 0) {
                String operator = expression.substring(expression.length() - 1);

                if (parser.isOperator(operator)) {
                    expression = expression.substring(0, expression.length() - 1) + value;
                }
            }
        } else if (parser.isDelimiter(value)) {
            if (value.equals("(")){
                temp = temp + value;
                ++countScopes;
            } else if (value.equals(")") && (countScopes > 0)) {
                temp = temp + value;
                --countScopes;
            }
        } else if (value.equals("=")) {
            System.out.println(expression.length());
            System.out.println(temp.length());
            String fullExpression = expression + " " + temp;
            if (expression.length() > 0 && temp.length() > 0) {
                fullExpression = fullExpression.replaceAll("\\s+","");
                if (fullExpression.contains("√")){
                    for (int i = 0; i < fullExpression.length() - 1; i++){
                        if (fullExpression.charAt(i) == '√' && i > 0){
                            String checkNum  = Character.toString(fullExpression.charAt(i - 1));
                            if (i != 0 && numbers.contains(checkNum)) {
                                String leftSide = fullExpression.substring(0, i);
                                String rightSide = fullExpression.substring(i);
                                fullExpression = leftSide + " * " + rightSide;
                            }
                        }
                    }
                }

                List<String> parsedExpression = parser.parse(fullExpression);

                if (parser.getFlag()) {
                    log = log + ((log.length() > 0) ? "\n\n\r" : "") + fullExpression + " = ";
                    for (String x : parsedExpression) System.out.print(x + " ");
                    temp = calculate(parsedExpression);

                    if (temp.equals("Infinity") || temp.equals("NaN")) {
                        temp = "0";
                        log = log + " Нельзя делить на 0";
                    } else {
                        log = log + " " + temp;
                        expression = temp;
                        temp = "";
                    }
                } else {
                    return;
                }
            } else {
                return;
            }

        } else if (value.equals("C")) {
            log = log + "\n\r";
            temp = "0";
            expression = "";
        } else if (value.equals("AC")){
            log  = "";
            temp = "0";
            expression = "";
        } else if (value.equals("Del")) {
            if (temp.length() > 0) {
                if (temp.length() == 1 || (temp.length() == 2 && temp.charAt(0) == '-')) {
                    action = false;
                    temp = "";
                } else {
                    temp = temp.substring(0, temp.length() - 1);
                }
            } else if (expression.length() > 0) {
                if (parser.isOperator(expression.substring(expression.length() - 1))) {
                    expression = expression.substring(0, expression.lastIndexOf(" "));
                    if (expression.contains(" ")) {
                        temp = expression.substring(expression.lastIndexOf(" ") + 1);
                        expression = expression.substring(0, expression.lastIndexOf(" "));
                        action = true;
                    } else {
                        temp = expression;
                        expression = "";
                    }
                }
            }
        } else if (value.equals(",")) {
            if (!temp.contains(".")) {
                action = true;
                if (temp.length() < 1) {
                    temp = "0.";
                } else {
                    temp = temp + ".";
                }
            }
        } else if (value.equals("+/-")) {
            if (temp.length() > 0) {
                if (temp.charAt(0) == '-') {
                    temp = temp.substring(1);
                } else {
                    temp = '-' + temp;
                }
            }
        }

        viewer.update(expression + " " + temp, log);
    }

    private String calculate(List<String> postfix) {
        Deque<Double> stack = new ArrayDeque<Double>();
        for (String x : postfix) {
            if (x.equals("+")) stack.push(stack.pop() + stack.pop());
            else if (x.equals("-")) {
                Double b = stack.pop(), a = stack.pop();
                stack.push(a - b);
            }
            else if (x.equals("*")) {
                stack.push(stack.pop() * stack.pop());
            }
            else if (x.equals("√")) stack.push(Math.sqrt(stack.pop()));
            else if (x.equals("^")) {
                Double exponent = stack.pop();
                Double base = stack.pop();
                stack.push(Math.pow(base, exponent));
            }
            else if (x.equals("/")) {
                Double b = stack.pop(), a = stack.pop();
                stack.push(a / b);
            }
            else if (x.equals("u-")) stack.push(-stack.pop());
            else stack.push(Double.valueOf(x));
        }

        return parser.isInteger(""+stack.pop());
    }

}