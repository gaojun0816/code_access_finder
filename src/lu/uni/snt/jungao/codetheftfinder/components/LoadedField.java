/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.components;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Stmt;

import lu.uni.snt.jungao.codetheftfinder.utils.StaticUtils;

/**
 * @author jun.gao
 *
 */
public class LoadedField extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();
  
  private static final String CLASS = "java.lang.Class";
  private static final String[] SIG = {"java.lang.reflect.Field getDeclaredField(java.lang.String)",
      "java.lang.reflect.Field getField(java.lang.String)"};
  
  private Value fieldNameRef;

  /**
   * @param stmt
   * @param container
   */
  public LoadedField(Stmt stmt, SootMethod container) {
    super(stmt, container);
    fieldNameRef = getInvokeExprArg(0);
    tracker.add(this);
  }
  
  public List<String> getConstantFieldName() {
    List<String> name = null;
    if (fieldNameRef instanceof Constant){
      name = new ArrayList<>();
      name.add(fieldNameRef.toString().replace("\"", ""));
    } else {
      //TODO use constant propagation to further check
    }
    return name;
  }
  
  public Value getFieldNameRef() {
    return fieldNameRef;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Field -----------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Field name: ");
    List<String> name = getConstantFieldName();
    if (name != null) {
      sb.append(name.toString());
    } else {
      sb.append("Unknown");
    }
    sb.append("\n\t");
    sb.append("Field references: ");
    sb.append(theRefs.toString());
    sb.append("\n-----------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isLoadingField(Stmt stmt) {
    Boolean itis = false;
    if (StaticUtils.isIt(stmt, CLASS, SIG[0]))
        itis = true;
    else if (StaticUtils.isIt(stmt, CLASS, SIG[1]))
      itis = true;
    return itis;
  }
}
