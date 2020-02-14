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
import lu.uni.snt.jungao.codetheftfinder.utils.sootarray.SootArray;
import lu.uni.snt.jungao.codetheftfinder.utils.sootarray.SootArrayElement;

/**
 * @author jun.gao
 *
 */
public class LoadedMethod extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();
  
  private static final String CLASS = "java.lang.Class";
  private static final String[] SIG = {"java.lang.reflect.Method getDeclaredMethod(java.lang.String,java.lang.Class[])",
      "java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])"};
  
  private Value methodNameRef, argsRef;
  
  public LoadedMethod(Stmt stmt, SootMethod container) {
    super(stmt, container);
    methodNameRef = getInvokeExprArg(0);
    argsRef = getInvokeExprArg(1); 
    tracker.add(this);
  }

  public List<String> getConstantMethodName() {
    List<String> name = null;
    if (methodNameRef instanceof Constant){
      name = new ArrayList<>();
      name.add(methodNameRef.toString().replace("\"", ""));
    } else {
      //TODO use constant propagation to further check
    }
    return name;
  }
  
  public List<Integer> getSizeOfArgs() {
    return SootArray.tracker.get(argsRef).getConstantSize();
  }
  
  public List<List<String>> getConstantMethodArgs() {
    List<List<String>> args = new ArrayList<>();
    SootArray sa = SootArray.tracker.get(argsRef);
    for ( SootArrayElement sae : sa.getCurrentElements()) {
      List<String> content = sae.getConstantContent();
      if (content == null) {
        // Further check if it is a class obtained by using reflection.
        // We check the source local variable with reflectedly-generated class reference.
        List<String> possibleNames = new ArrayList<>();
        Value rawContent = sae.getContent();
        for (LoadedComponent lcom : LoadedClass.tracker) {
          LoadedClass lc = (LoadedClass) lcom;
          if (lc.getTheRefs().contains(rawContent)) {
            List<String> classNames = lc.getConstantClassName();
            if (classNames != null) {
              possibleNames.addAll(classNames);
            }
          }
        }
        if (possibleNames.size() == 0) {
          args.add(null);
        } else {
          args.add(possibleNames);
        }
//        if (rawContent instanceof Local) {
//          Local l = (Local) rawContent;
//          List<String> possibleNames = new ArrayList<>();
//          for (LoadedClass lc : LoadedClass.tracker) {
//            // TODO equality comparison could be removed.
//            if (l == lc.getClassRef() || StaticUtils.isIntersected(l, lc.getClassRef())) {
//              List<String> classNames = lc.getConstantClassName();
//              if (classNames != null) {
//                possibleNames.addAll(classNames);
//              }
//            }
//          }
//          if (possibleNames.size() == 0) {
//            args.add(null);
//          } else {
//            args.add(possibleNames);
//          }
//        } else {
//          args.add(null);
//        }
      } else {
        args.add(content);
      }
    }
    return args;
  }
  
  public Value getMethodNameRef() {
    return methodNameRef;
  }
  
  public Value getMethodArgRef() {
    return argsRef;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Method -----------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Method name: ");
    List<String> name = getConstantMethodName();
    if (name != null) {
      sb.append(name.toString());
    } else {
      sb.append("unknown");
    }
    sb.append("\n\t");
    sb.append("Method args: ");
    List<List<String>> args = getConstantMethodArgs();
    if (args != null) {
      sb.append(args.toString());
//      for (List<String> arg : methodArgs) {
//        if (arg == null) {
//          sb.append("unknown, ");
//        } else{
//          sb.append(arg.toString());
//          sb.append(", ");
//        }
//      }
    } else {
      sb.append("unknown");
    }
    sb.append("\n\t");
    sb.append("Possible number of method args: ");
    List<Integer> size = getSizeOfArgs();
    if (size != null) {
      sb.append(size.toString());
    } else {
      sb.append("unknown");
    }
    sb.append("\n\t");
    sb.append("Method references: ");
    sb.append(theRefs.toString());
    sb.append("\n------------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isLoadingMethod(Stmt stmt) {
    Boolean itis = false;
    if (StaticUtils.isIt(stmt, CLASS, SIG[0]))
        itis = true;
    else if (StaticUtils.isIt(stmt, CLASS, SIG[1]))
      itis = true;
    return itis;
  }
}
