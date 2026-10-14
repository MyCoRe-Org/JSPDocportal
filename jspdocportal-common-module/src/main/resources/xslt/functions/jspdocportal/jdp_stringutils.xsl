<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:array="http://www.w3.org/2005/xpath-functions/array"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns:jdp_stringutils="http://www.mycore.de/xslt/jspdocportal/jdp_stringutils"
  xmlns:_jdp_stringutils="http://www.mycore.de/xslt/jspdocportal/jdp_stringutils_private"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="#all">

  <!-- creates html links for URLs in plain text (for all strings starting with http(s)... -->
  <!-- accepts a sequence - non text nodes are copied without modifications -->
  <xsl:function name="jdp_stringutils:activate-hyperlinks" as="node()*">
    <xsl:param name="input" as="item()*"/>
    <xsl:for-each select="$input">
      <xsl:choose>
        <!-- 1. item is string or text() node -> call private function to enable hyperlinks-->
        <xsl:when test=". instance of xs:anyAtomicType or self::text()">
          <xsl:sequence select="_jdp_stringutils:activate-hyperlinks-in-string(string(.))"/>
        </xsl:when>
        <!-- 2. item is an element -> call function recursively on element's content -->
        <xsl:when test=". instance of element()">
          <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:sequence select="jdp_stringutils:activate-hyperlinks(node())"/>
          </xsl:copy>
        </xsl:when>
        <!-- 3. otherwise -> comments and processing instructions are simply copied-->
        <xsl:otherwise>
          <xsl:sequence select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:function>

  <!-- private utility function - processing a single string node() -->
  <xsl:function name="_jdp_stringutils:activate-hyperlinks-in-string" as="node()*">
    <xsl:param name="input-text" as="xs:string"/>

    <xsl:analyze-string select="$input-text" regex="(https?://[^\s()\[\]]+)">
      <xsl:matching-substring>
        <xsl:variable name="full" select="regex-group(1)"/>
        <xsl:variable name="url" select="replace($full, '[.,]+$', '')"/>
        <xsl:variable name="trailing" select="substring($full, string-length($url) + 1)"/>

        <a href="{$url}"><xsl:value-of select="$url"/></a>
        <xsl:value-of select="$trailing"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:function>

  <!-- replaces linebreaks with <br> nodes -->
  <xsl:function name="jdp_stringutils:lf2br"  as="item()*">
    <!-- Akzeptiert eine Sequenz aus beliebigen Items (Knoten, Strings, etc.) -->
    <xsl:param name="inputSequence" as="item()*"/>
    
    <xsl:for-each select="$inputSequence">
      <xsl:choose>
        <!-- 1. item is string or text() node -> replace linebreaks with <br/>-tags  -->
        <xsl:when test=". instance of text() or . instance of xs:anyAtomicType">
          <xsl:for-each select="tokenize(string(.), '\r?\n')">
            <xsl:value-of select="."/>
            <xsl:if test="position() ne last()">
              <br />
            </xsl:if>
          </xsl:for-each>
        </xsl:when>
        <!-- 2. item is element node -> call function recursive -->
        <xsl:when test=". instance of element()">
          <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:sequence select="jdp_stringutils:lf2br(node())"/>
          </xsl:copy>
        </xsl:when>
        <!-- 3. otherwise -> comments and processing instructions are simply copied-->
        <xsl:otherwise>
          <xsl:sequence select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:function>

</xsl:stylesheet>
