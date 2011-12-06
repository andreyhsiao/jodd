// Copyright (c) 2003-2012, Jodd Team (jodd.org). All Rights Reserved.

package jodd.asm;

import jodd.mutable.MutableInteger;
import jodd.util.ClassLoaderUtil;

/**
 * Generic ASM utils.
 */
public class AsmUtil {

	private static final String INVALID_BASE_TYPE = "Invalid base type: ";
	private static final String INVALID_TYPE_DESCRIPTION = "Invalid type description: ";

	// ---------------------------------------------------------------- class relates

	/**
	 * Converts bytecode-like description to java class name that can be loaded
	 * with a classloader. Uses less-known feature of class loaders for loading
	 * array classes. For base types returns the one-letter string that can be used
	 * with {@link #loadBaseTypeClass(String)}.
	 *
	 * @see #typedescToSignature(String, jodd.mutable.MutableInteger)
	 */
	public static String typedesc2ClassName(String desc) {
		String className = desc;
		switch (desc.charAt(0)) {
			case 'B':
			case 'C':
			case 'D':
			case 'F':
			case 'I':
			case 'J':
			case 'S':
			case 'Z':
			case 'V':
				if (desc.length() != 1) {
					throw new IllegalArgumentException(INVALID_BASE_TYPE + desc);
				}
				break;
			case 'L': className = className.substring(1, className.length() - 1);
			case '[':
				// uses less-known feature of class loaders for loading array types
				// using bytecode-like signatures.
				className = className.replace('/', '.');
				break;
			default: throw new IllegalArgumentException(INVALID_TYPE_DESCRIPTION + desc);
		}

		return className;
	}

	/**
	 * Converts type reference to java-name.
	 */
	public static String typeref2Name(String desc) {
		if (desc.charAt(0) != AsmConst.TYPE_REFERENCE) {
			throw new IllegalArgumentException(INVALID_TYPE_DESCRIPTION + desc);
		}
		String name = desc.substring(1, desc.length() - 1);
		return name.replace('/', '.');
	}

	/**
	 * Loads class defined by bytecode-like descriptor,
	 */
	public static Class loadClass(String desc) throws ClassNotFoundException {
		String className = typedesc2ClassName(desc);
		if (className.length() == 1) {
			return loadBaseTypeClass(className);
		}
		return ClassLoaderUtil.loadClass(className);
	}

	/**
	 * Loads base class type.
	 */
	public static Class loadBaseTypeClass(String desc) throws ClassNotFoundException {
		if (desc.length() != 1) {
			throw new ClassNotFoundException(INVALID_BASE_TYPE + desc);
		}
		switch (desc.charAt(0)) {
			case 'B': return byte.class;
			case 'C': return char.class;
			case 'D': return double.class;
			case 'F': return float.class;
			case 'I': return int.class;
			case 'J': return long.class;
			case 'S': return short.class;
			case 'Z': return boolean.class;
			case 'V': return void.class;
			default: throw new ClassNotFoundException(INVALID_BASE_TYPE + desc);
		}
	}

	// ---------------------------------------------------------------- description

	/**
	 * Returns java-like signature of a bytecode-like description.
	 * @see #typedescToSignature(String, jodd.mutable.MutableInteger)
	 */
	public static String typedescToSignature(String desc) {
		return typedescToSignature(desc, new MutableInteger());
	}

	/**
	 * Returns java-like signature of a bytecode-like description.
	 * Only first description is parsed.
	 *
	 * The field signature represents the value of an argument to a function or
	 * the value of a variable. It is a series of bytes generated by the
	 * following grammar:
	 *
	 * <PRE>
	 * <field_signature> ::= <field_type>
	 * <field_type>      ::= <base_type>|<object_type>|<array_type>
	 * <base_type>       ::= B|C|D|F|I|J|S|Z
	 * <object_type>     ::= L<fullclassname>;
	 * <array_type>      ::= [<field_type>
	 *
	 * The meaning of the base types is as follows:
	 * B byte signed byte
	 * C char character
	 * D double double precision IEEE float
	 * F float single precision IEEE float
	 * I int integer
	 * J long long integer
	 * L<fullclassname>; ... an object of the given class
	 * S short signed short
	 * Z boolean true or false
	 * [<field sig> ... array
	 * </PRE>
	 *
	 * This method converts this string into a Java type declaration such as
	 * <code>String[]</code>.
	 */
	public static String typedescToSignature(String desc, MutableInteger from) {
		int fromIndex = from.getValue();
		from.value++;	// default usage for most cases

		switch (desc.charAt(fromIndex)) {
			case 'B': return "byte";
			case 'C': return "char";
			case 'D': return "double";
			case 'F': return "float";
			case 'I': return "int";
			case 'J': return "long";
			case 'S': return "short";
			case 'Z': return "boolean";
			case 'V': return "void";

			case 'L':
				int index = desc.indexOf(';', fromIndex);
				if (index < 0) {
					throw new IllegalArgumentException(INVALID_TYPE_DESCRIPTION + desc);
				}
				from.setValue(index + 1);
				String str = desc.substring(fromIndex + 1, index);
				return str.replace('/', '.');

			case '[':
				StringBuilder brackets = new StringBuilder();
				int n = fromIndex;
				while (desc.charAt(n) == '[') {	// count opening brackets
					brackets.append("[]");
					n++;
				}
				from.value = n;
				String type = typedescToSignature(desc, from);	// the rest of the string denotes a `<field_type>'
				return type + brackets;

			default: throw new IllegalArgumentException(INVALID_TYPE_DESCRIPTION + desc);
		}
	}

}
