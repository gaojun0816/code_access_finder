/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder;

import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

import java.io.IOException;
import java.util.Set;

/**
 * @author jun.gao
 *
 */
public class ManifestInfo {
  private static ManifestInfo singleton = null;
  private static ProcessManifest processManifest;
  private static String apkDir = null;

  private ManifestInfo() {
      try {
          processManifest = new ProcessManifest(apkDir);
      } catch (IOException e) {
          e.printStackTrace();
          System.exit(-1);
      } catch (XmlPullParserException e) {
          e.printStackTrace();
          System.exit(-1);
      }
  }

  public static void init() {
      if (ManifestInfo.apkDir == null) {
          ManifestInfo.apkDir = Global.apk;
      }
      if (singleton == null) {
          singleton = new ManifestInfo();
      }
  }

  private static void checkInitialization() {
      if (singleton == null) {
          throw new AssertionError("method 'init' have not been called");
      }
  }

  public static String getPkgName() {
      checkInitialization();
      return processManifest.getPackageName();
  }

  public static String getAppName() {
      checkInitialization();
      return processManifest.getApplicationName();
  }

  public static Set<String> getEntryPoints() {
      checkInitialization();
      return processManifest.getEntryPointClasses();
  }

  public static ProcessManifest getProcessManifest() {
      return processManifest;
  }
}
