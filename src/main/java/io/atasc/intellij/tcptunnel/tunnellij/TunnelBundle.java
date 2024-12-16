package io.atasc.intellij.tcptunnel.tunnellij;

import java.util.PropertyResourceBundle;

/**
 * @author boruvka
 * @since
 */
public class TunnelBundle {

  private static java.util.ResourceBundle bundle;

  public static java.util.ResourceBundle getBundle() {
    if (bundle == null) {
//          bundle = PropertyResourceBundle
//              .getBundle("io/atasc/intellij/nettunnel/tunnellij/TunnelPlugin");

      bundle = PropertyResourceBundle.getBundle("tunnellij.TunnelPlugin");
      //System.out.println(bundle.getString("TunnelliJ.version")); // Sostituisci "key1"
    }
    return bundle;
  }
}
