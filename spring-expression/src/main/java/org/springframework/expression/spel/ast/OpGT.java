/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.expression.spel.ast;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.asm.MethodVisitor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.CodeFlow;
import org.springframework.expression.spel.ExpressionState;
import org.springframework.expression.spel.support.BooleanTypedValue;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.NumberUtils;
import org.springframework.util.ObjectUtils;

/**
 * Implements the greater-than operator.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @author Giovanni Dall'Oglio Risso
 * @since 3.0
 */
public class OpGT extends Operator {

	public OpGT(int pos, SpelNodeImpl... operands) {
		super(">", pos, operands);
		this.exitTypeDescriptor = "Z";
	}


	@Override
	public BooleanTypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		Object left = getLeftOperand().getValueInternal(state).getValue();
		Object right = getRightOperand().getValueInternal(state).getValue();

		this.leftActualDescriptor = CodeFlow.toDescriptorFromObject(left);
		this.rightActualDescriptor = CodeFlow.toDescriptorFromObject(right);

		if (left instanceof Number && right instanceof Number) {
			Number leftNumber = (Number) left;
			Number rightNumber = (Number) right;

			if (leftNumber instanceof BigDecimal || rightNumber instanceof BigDecimal) {
				BigDecimal leftBigDecimal = NumberUtils.convertNumberToTargetClass(leftNumber, BigDecimal.class);
				BigDecimal rightBigDecimal = NumberUtils.convertNumberToTargetClass(rightNumber, BigDecimal.class);
				return BooleanTypedValue.forValue(leftBigDecimal.compareTo(rightBigDecimal) > 0);
			}
			else if (leftNumber instanceof Double || rightNumber instanceof Double) {
				return BooleanTypedValue.forValue(leftNumber.doubleValue() > rightNumber.doubleValue());
			}
			else if (leftNumber instanceof Float || rightNumber instanceof Float) {
				return BooleanTypedValue.forValue(leftNumber.floatValue() > rightNumber.floatValue());
			}
			else if (leftNumber instanceof BigInteger || rightNumber instanceof BigInteger) {
				BigInteger leftBigInteger = NumberUtils.convertNumberToTargetClass(leftNumber, BigInteger.class);
				BigInteger rightBigInteger = NumberUtils.convertNumberToTargetClass(rightNumber, BigInteger.class);
				return BooleanTypedValue.forValue(leftBigInteger.compareTo(rightBigInteger) > 0);
			}
			else if (leftNumber instanceof Long || rightNumber instanceof Long) {
				return BooleanTypedValue.forValue(leftNumber.longValue() > rightNumber.longValue());
			}
			else if (leftNumber instanceof Integer || rightNumber instanceof Integer) {
				return BooleanTypedValue.forValue(leftNumber.intValue() > rightNumber.intValue());
			}
			else if (leftNumber instanceof Short || rightNumber instanceof Short) {
				return BooleanTypedValue.forValue(leftNumber.shortValue() > rightNumber.shortValue());
			}
			else if (leftNumber instanceof Byte || rightNumber instanceof Byte) {
				return BooleanTypedValue.forValue(leftNumber.byteValue() > rightNumber.byteValue());
			}
			else {
				// Unknown Number subtypes -> best guess is double comparison
				return BooleanTypedValue.forValue(leftNumber.doubleValue() > rightNumber.doubleValue());
			}
		}

		if (left instanceof CharSequence && right instanceof CharSequence) {
			left = left.toString();
			right = right.toString();
		}

		return BooleanTypedValue.forValue(state.getTypeComparator().compare(left, right) > 0);
	}

	@Override
	public boolean isCompilable() {
		return isCompilableOperatorUsingNumerics();
	}

	@Override
	public void generateCode(MethodVisitor mv, CodeFlow cf) {
		generateComparisonCode(mv, cf, IFLE, IF_ICMPLE);
	}


	/**
	 * Perform an equality check for the given operand values.
	 * <p>This method is not just used for reflective comparisons in subclasses
	 * but also from compiled expression code, which is why it needs to be
	 * declared as {@code public static} here.
	 * @param context the current evaluation context
	 * @param left the left-hand operand value
	 * @param right the right-hand operand value
	 */
	public static boolean equalityCheck(EvaluationContext context, @Nullable Object left, @Nullable Object right) {
		if (left instanceof Number && right instanceof Number) {
			Number leftNumber = (Number) left;
			Number rightNumber = (Number) right;
	
			if (leftNumber instanceof BigDecimal || rightNumber instanceof BigDecimal) {
				BigDecimal leftBigDecimal = NumberUtils.convertNumberToTargetClass(leftNumber, BigDecimal.class);
				BigDecimal rightBigDecimal = NumberUtils.convertNumberToTargetClass(rightNumber, BigDecimal.class);
				return (leftBigDecimal.compareTo(rightBigDecimal) == 0);
			}
			else if (leftNumber instanceof Double || rightNumber instanceof Double) {
				return (leftNumber.doubleValue() == rightNumber.doubleValue());
			}
			else if (leftNumber instanceof Float || rightNumber instanceof Float) {
				return (leftNumber.floatValue() == rightNumber.floatValue());
			}
			else if (leftNumber instanceof BigInteger || rightNumber instanceof BigInteger) {
				BigInteger leftBigInteger = NumberUtils.convertNumberToTargetClass(leftNumber, BigInteger.class);
				BigInteger rightBigInteger = NumberUtils.convertNumberToTargetClass(rightNumber, BigInteger.class);
				return (leftBigInteger.compareTo(rightBigInteger) == 0);
			}
			else if (leftNumber instanceof Long || rightNumber instanceof Long) {
				return (leftNumber.longValue() == rightNumber.longValue());
			}
			else if (leftNumber instanceof Integer || rightNumber instanceof Integer) {
				return (leftNumber.intValue() == rightNumber.intValue());
			}
			else if (leftNumber instanceof Short || rightNumber instanceof Short) {
				return (leftNumber.shortValue() == rightNumber.shortValue());
			}
			else if (leftNumber instanceof Byte || rightNumber instanceof Byte) {
				return (leftNumber.byteValue() == rightNumber.byteValue());
			}
			else {
				// Unknown Number subtypes -> best guess is double comparison
				return (leftNumber.doubleValue() == rightNumber.doubleValue());
			}
		}
	
		if (left instanceof CharSequence && right instanceof CharSequence) {
			return left.toString().equals(right.toString());
		}
	
		if (left instanceof Boolean && right instanceof Boolean) {
			return left.equals(right);
		}
	
		if (ObjectUtils.nullSafeEquals(left, right)) {
			return true;
		}
	
		if (left instanceof Comparable && right instanceof Comparable) {
			Class<?> ancestor = ClassUtils.determineCommonAncestor(left.getClass(), right.getClass());
			if (ancestor != null && Comparable.class.isAssignableFrom(ancestor)) {
				return (context.getTypeComparator().compare(left, right) == 0);
			}
		}
	
		return false;
	}

}
