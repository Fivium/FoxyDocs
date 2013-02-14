package net.foxopen.fde.view;

import static net.foxopen.utils.Constants.BLUE;
import static net.foxopen.utils.Constants.CYAN;
import static net.foxopen.utils.Constants.GREEN;
import static net.foxopen.utils.Constants.PURPLE;
import static net.foxopen.utils.Constants.RED;

import java.util.ArrayList;
import java.util.List;

import net.xmlparser.XmlRegion;
import net.xmlparser.XmlRegionAnalyzer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GlyphMetrics;

public class SyntaxHighlighter {

  public static void addSyntaxHighligherListener(final StyledText target) {

    // Line number
    target.addLineStyleListener(new LineStyleListener() {

      @Override
      public void lineGetStyle(LineStyleEvent event) {
        event.bulletIndex = target.getLineAtOffset(event.lineOffset);
        StyleRange style = new StyleRange();
        style.metrics = new GlyphMetrics(0, 0, Integer.toString(target.getLineCount() + 1).length() * 12);
        event.bullet = new Bullet(ST.BULLET_NUMBER, style);
      }
    });

    // Colours
    target.addExtendedModifyListener(new ExtendedModifyListener() {
      public void modifyText(ExtendedModifyEvent event) {
        int end = event.start + event.length - 1;
        String text = target.getText(event.start, end);

        XmlRegionAnalyzer analyzer = new XmlRegionAnalyzer();
        List<XmlRegion> regions = analyzer.analyzeXml(text);
        for (XmlRegion xr : regions) {
          // Create a collection to hold the StyleRanges
          ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();
          int regionLength = xr.getEnd() - xr.getStart();
          switch (xr.getXmlRegionType()) {
          case MARKUP:
            ranges.add(new StyleRange(xr.getStart(), regionLength, RED, null));
            break;
          case ATTRIBUTE:
            ranges.add(new StyleRange(xr.getStart(), regionLength, RED, null, SWT.BOLD));
            break;
          case ATTRIBUTE_VALUE:
            ranges.add(new StyleRange(xr.getStart(), regionLength, GREEN, null, SWT.BOLD));
            break;
          case MARKUP_VALUE:
            ranges.add(new StyleRange(xr.getStart(), regionLength, GREEN, null));
            break;
          case COMMENT:
            ranges.add(new StyleRange(xr.getStart(), regionLength, BLUE, null));
            break;
          case INSTRUCTION:
            ranges.add(new StyleRange(xr.getStart(), regionLength, CYAN, null));
            break;
          case CDATA:
            ranges.add(new StyleRange(xr.getStart(), regionLength, BLUE, null, SWT.BOLD));
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
          // If we have any ranges to set, set them
          if (!ranges.isEmpty()) {
            target.replaceStyleRanges(xr.getStart(), regionLength, (StyleRange[]) ranges.toArray(new StyleRange[0]));
          }
        }
      }
    });
  }

}
