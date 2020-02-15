/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.construct;

import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;

import lu.uni.snt.jungao.codetheftfinder.Global;

/**
 * @author jun.gao
 *
 */
public class Constructor {
  private static Constructor singleton = null;
  private ComponentsBuilder builder;
  private ComponentsConnector connector;

  /**
   * Construct components and the relationship.
   */
  private Constructor() {
    construct();
  }
  
  public static Constructor getInstance() {
    if (singleton == null) {
      singleton = new Constructor();
    }
    return singleton;
  }
  
  /**
   * Build up all the components.
   * Contains the main logic of constructing all components.
   */
  private void construct() {
    preConstruct();
    builder = new ComponentsBuilder();
    builder.build();
    connector = new ComponentsConnector();
    connector.connect();
  }
  
  /**
   * Run FlowDroid to get relevant resources e.g., callgraph.
   */
  private void preConstruct() {
//    String ssf = "/Users/jun.gao/Documents/work/tools/FlowDroid/soot-infoflow-android/SourcesAndSinks.txt";
    InfoflowAndroidConfiguration infoConf = new InfoflowAndroidConfiguration();
    infoConf.getAnalysisFileConfig().setAndroidPlatformDir(Global.androidSdk);
    infoConf.getAnalysisFileConfig().setTargetAPKFile(Global.apk);
    // allow to load multiple dex files in apk.
    infoConf.setMergeDexFiles(true);
    SetupApplication infoflow = new SetupApplication(infoConf);
//    infoflow.getConfig().setWriteOutputFiles(true);
    try{
      infoflow.constructCallgraph();
//      infoflow.runInfoflow(ssf);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
