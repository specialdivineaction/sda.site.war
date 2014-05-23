package edu.tamu.tcat.oss.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This generic abstract class is used for obtaining full generics type information
 * by sub-classing; it must be converted to {@link ResolvedType} implementation
 * (implemented by <code>JavaType</code> from "databind" bundle) to be used.
 * Class is based on ideas from
 * <a href="http://gafter.blogspot.com/2006/12/super-type-tokens.html"
 * >http://gafter.blogspot.com/2006/12/super-type-tokens.html</a>,
 * Additional idea (from a suggestion made in comments of the article)
 * is to require bogus implementation of <code>Comparable</code>
 * (any such generic interface would do, as long as it forces a method
 * with generic type to be implemented).
 * to ensure that a Type argument is indeed given.
 *<p>
 * Usage is by sub-classing: here is one way to instantiate reference
 * to generic type <code>List&lt;Integer></code>:
 *<pre>
 *  TypeReference ref = new TypeReference&lt;List&lt;Integer>>() { };
 *</pre>
 * which can be passed to methods that accept TypeReference, or resolved
 * using <code>TypeFactory</code> to obtain {@link ResolvedType}.
 */
public class JsonTypeReference<T> implements Comparable<JsonTypeReference<T>>
{
    protected final Type _type;
    
    protected JsonTypeReference()
    {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) 
        { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }

        _type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() { return _type; }
    
    /**
     * The only reason we define this method (and require implementation
     * of <code>Comparable</code>) is to prevent constructing a
     * reference without type information.
     */
    @Override
    public int compareTo(JsonTypeReference<T> o) { return 0; }
    // just need an implementation, not a good one... hence ^^^
}

