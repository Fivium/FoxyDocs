<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fm="http://www.og.dti.gov/fox_module">
	<xsl:output method="xml" indent="yes" />
	<xsl:template match="/xs:schema/xs:annotation/xs:appinfo/fm:module">
		<xsl:variable name="MODULE_LIST" select="MODULE_LIST" />
		<head>
			<title>
				<xsl:value-of select="fm:header/fm:name" />
				- Module Specification
			</title>
			<link type="text/css" rel="stylesheet" href="assets/xsl/style.css" />
		</head>
		<body>
			<div class="header">
				<div class="foxOpenLogo">
					<a href="summary.html">
						<img src="assets/xsl/logo.png" alt="FOXOpen" border="0" />
					</a>
				</div>
				<div class="nav">
					<h1>
						<xsl:value-of select="fm:header/fm:application-title" />
						-
						<xsl:value-of select="fm:header/fm:title" />
						(
						<xsl:value-of select="fm:header/fm:name" />
						)
					</h1>
				</div>
			</div>
			<p>
				<xsl:value-of select="fm:header/fm:description" />
			</p>
			<h1>Module References</h1>
			<h2>Libraries Used</h2>
			<xsl:choose>
				<xsl:when test="not(fm:library-list/fm:library)">
					None
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="fm:library-list/fm:library">
						<xsl:sort select="text()" />
						<xsl:variable name="MODULE_NAME" select="text()" />
						<xsl:choose>
							<xsl:when test="$MODULE_LIST/MODULE[text() = $MODULE_NAME]">
								<xsl:element name="a">
									<xsl:attribute name="href"><xsl:value-of
										select="text()" />.html</xsl:attribute>
									<xsl:value-of select="text()" />
								</xsl:element>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="text()" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:if test="position() != last()">
							,
						</xsl:if>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
			<h2>States</h2>
			<xsl:choose>
				<xsl:when test="not(fm:state-list/fm:state)">
					None
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="fm:state-list/fm:state">
						<xsl:sort select="@name" />
						<xsl:value-of select="@name" />
						<xsl:if test="position() != last()">
							,
						</xsl:if>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
			<h2>Sub-Modules Called</h2>
			<xsl:choose>
				<xsl:when test="not(//fm:call-module)">
					None
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="//fm:call-module">
						<xsl:sort select="@module" />
						<xsl:sort select="@theme" />
						<xsl:variable name="MODULE_NAME" select="@module" />
						<p>
							<xsl:choose>
								<xsl:when test="$MODULE_LIST/MODULE[text() = $MODULE_NAME]">
									<xsl:element name="a">
										<xsl:attribute name="href"><xsl:value-of
											select="@module" />.html</xsl:attribute>
										<xsl:value-of select="@module" />
									</xsl:element>
									-
									<xsl:element name="a">
										<xsl:attribute name="href"><xsl:value-of
											select="@module" />.html#entry-theme-<xsl:value-of
											select="@theme" /></xsl:attribute>
										<xsl:value-of select="@theme" />
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="@module" />
									-
									<xsl:value-of select="@theme" />
								</xsl:otherwise>
							</xsl:choose>
						</p>
						<br />
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
			<h1>Entry Themes</h1>
			<xsl:choose>
				<xsl:when test="not(fm:entry-theme-list/fm:entry-theme)">
					None
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="fm:entry-theme-list/fm:entry-theme">
						<p>
							<xsl:element name="a">
								<xsl:attribute name="name">entry-theme-<xsl:value-of
									select="@name" /></xsl:attribute>
							</xsl:element>
						</p>
						<h2>
							<xsl:value-of select="@name" />
						</h2>
						<p>
							<xsl:value-of select="fm:documentation/fm:description" />
						</p>
						<h3>Input Parameters</h3>
						<p>
							<xsl:choose>
								<xsl:when test="not(fm:param-list/fm:param)">
									None
								</xsl:when>
								<xsl:otherwise>
									<ul>
										<xsl:for-each select="fm:param-list/fm:param">
											<li>
												<xsl:choose>
													<xsl:when test="@mand='.'">
														<b>
															<xsl:value-of select="@xpath" />
															(
															<xsl:value-of select="@type" />
															)
														</b>
													</xsl:when>
													<xsl:otherwise>
														<xsl:value-of select="@xpath" />
														(
														<xsl:value-of select="@type" />
														)
													</xsl:otherwise>
												</xsl:choose>
											</li>
										</xsl:for-each>
									</ul>
								</xsl:otherwise>
							</xsl:choose>
						</p>
						<h3>Output Parameters</h3>
						<p>
							<xsl:choose>
								<xsl:when test="not(fm:return-list/fm:return)">
									None
								</xsl:when>
								<xsl:otherwise>
									<ul>
										<xsl:for-each select="fm:return-list/fm:return">
											<li>
												<xsl:value-of select="@xpath" />
												(
												<xsl:value-of select="@type" />
												)
												<xsl:choose>
													<xsl:when test="@mand='.'">
														Required
													</xsl:when>
													<xsl:otherwise>
														Optional
													</xsl:otherwise>
												</xsl:choose>
											</li>
										</xsl:for-each>
									</ul>
								</xsl:otherwise>
							</xsl:choose>
						</p>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</body>
	</xsl:template>
</xsl:stylesheet>
