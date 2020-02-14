/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.utils.sootarray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.SootMethod;
import soot.Type;
import soot.ArrayType;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.NewArrayExpr;
import soot.jimple.Stmt;

/**
 * @author jun.gao
 *
 */
public class SootArray {
  /**
   * Global SootArray objects tracker. Use this to find all generated Soot arrays.
   */
  public static Map<Value, SootArray> tracker = new HashMap<>();

  private final int DEFAULTSIZE = 100;
  
  private SootMethod container = null;
  private Stmt stmt = null;
  private Value base, size = null;
  private Type arrayType, elementType;

  private List<SootArrayElement> elements;
  private SootArrayElement[] currentElements;
  
  public SootArray(SootArrayElement element) {
    base = element.getBase();
    arrayType = base.getType();
    elementType = ((ArrayType) arrayType).baseType;
    initElements();
    addElement(element);
    tracker.put(this.base, this);
  }
  
  public SootArray(Stmt newArrayStmt, SootMethod newStmtContainer) {
    this.stmt = newArrayStmt;
    this.container = newStmtContainer;
    attributeSetup();
    initElements();
    tracker.put(this.base, this);
  }
  
  private void attributeSetup() {
    AssignStmt astmt = (AssignStmt) stmt;
    base = astmt.getLeftOp();
    NewArrayExpr nae = (NewArrayExpr) astmt.getRightOp();
    size = nae.getSize();
    arrayType = nae.getType();
    elementType = nae.getBaseType();
  }
  
  private void initElements() {
    List<Integer> size = getConstantSize();
    elements = new ArrayList<>();
    if (size == null) {
      currentElements = new SootArrayElement[DEFAULTSIZE];
    } else {
      Integer maxSize = Collections.max(size);
      currentElements = new SootArrayElement[maxSize];
    }
  }
  
  /**
   * Return the SootMethod instance from which the new array statement is found.
   * Note: the returned value can be null in the situation that the new array statement did not spotted and this instance
   * is generated based on the generation of one of the array element.
   * 
   * @return
   */
  public SootMethod getContainer() {
    return container;
  }
  
  /**
   * Return the new array statement which instantiated the array variable.
   * Note: the returned value can be null in the situation that the new array statement did not spotted and this instance
   * is generated based on the generation of one of the array element.
   * 
   * @return
   */
  public Stmt getStmt() {
    return stmt;
  }
  
  /**
   * Return the base soot variable of the array.
   * 
   * @return
   */
  public Value getBase() {
    return base;
  }
  
  /**
   * Return the type of the array.
   * 
   * @return
   */
  public Type getArrayType() {
    return arrayType;
  }
  
  /**
   * Return the type of the element of the array.
   * 
   * @return
   */
  public Type getElementType() {
    return elementType;
  }
  
  /**
   * Return the original size variable which is a Soot Value instance and obtained directly from the array assigning
   * statement. If the digital value of the size is desired, please refer to {@link #getConstantSize()}.
   * Note: the returned value can be null in the situation that the new array statement did not spotted and this instance
   * is generated based on the generation of one of the array element.
   * 
   * @return
   */
  public Value getSize() {
    return size;
  }
  
  /**
   * Add the new element of the array. It will update the current array state and also record the used element.
   * 
   * @param el
   */
  public void addElement(SootArrayElement el) {
    List<Integer> index = el.getConstantIndex();
    elements.add(el);
    if (index != null) {
      extendSize(index);
      currentElements[index.get(0)] = el;
    }
  }
  
  /**
   * Extend the current elements length according to the possible maximum index numbers.
   * This method can always be called, since it will judge whether it is needed to be extended.
   * 
   * @param indexes list of possible indexes.
   */
  private void extendSize(List<Integer> indexes) {
    int maxSize = Collections.max(indexes) + 1;
    if (maxSize > currentElements.length) {
      currentElements = Arrays.copyOf(currentElements, maxSize);
    }
  }
  
  /**
   * Return all elements with all their possible SootArrayElement instances. 
   * Because of branches and re-assignments, on array element could be assigned with
   * different values. This method will return all these possible values in the order of analysis.
   * 
   * @return
   */
  public List<SootArrayElement> getElements() {
    return elements;
  }

  /**
   * Return the array with its current elements. As the analysis goes on, the returned value could be different because of
   * new added or updated elements.
   * Note: it only returns 1 possible result at current state. e.g., latest operation try to assign value "a" to element
   * at possible indexes of 0 and 3, the result will only show this value at index either 0 or 3 based on the analysis
   * order. But you need to be aware of the existence of other possibilities.
   * 
   * @return 1 possible combination of array elements at current state.
   */
  public SootArrayElement[] getCurrentElements() {
    return currentElements;
  }
  
  /**
   * Return all possible digits of the size of the array if it can be recognized as a constant somehow in the code.
   * Otherwise, null will be returned.
   * 
   * @return a list of possible digital values of the size or null if none can be found.
   */
  public List<Integer> getConstantSize() {
    List<Integer> size = null;
    if (this.size != null) {
      if (this.size instanceof Constant) {
        size = new ArrayList<>();
        size.add(Integer.parseInt(this.size.toString()));
      } else {
        // TODO the constant propagation for further analysis.
      }
    }
    return size;
  }
  
  /**
   * Showing 1 possible known combination of constant values at current state.
   * The value could be different as the analysis goes on.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (SootArrayElement sae : getCurrentElements()) {
      if (sae == null) {
        sb.append("unknow");
      } else {
        sb.append(sae);
      }
      sb.append(", ");
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Generate arrays according to the statement provided. There are main 2 kind of statement will be used.
   *   1. statement to new an array and assign the new array to a variable (e.g., $r23 = newarray (java.lang.Object)[2]). 
   *   2. statement to assign content to a certain array element (e.g., $r23[0] = 1).
   * 
   * @param stmt the statement to be checked and used for array generation.
   * @param container the Soot method contains the statement.
   * @return if it is an array relevant statement, the generated SootArray instance will be returned. Otherwise, return null.
   */
  public static SootArray genSootArray(Stmt stmt, SootMethod container) {
    SootArray sa = null;
    if (isNewArrayStmt(stmt)) {
      sa = new SootArray(stmt, container);
    } else if (SootArrayElement.isSootArrayElementAssignment(stmt)) {
        SootArrayElement ae = new SootArrayElement(stmt, container);
        sa = ae.getTheArray();
    }
    return sa;
  }
  
  /**
   * Check if a statement is to new an array and assign the new array to a variable
   * (e.g., $r23 = newarray (java.lang.Object)[2]).
   * 
   * @param stmt the statement to be judged.
   * @return
   */
  public static Boolean isNewArrayStmt(Stmt stmt) {
    Boolean itis = false;
    if (stmt instanceof AssignStmt) {
      AssignStmt astmt = (AssignStmt) stmt;
      if (astmt.getRightOp() instanceof NewArrayExpr) {
        itis = true;
      }
    }
    return itis;
  }
}
