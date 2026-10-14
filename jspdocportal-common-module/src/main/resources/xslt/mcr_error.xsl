<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  exclude-result-prefixes="#all"
  expand-text="yes">
  <xsl:output method="html" indent="yes" />
  
  <xsl:include href="default-parameters.xsl" />
  <xsl:include href="xslInclude:functions" />
  
  <xsl:variable
    name="PageTitle"
    select="mcri18n:translate-with-params('titles.pageTitle.error', concat(' ', /mcr_error/@HttpError))" />

  <xsl:template match="/">
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
    <html lang="de">
      <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      <title>{$PageTitle}</title>
      <style>
        <xsl:text expand-text="no">
        :root {
          --bg: #f7f7f8;
          --card-bg: #ffffff;
          --text: #1a1a1a;
          --muted: #6b7280;
          --accent: #dc2626;
          --border: #e5e7eb;
        }
        @media (prefers-color-scheme: dark) {
          :root {
           --bg: #0f0f10;
           --card-bg: #1a1a1c;
           --text: #f2f2f2;
           --muted: #9ca3af;
           --accent: #ef4444;
           --border: #2c2c2e;
          }
        }
        * { box-sizing: border-box; }
        html, body {
          height: 100%;
          margin: 0;
        }
        body {
          display: flex;
          align-items: center;
          justify-content: center;
          background: var(--bg);
          color: var(--text);
          font-family: "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
          padding: 1.5rem;
        }
        .card {
           background: var(--card-bg);
           border: 1px solid var(--border);
           border-radius: 12px;
           padding: 1rem;
           max-width: 550px;
           width: 100%;
           text-align: center;
           box-shadow: 0 4px 20px rgba(0,0,0,0.05);
         }
         .code {
           font-size: 4rem;
           font-weight: 700;
           color: var(--accent);
           line-height: 1;
           margin: 0 0 0.5rem;
         } 
         h1 {
           font-size: 1.25rem;
           margin: 0 0 0.75rem;
         }
         p {
          color: var(--muted);
          margin: 0 0 1.5rem;
          line-height: 1.5;
        }
        .alert {
          margin: 0 0 1rem;
        }
        a.button {
          display: inline-block;
          background: var(--accent);
          color: #fff;
          text-decoration: none;
          padding: 0.65rem 1.5rem;
          border-radius: 8px;
          font-weight: 600;
          transition: opacity 0.15s ease;
        }
        a.button:hover {
          opacity: 0.85;
        }
        .card-body{
          max-height: 40vh;
          overflow: auto;
        }
        </xsl:text>
      </style>
      </head>
      <body>
        <div class="card">
          <xsl:apply-templates />
          <a class="button" href="{$WebApplicationBaseURL}"><xsl:value-of select="mcri18n:translate('mir.error.backhome')" /></a>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="/mcr_error" priority="0">
      <p class="code">{concat(@HttpError, '!')}</p>
      <h1>{$PageTitle}</h1>
      <p>
        <xsl:copy-of
          select="
            parse-xml-fragment(
              mcri18n:translate-with-params(
                concat('mir.error.codes.', @HttpError),
                fn:escape-html-uri(string(@requestURI))
              )
            )/node()" />
      </p>
      <xsl:choose>
        <xsl:when test="(string(@errorServlet) = 'true' and string-length(text()) gt 1) or exists(exception)">
          <xsl:if test="string(@errorServlet) = 'true' and string-length(text()) gt 1">
            <div class="alert alert-info" role="alert">
              <xsl:attribute name="title">
                <xsl:value-of select="mcri18n:translate('mir.error.message')" />
              </xsl:attribute>
              <xsl:call-template name="lf2br">
                <xsl:with-param name="string" select="text()" />
              </xsl:call-template>
            </div>
          </xsl:if>
          <xsl:if test="exists(exception)">
            <div class="card">
              <div class="card-header bg-danger">
                <xsl:value-of select="concat(mcri18n:translate('error.stackTrace'),' :')" />
              </div>
              <div class="card-body text-start">
                <xsl:for-each select="exception/trace">
                  <pre style="font-size:0.8em;">
                    <xsl:value-of select="." />
                  </pre>
                </xsl:for-each>
              </div>
            </div>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <p>
            <small>
              <xsl:value-of select="mcri18n:translate('error.noInfo')" />
            </small>
          </p>
        </xsl:otherwise>
      </xsl:choose>
      <p>
        <strong>
          <xsl:value-of select="mcri18n:translate('mir.error.finalLine')" />
        </strong>
      </p>
  </xsl:template>

  <xsl:template name="lf2br">
    <xsl:param name="string" as="xs:string" />

    <xsl:for-each select="tokenize($string, '\r?\n')">
      <xsl:value-of select="." />
      <xsl:if test="position() ne last()">
        <br />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
