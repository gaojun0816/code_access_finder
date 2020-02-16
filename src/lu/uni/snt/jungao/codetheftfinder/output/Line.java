/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.output;

import java.util.ArrayList;
import java.util.List;

import lu.uni.snt.jungao.codetheftfinder.ManifestInfo;
import lu.uni.snt.jungao.codetheftfinder.output.exceptions.SizeTooSmallException;

/**
 * @author jun.gao
 *
 */
public class Line {
  private Type type;
  private int stopLevel;
  private String pkgName, appName, clsName, methodOrConstructorSignatureOrFieldName, argsOrValue, setAccessible, creatorFlag,
                  creatorSite, invocationOrFieldSettingSite;
  private Boolean invokedOrSetOrNewed, isStatic;

  /**
   * 
   */
  public Line(String appName) {
    // TODO Auto-generated constructor stub
    this(appName, null, null, null, null, Type.UNKNOWN, null, false, null, null, null, 1);
  }
  
  public Line(String appName, String creatorFlag, String clsName, String methodOrConstructorSignatureOrFieldName,
      String argsOrValue, Type type, String setAccessible, Boolean invokedOrSetOrNewed, Boolean isStatic,
      String creatorSite, String invocationOrFieldSettingSite, int stopLevel) {
    pkgName = ManifestInfo.getPkgName();
    this.appName = appName;
    this.creatorFlag = creatorFlag;
    this.clsName = clsName;
    this.methodOrConstructorSignatureOrFieldName = methodOrConstructorSignatureOrFieldName;
    this.argsOrValue = argsOrValue;
    this.type = type;
    this.setAccessible = setAccessible;
    this.invokedOrSetOrNewed = invokedOrSetOrNewed;
    this.isStatic = isStatic;
    this.creatorSite = creatorSite;
    this.invocationOrFieldSettingSite = invocationOrFieldSettingSite;
    this.stopLevel = stopLevel;
  }
  
  public static enum Type {
    METHOD, FIELD, CONSTRUCTOR, UNKNOWN
  }
  
  /**
   * Return a list of replications of current Line instance.
   * 
   * @param size number of replications need to be returned.
   * @param inclCur indicate whether the current line instance should be included or not.
   *        If true, current instance will be the first in the returned list.
   * @return a list of replications.
   * @throws SizeTooSmallException 
   */
  public List<Line> multiReplicate(int size, boolean inclCur) throws SizeTooSmallException {
    if (size < 1) throw new SizeTooSmallException("The required size need to be bigger than 0!");
    List<Line> lines = new ArrayList<>();
    int i = 0;
    if (inclCur) {
      lines.add(this);
      i++;
    }
    while(i < size) {
      lines.add(this.get1Copy());
      i++;
    }
    return lines;
  }
  
  /**
   * Get a new Line object which is a copy of current Line instance.
   * 
   * @return
   */
  public Line get1Copy(){
    return new Line(appName, creatorFlag, clsName, methodOrConstructorSignatureOrFieldName, 
                    argsOrValue, type, setAccessible, invokedOrSetOrNewed, isStatic, creatorSite, invocationOrFieldSettingSite, stopLevel);
  }
  
  public void setCreatorFlag(String creatorFlag) {
    this.creatorFlag = creatorFlag;
  }

  public void setClsName(String clsName) {
    this.clsName = clsName;
  }
  
  public void setMethodOrConstructorSignatureOrFieldName(String methodOrConstructorSignatureOrFieldName) {
    this.methodOrConstructorSignatureOrFieldName = methodOrConstructorSignatureOrFieldName;
  }
  
  public void setArgsOrValue(String argsOrValue) {
    this.argsOrValue = argsOrValue;
  }
  
  public void setType(Type type) {
    this.type = type;
  }
  
  public void setSetAccessible(String setAccessible) {
    this.setAccessible = setAccessible;
  }
  
  public void setIvokedOrSetOrNewed() {
    invokedOrSetOrNewed = true;
  }
  
  public void setIsStatic(Boolean isStatic) {
    this.isStatic = isStatic;
  }
  
  public void setCreatorSite(String creatorSite) {
    this.creatorSite = creatorSite;
  }
  
  public void setInvocationOrFieldSettingSite(String invocationOrFieldSettingSite) {
    this.invocationOrFieldSettingSite = invocationOrFieldSettingSite;
  }
  
  public void upgradeStopLevel() {
    // the level cannot be bigger than 5.
    if (this.stopLevel < 5) 
      this.stopLevel++;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(pkgName);
    sb.append(";");

    if (appName != null)
      sb.append(appName);
    sb.append(";");

    if (clsName != null)
      sb.append(clsName);
    sb.append(";");

    if (creatorFlag != null)
      sb.append(creatorFlag);
    sb.append(";");

//    if (argsOrValue != null)
//      sb.append(argsOrValue);
//    sb.append(";");

    switch (type) {
      case UNKNOWN:
        break;
      case METHOD:
        sb.append("method");
        break;
      case FIELD:
        sb.append("field");
        break;
      case CONSTRUCTOR:
        sb.append("constructor");
        break;
      default:
        break;
    }
    sb.append(";");

    if (methodOrConstructorSignatureOrFieldName != null)
      sb.append(methodOrConstructorSignatureOrFieldName);
    sb.append(";");

    if (setAccessible != null)
      sb.append(setAccessible);
    sb.append(";");

    if (invokedOrSetOrNewed)
      sb.append("yes");
    else
      sb.append("no");
    sb.append(";");

    if (isStatic != null) {
      if (isStatic)
        sb.append("yes");
      else
        sb.append("no");
    }
    sb.append(";");
    
    if (creatorSite != null)
      sb.append(creatorSite);
    sb.append(";");
    
    if (invocationOrFieldSettingSite != null)
      sb.append(invocationOrFieldSettingSite);
    
    sb.append(";");
    sb.append(stopLevel);
    
    sb.append("\n");
    
    return sb.toString();
  }

}
