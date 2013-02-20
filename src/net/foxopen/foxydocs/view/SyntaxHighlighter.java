package net.foxopen.foxydocs.view;

import static net.foxopen.foxydocs.FoxyDocs.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.xmlparser.XmlRegion;
import net.xmlparser.XmlRegionAnalyzer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GlyphMetrics;

public class SyntaxHighlighter {

  private final static HashMap<String, StyleRange[]> cache = new HashMap<String, StyleRange[]>();

  public static String getHash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    digest.update(input.getBytes("UTF-8"));
    return new String(digest.digest());
  }

  public static StyleRange[] getStyle(final StyledText target) {
    String hash = "";
    try {
      hash = getHash(target.getText());
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (hash != "" && cache.containsKey(hash)) {
      return cache.get(hash);
    }
    ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();
    XmlRegionAnalyzer analyzer = new XmlRegionAnalyzer();
    String xml = target.getText();
    List<XmlRegion> regions = analyzer.analyzeXml(xml);
    for (XmlRegion xr : regions) {
      // Create a collection to hold the StyleRanges
      int regionLength = xr.getEnd() - xr.getStart();
      switch (xr.getXmlRegionType()) {
      case MARKUP:
        // Name is red
        ranges.add(new StyleRange(xr.getStart(), regionLength, DARK_RED, null));
        // <, > and / in blue
        char[] cArray = xml.substring(xr.getStart(), xr.getEnd()).toCharArray();
        for (int i = 0; i < cArray.length; i++) {
          switch (cArray[i]) {
          case '<':
          case '>':
          case '/':
            ranges.add(new StyleRange(xr.getStart() + i, 0, BLUE, null)); // Last
            break;
          default:
            break;
          }
        }

        break;
      case ATTRIBUTE:
        ranges.add(new StyleRange(xr.getStart(), regionLength, RED, null));
        break;
      case ATTRIBUTE_VALUE:
        // Color the two first and the last char in blue
        ranges.add(new StyleRange(xr.getStart(), 2, BLUE, null));
        ranges.add(new StyleRange(xr.getStart() + regionLength - 1, 1, BLUE, null));
        break;
      case MARKUP_VALUE:
        // Default
        break;
      case COMMENT:
        ranges.add(new StyleRange(xr.getStart(), regionLength, GREY, null));
        break;
      case INSTRUCTION:
        ranges.add(new StyleRange(xr.getStart(), regionLength, GREEN, null));
        break;
      case CDATA:
        // Default
        break;
      case WHITESPACE:
        // Nothing
        break;
      case UNEXPECTED:
        ranges.add(new StyleRange(xr.getStart(), regionLength, PURPLE, null, SWT.BOLD));
        break;
      default:
        break;
      }
    }
    StyleRange[] styles = ranges.toArray(new StyleRange[0]);
    cache.put(hash, styles);
    return styles;
  }

  public static void addSyntaxHighligherListener(final StyledText target) {
    // Line number
    target.addLineStyleListener(new LineStyleListener() {

      @Override
      public void lineGetStyle(LineStyleEvent event) {
        event.bulletIndex = target.getLineAtOffset(event.lineOffset);
        StyleRange style = new StyleRange();
        style.metrics = new GlyphMetrics(0, 0, Integer.toString(target.getLineCount() + 1).length() * 12);
        // Line number
        event.bullet = new Bullet(ST.BULLET_NUMBER, style);
        // Colours
        event.styles = getStyle(target);
      }
    });

  }

}
