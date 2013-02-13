package net.foxopen.utils;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class XPath {
  public static Namespace ns_fm = Namespace.getNamespace("fm", "http://www.og.dti.gov/fox_module");

  public static List<Element> run(String xpath, org.jdom2.Document document) {
    XPathExpression<Element> actionsXPath = XPathFactory.instance().compile(xpath, Filters.element(), null, ns_fm);
    List<Element> results = actionsXPath.evaluate(document);
    return results;
  }
}
