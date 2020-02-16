/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder;

import java.nio.file.Paths;
import java.util.Collections;

import lu.uni.snt.jungao.codetheftfinder.construct.Constructor;
import lu.uni.snt.jungao.codetheftfinder.output.Printer;
import lu.uni.snt.jungao.codetheftfinder.output.exceptions.ListEmptyOrNullException;
import lu.uni.snt.jungao.codetheftfinder.output.exceptions.SizeTooSmallException;


/**
 * @author jun.gao
 *
 */
public class Finder {
  
  public static void main(String[] args) throws ListEmptyOrNullException, SizeTooSmallException {
    setup(args);
//    Constructor constructor = Constructor.getInstance();
    System.out.println("Start job: " + Global.apk);
    Constructor.getInstance();
//    Printer.print();
    Printer.print2File(getOutputFilePath());
    System.out.println("Finder finished job!");
    System.out.println(String.join("", Collections.nCopies(100, "*")));
  }

  private static void setup(String[] args) {
    Global.apk = args[0];
    Global.androidSdk = args[1];
    if (args.length > 2) 
      Global.outputPath = args[2];
    else
      Global.outputPath = ".";
    ManifestInfo.init();
  }
  
  private static String getOutputFilePath() {
    String[] parts = Global.apk.split("/");
    String fileName = parts[parts.length - 1];
    fileName = fileName.split("\\.")[0];
    return Paths.get(Global.outputPath, fileName).toString();
  }
}
