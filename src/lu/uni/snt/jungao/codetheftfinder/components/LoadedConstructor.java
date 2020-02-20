/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.components;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

import lu.uni.snt.jungao.codetheftfinder.utils.StaticUtils;
import lu.uni.snt.jungao.codetheftfinder.utils.sootarray.SootArray;
import lu.uni.snt.jungao.codetheftfinder.utils.sootarray.SootArrayElement;

/**
 * @author jun.gao
 *
 */
public class LoadedConstructor extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();
  
  private static final String CLASS = "java.lang.Class";
  private static final String[] SIG = {"java.lang.reflect.Constructor getConstructor(java.lang.Class[])",
      "java.lang.reflect.Constructor getDeclaredConstructor(java.lang.Class[])"};

  private Value argRef;

  /**
   * @param stmt
   * @param container
   */
  public LoadedConstructor(Stmt stmt, SootMethod container) {
    super(stmt, container);
    argRef = getInvokeExprArg(0);
    tracker.add(this);
  }

  public List<Integer> getSizeOfArgs() {
    return SootArray.tracker.get(argRef).getConstantSize();
  }

  public List<List<String>> getConstantArgs() {
    List<List<String>> args = new ArrayList<>();
    SootArray sa = SootArray.tracker.get(argRef);
    for ( SootArrayElement sae : sa.getCurrentElements()) {
      List<String> content = sae.getConstantContent();
      if (content == null) {
        // Further check if it is a class obtained by using reflection.
        // We check the source local variable with reflectedly-generated class reference.
        Value rawContent = sae.getContent();
        List<String> possibleNames = new ArrayList<>();
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
  
  public Value getArgRef() {
    return argRef;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Constructor Invocation -----------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Constructor args: ");
    List<List<String>> args = getConstantArgs();
    if (args != null) {
      sb.append(args.toString());
//      for (List<String> arg : args) {
//        if (arg == null) {
//          sb.append("unkown, ");
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
    sb.append("\n---------------------------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isLoadingConstructor(Stmt stmt) {
    Boolean itis = false;
    if (StaticUtils.isIt(stmt, CLASS, SIG[0]))
      itis = true;
    else if (StaticUtils.isIt(stmt, CLASS, SIG[1]))
      itis = true;
    return itis;
  }
}
