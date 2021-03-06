// This file is part of the RECODER library and protected by the LGPL.

package recoder;

/**
 * A semantic part of the software model. A model element is not necessarily
 * connected to a piece of syntax.
 * 
 * @see recoder.java.SourceElement
 */
public interface ModelElement {

    /**
     * Check consistency and admissibility of a construct, e.g. cardinality of
     * participants. Should <b>only</b> check syntactical correctness,
     *  <b>not</b> semantical correctness.<br>
     *  For checking, e.g., if an AST element of type <code>recoder.java.statement.If</code>
     *  has a boolean parameter as condition, 
     *  <code>recoder.service.SemanticsChecker</code>
     * 
     * @see recoder.service.SemanticsChecker
     * @exception ModelException
     */
    void validate() throws ModelException;
}