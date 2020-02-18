/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lu.uni.snt.jungao.codetheftfinder.components.ConstructorInvocation;
import lu.uni.snt.jungao.codetheftfinder.components.FieldOrMethodAccessibleSetting;
import lu.uni.snt.jungao.codetheftfinder.components.FieldValueSetting;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedClass;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedClassLoader;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedComponent;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedConstructor;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedCreator;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedField;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedMethod;
import lu.uni.snt.jungao.codetheftfinder.components.MethodInvocation;
import lu.uni.snt.jungao.codetheftfinder.output.exceptions.ListEmptyOrNullException;
import lu.uni.snt.jungao.codetheftfinder.output.exceptions.SizeTooSmallException;

/**
 * @author jun.gao
 *
 */
public class Printer {
  private static List<Line> records = new ArrayList<>();
  private static boolean recordsMade = false;
  /**
   * 
   */
  public Printer() {
    // TODO Auto-generated constructor stub
  }
  
  public static void print() throws ListEmptyOrNullException, SizeTooSmallException {
    // prepare the records if it has not been made.
    if (!recordsMade) makeRecords();
    for (Line l : records) {
      System.out.print(l);
    }
  }
  
  public static void print2File(String path) throws ListEmptyOrNullException, SizeTooSmallException { 
    // prepare the records if it has not been made.
    if (!recordsMade) makeRecords();
    File file = new File(path);
    try {
      FileWriter fr = new FileWriter(file);
      BufferedWriter br = new BufferedWriter(fr);
      for (Line l : records) {
        br.write(l.toString());
      }
      br.close();
      fr.close();
    } catch (IOException e) {
      System.err.print("[Printer::print2Fiel] ");
      System.err.println(e);
    }
  }
  
  private static void makeRecords() throws ListEmptyOrNullException, SizeTooSmallException {
    recordsMade = true;
    for (LoadedComponent rawCreator : LoadedCreator.tracker) {
      LoadedCreator creator = (LoadedCreator) rawCreator;
      traverseCreator(creator);
    }
  }
  
  /**
   * Traverse to make the Line records start from a LoadedCreator instance.
   * @param creator
   * 
   * TODO situation of multiple possible constant names.
   * @throws SizeTooSmallException 
   */
  private static void traverseCreator(LoadedCreator creator) throws SizeTooSmallException {
    Line currentRecord;
    List<String> appNames = creator.getConstantAppName();
    List<String> flags = creator.getConstantFlag();
    if (appNames == null) {
      appNames = new ArrayList<>();
      appNames.add("Unknown");
    }
    currentRecord = new Line(appNames.get(0));
    if (flags == null) {
      flags = new ArrayList<>();
      flags.add("Unknown");
    }
    currentRecord.setCreatorFlag(flags.get(0));
    currentRecord.setCreatorSite(creator.getContainer().getSignature());
    List<LoadedComponent> loaders = creator.getSuccessorComponents();
    if (loaders == null) {
      records.add(currentRecord);
    } else {
      List<Line> replicas = currentRecord.multiReplicate(loaders.size(), true);
      for (int i=0; i<loaders.size(); i++) {
        traverseLoader((LoadedClassLoader) loaders.get(i), replicas.get(i));
      }
    }
  }
  
  private static void traverseLoader(LoadedClassLoader loader, Line record) throws SizeTooSmallException {
    List<LoadedComponent> classes = loader.getSuccessorComponents();
    if (classes == null) {
      record.upgradeStopLevel();
      records.add(record);
    } else {
      List<Line> replicas = record.multiReplicate(classes.size(), true);
      for (int i=0; i<classes.size(); i++) {
        Line l = replicas.get(i);
        l.upgradeStopLevel();
        traverseClass((LoadedClass) classes.get(i), l);
      }
    }
  }
  
  private static void traverseClass(LoadedClass cls, Line record) throws SizeTooSmallException {
    List<String> classNames = cls.getConstantClassName();
    if (classNames == null) {
      classNames = new ArrayList<>();
      classNames.add("Unknown");
    }
    record.setClsName(classNames.get(0));
    List<LoadedComponent> successors = cls.getSuccessorComponents();
    if (successors == null) {
      record.upgradeStopLevel();
      records.add(record);
    } else {
      List<Line> replicas = record.multiReplicate(successors.size(), true);
      for (int i=0; i<successors.size(); i++) {
        LoadedComponent successor = successors.get(i);
        Line l = replicas.get(i);
        l.upgradeStopLevel();
        if (successor instanceof LoadedMethod) {
          traverseMethod((LoadedMethod) successor, l);
        } else if (successor instanceof LoadedField) {
          traverseField((LoadedField) successor, l);
        } else if (successor instanceof LoadedConstructor) {
          traverseConstructor((LoadedConstructor) successor, l);
        }else if (successor instanceof ConstructorInvocation) {
          l.setType(Line.Type.CONSTRUCTOR);
          traverseConstructionInvocation((ConstructorInvocation) successor, l);
        } else {
          StringBuilder sb = new StringBuilder();
          sb.append("[Printer::traverseClass] ");
          sb.append("unknown component type!");
          System.err.println(sb);
        }
      }
    }
  }
  
  private static void traverseMethod(LoadedMethod method, Line record) throws SizeTooSmallException {
    StringBuilder signature = new StringBuilder();
    List<String> methodNames = method.getConstantMethodName();
    List<List<String>> args = method.getConstantMethodArgs();
    List<Integer> argSizes = method.getSizeOfArgs();
    if (methodNames == null) {
      methodNames = new ArrayList<>();
      methodNames.add("Unknown_method_name");
    }
    signature.append(methodNames.get(0));
    signature.append("(");
    if (args.size() == 0) {
      if (argSizes == null) {
        signature.append("unknown_argument");
      } else {
        Integer size = argSizes.get(0);
        if (size == 0) {
          // nothing need to be done for none parameter method.
        } else {
          signature.append(size);
          signature.append(" unknown_argument");
        }
      }
    } else {
      for (List<String> arg : args) {
        if (arg == null) {
          signature.append("unknown,");
        } else {
          signature.append(stringClean(arg.get(0)));
          signature.append(",");
        }
      }
      signature.deleteCharAt(signature.length() - 1);
    }

    signature.append(")");
    record.setMethodOrConstructorSignatureOrFieldName(signature.toString());
    record.setType(Line.Type.METHOD);
    List<LoadedComponent> successors = method.getSuccessorComponents();
    if (successors == null) {
      record.upgradeStopLevel();
      records.add(record);
    } else {
      List<Line> replicas = record.multiReplicate(successors.size(), true);
      for (int i=0; i<successors.size(); i++) {
        LoadedComponent successor = successors.get(i);
        Line l = replicas.get(i);
        l.upgradeStopLevel();
        if (successor instanceof MethodInvocation) {
          treverseMethodInvocation((MethodInvocation) successor, l);
        } else if (successor instanceof FieldOrMethodAccessibleSetting) {
          treverseFieldOrMethodAccessibleSetting((FieldOrMethodAccessibleSetting) successor, l);
        } else {
          StringBuilder sb = new StringBuilder();
          sb.append("[Printer::traverseMethod] ");
          sb.append("unknown component type!");
          System.err.println(sb);
        }
      }
    }
  }
  
  private static void treverseMethodInvocation(MethodInvocation invocation, Line record) {
    record.setIvokedOrSetOrNewed();
    record.setIsStatic(invocation.isStaticInvocation());
    record.setInvocationOrFieldSettingSite(invocation.getContainer().getSignature());
    record.upgradeStopLevel();
    records.add(record);
  }

  private static void treverseFieldOrMethodAccessibleSetting(FieldOrMethodAccessibleSetting setting, Line record) {
    List<String> accessibles = setting.getConstantAccessibility();
    if (accessibles == null) {
      record.setSetAccessible("Unknown");
    } else {
      record.setSetAccessible(accessibles.get(0));
    }
    record.upgradeStopLevel();
    records.add(record);
  }

  private static void traverseField(LoadedField field, Line record) throws SizeTooSmallException {
    List<String> fieldNames = field.getConstantFieldName();
    if (fieldNames == null) {
      fieldNames = new ArrayList<>();
      fieldNames.add("Unknown");
    }
    record.setMethodOrConstructorSignatureOrFieldName(fieldNames.get(0));
    record.setType(Line.Type.FIELD);
    List<LoadedComponent> successors = field.getSuccessorComponents();
    if (successors == null) {
      record.upgradeStopLevel();
      records.add(record);
    } else {
      List<Line> replicas = record.multiReplicate(successors.size(), true);
      for (int i=0; i<successors.size(); i++) {
        LoadedComponent successor = successors.get(i);
        Line l = replicas.get(i);
        l.upgradeStopLevel();
        if (successor instanceof FieldOrMethodAccessibleSetting) {
          treverseFieldOrMethodAccessibleSetting((FieldOrMethodAccessibleSetting) successor,l);
        } else if (successor instanceof FieldValueSetting) {
          treverseFieldValueSetting((FieldValueSetting) successor, l);
        } else {
          StringBuilder sb = new StringBuilder();
          sb.append("[Printer::traverseField] ");
          sb.append("unknown component type!");
          System.err.println(sb);
        }
      }
    }
  }
  
  private static void treverseFieldValueSetting(FieldValueSetting setting, Line record) {
    record.setIvokedOrSetOrNewed();
    record.setIsStatic(setting.isStaticFieldSetting());
    record.setInvocationOrFieldSettingSite(setting.getContainer().getSignature());
    record.upgradeStopLevel();
    records.add(record);
  }

  private static void traverseConstructor(LoadedConstructor constructor, Line record) throws SizeTooSmallException {
    List<List<String>> args = constructor.getConstantArgs();
    List<Integer> argSizes = constructor.getSizeOfArgs();
    StringBuilder sig = new StringBuilder();
    sig.append("(");
    if (args.size() == 0) {
      if (argSizes == null) {
        sig.append("unknown_argument");
      } else {
        Integer size = argSizes.get(0);
        if (size == 0) {
          // nothing need to be done for none parameter method.
        } else {
          sig.append(size);
          sig.append(" unknown_argment");
        }
      }
    } else {
      for (List<String> arg : args) {
        if (arg == null) {
          sig.append("unknown,");
        } else {
          sig.append(stringClean(arg.get(0)));
          sig.append(",");
        }
      }
      sig.deleteCharAt(sig.length() - 1);
    }
    sig.append(")");
    record.setMethodOrConstructorSignatureOrFieldName(sig.toString());
    record.setType(Line.Type.CONSTRUCTOR);
    List<LoadedComponent> invocations = constructor.getSuccessorComponents();
    if (invocations == null) {
      record.upgradeStopLevel();
      records.add(record);
    } else {
      List<Line> replicas = record.multiReplicate(invocations.size(), true);
      for (int i=0; i<invocations.size(); i++) {
        Line l = replicas.get(i);
        l.upgradeStopLevel();
        traverseConstructionInvocation(invocations.get(i), l);
      }
    }
  }
  
  private static void traverseConstructionInvocation(LoadedComponent invocation, Line record) {
    record.setIvokedOrSetOrNewed();
    record.setInvocationOrFieldSettingSite(invocation.getContainer().getSignature());
    record.upgradeStopLevel(5);
    records.add(record);
  }

  private static String stringClean(String s) {
    if (s.startsWith("class \"")) {
      s = s.substring(7);
    }
    
    if (s.endsWith("\"")) {
      s = s.substring(0, s.length() - 1);
    }
    
    if (s.endsWith(";")) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }
}
