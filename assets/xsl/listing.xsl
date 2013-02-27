<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fm="http://www.og.dti.gov/fox_module">
  <xsl:output method="html" doctype-system="http://www.w3.org/TR/html4/strict.dtd" doctype-public="-//W3C//DTD HTML 4.01//EN" indent="yes"/>
  <xsl:template match="/*">
    <html>
      <head>
        <title>Module Listing</title>
        <link rel="stylesheet" type="text/css" href="style.css"/>
      </head>
      <body>
        <div class="header">
          <h1>Module Listing</h1>
        </div>
        <div class="fmHeader">
          <ul>
            <xsl:for-each select="MODULE">
              <li>
                <xsl:element name="a">
                  <xsl:attribute name="href"><xsl:value-of select="text()"/>.html</xsl:attribute>
                  <xsl:attribute name="target">moduleFrame</xsl:attribute>
                  <xsl:value-of select="text()"/>
                </xsl:element>
              </li>
            </xsl:for-each>
          </ul>
        </div>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>