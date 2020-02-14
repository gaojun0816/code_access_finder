/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.utils.sootarray;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.Stmt;

/**
 * @author jun.gao
 *
 */
public class SootArrayElement {
  private SootMethod container;
  private Stmt stmt;
  private Value base, index, content;
  private SootArray theArray;
  
  public SootArrayElement(Stmt elementAssignStmt, SootMethod container) {
    this.stmt = elementAssignStmt;
    this.container = container;
    attributeSetup();
    SootArray sa = SootArray.tracker.get(base);
    if (sa == null) {
      sa = new SootArray(this);
    } else {
      sa.addElement(this);
    }
    theArray = sa;
  }
  
  private void attributeSetup() {
    AssignStmt astmt = (AssignStmt) stmt;
    ArrayRef ar = (ArrayRef) astmt.getLeftOp();
    base = ar.getBase();
    index = ar.getIndex();
    content = astmt.getRightOp();
  }
  
  /**
   * Return the method from which the array assignment statement is found.
   * 
   * @return the SootMethod instance contains the array assignment statement.
   */
  public SootMethod getContainer() {
    return container;
  }
  
  /**
   * Return the array assignment statement of this array element.
   * 
   * @return the array assignment statement.
   */
  public Stmt getStmt() {
    return stmt;
  }
  
  /**
   * Return the base variable of the array as a Soot Value instance.
   * e.g., with Soot array element $r1[0], the base will be $r1.
   * 
   * @return the base variable of the array as a Soot Value instance.
   */
  public Value getBase() {
    return base;
  }
  
  /**
   * Return the original index variable which is a Soot Value instance and obtained directly from the array assigning
   * statement. If the digital value of the index is desired, please refer to {@link #getConstantIndex()}.
   * 
   * @return 
   */
  public Value getIndex() {
    return index;
  }
  
  /**
   * Return all possible digits of the index of the array element if it can be recognized as a constant somehow in the code.
   * Otherwise, null will be returned.
   * 
   * @return a list of possible digital values of the index or null if none can be found.
   */
  public List<Integer> getConstantIndex() {
    List<Integer> index = null;
    if (this.index instanceof Constant) {
      index = new ArrayList<>();
      index.add(Integer.parseInt(this.index.toString()));
    } else {
      // TODO the constant propagation for further analysis.
    }
    return index;
  }
  
  /**
   * Return the Soot Value instance assigned to this array element, which is obtained directly from the array assigning
   * statement. If the concrete value of the content is in desire, please refer to {@link #getConstantContent()}.
   * 
   * @return Soot Value instance assigned to the array element.
   */
  public Value getContent() {
    return content;
  }
  
  /**
   * Return all possible constant values of the content assigned to the array element if it can be deduced from the code.
   * Otherwise, null will be returned.
   * 
   * @return a list of possible constant content values or null if none can be found.
   */
  public List<String> getConstantContent() {
    List<String> content = null;
    if (this.content instanceof Constant) {
      content = new ArrayList<>();
      content.add(this.content.toString());
    } else {
      // TODO the constant propagation for further analysis.
    }
    return content;
  }
  
  /**
   * Return the SootArray instance to which the array element belongs.
   * 
   * @return
   */
  public SootArray getTheArray() {
    return theArray;
  }
  
  /**
   * Output current string value. Cooperated with SootArray to output the whole array.
   * If you need to show the detailed element content, refer to {@link #toDetailedString()}.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    List<String> content = getConstantContent();
    if (content == null) {
      sb.append("unknow");
    } else if (content.size() == 1) {
      sb.append(content.get(0));
    } else {
      sb.append("[");
      for (String c : content) {
        sb.append(c);
        sb.append(", ");
      }
      sb.append("]");
    }
    return sb.toString();
  }
  
  /**
   * Return a string with detailed information of the array element.
   * It can be used to obtain detailed information for debugging etc.
   * 
   * @return
   */
  public String toDetailedString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Assigned in: ");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n");
    sb.append("Statement: ");
    sb.append(stmt);
    sb.append("\n");
    sb.append("Possible indexes constant value: ");
    List<Integer> index = getConstantIndex();
    if (index == null) {
      sb.append("unknow");
    } else {
      for (Integer i : index) {
        sb.append(i);
        sb.append(",");
      }
    }
    sb.append("\n");
    sb.append("Possible contents constant value: ");
    sb.append(toString());
    return sb.toString();
  }
  
  /**
   * Check if the statement is to assign value to an array reference.
   * 
   * @param stmt the statement to check.
   * @return 
   */
  public static Boolean isSootArrayElementAssignment(Stmt stmt) {
    Boolean itis = false;
    if (stmt.containsArrayRef()) {
      if (stmt instanceof AssignStmt) {
        Value left = ((AssignStmt) stmt).getLeftOp();
        if (left instanceof ArrayRef) {
          itis = true;
        }
      }
    }
    return itis;
  }
}
